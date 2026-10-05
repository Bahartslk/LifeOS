import { randomBytes, randomUUID, createHash } from 'crypto';
import { ConflictException, Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { User } from '@prisma/client';
import { AppConfig } from '../../config/configuration';
import { parseDurationMs } from '../../common/utils/duration.util';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { UsersService } from '../users/users.service';
import { RefreshTokensRepository } from './repositories/refresh-tokens.repository';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { AuthTokensDto } from './dto/auth-tokens.dto';
import { LoginResponseDto } from './dto/login-response.dto';
import { LogoutResponseDto } from './dto/logout-response.dto';

/**
 * Precomputed once at module load: a valid bcrypt hash of an arbitrary
 * fixed string, compared against on login when no user matches the given
 * email. Without this, "email doesn't exist" returns instantly while
 * "email exists, wrong password" takes ~bcrypt's compare time — a timing
 * side channel that lets an attacker enumerate registered emails. Always
 * running one bcrypt.compare, real or dummy, equalizes the two paths.
 */
const DUMMY_PASSWORD_HASH = bcrypt.hashSync('lifeos-timing-attack-mitigation', 12);

/**
 * All Authentication business logic. Controllers stay thin (parse request,
 * call this, return); this is the only place register/login/refresh/logout
 * rules live, per CLAUDE.md's "business logic stays out of controllers"
 * rule. Depends on `UsersService` (never `UsersRepository`/`PrismaService`
 * directly) for user lookups, and its own `RefreshTokensRepository` for
 * token persistence.
 */
@Injectable()
export class AuthService {
  constructor(
    private readonly usersService: UsersService,
    private readonly refreshTokensRepository: RefreshTokensRepository,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService<AppConfig, true>,
  ) {}

  async register(dto: RegisterDto): Promise<AuthTokensDto> {
    const existing = await this.usersService.findByEmail(dto.email);
    if (existing) {
      // 409, per docs/15-api-design.md#error-handling ("Registering with an
      // email that already exists").
      throw new ConflictException('An account with this email already exists.');
    }

    const passwordHash = await this.hashPassword(dto.password);
    const user = await this.usersService.create({
      email: dto.email,
      passwordHash,
      displayName: dto.displayName,
    });

    return this.issueTokenPair(user);
  }

  async login(dto: LoginDto): Promise<LoginResponseDto> {
    const user = await this.usersService.findByEmail(dto.email);
    const isPasswordValid = await bcrypt.compare(
      dto.password,
      user?.passwordHash ?? DUMMY_PASSWORD_HASH,
    );

    if (!user || !isPasswordValid) {
      throw new UnauthorizedException('Invalid email or password.');
    }

    const tokens = await this.issueTokenPair(user);
    return { ...tokens, user: this.usersService.toPublicUser(user) };
  }

  async refresh(dto: RefreshTokenDto): Promise<AuthTokensDto> {
    const tokenHash = this.hashToken(dto.refreshToken);
    const record = await this.refreshTokensRepository.findByTokenHash(tokenHash);

    if (!record) {
      throw new UnauthorizedException('Invalid refresh token.');
    }

    if (record.revokedAt) {
      // A revoked token (whether from a prior rotation, a prior reuse
      // attempt, or an explicit logout) must never be usable again.
      // Presenting one is treated as a possible replay/theft signal
      // regardless of *why* it was revoked, so the whole family is revoked
      // defensively here too, per docs/12-project-architecture.md#jwt-strategy.
      await this.refreshTokensRepository.revokeFamily(record.familyId);
      throw new UnauthorizedException('Refresh token has been revoked. Please sign in again.');
    }

    if (record.expiresAt < new Date()) {
      throw new UnauthorizedException('Refresh token has expired.');
    }

    const user = await this.usersService.findById(record.userId);
    if (!user) {
      throw new UnauthorizedException('Account no longer exists.');
    }

    // Rotate: the presented token is revoked in the same operation that
    // issues its replacement, so it can never be validly used again.
    await this.refreshTokensRepository.revokeById(record.id);
    return this.issueTokenPair(user, record.familyId);
  }

  async logout(dto: RefreshTokenDto, currentUserId: string): Promise<LogoutResponseDto> {
    const tokenHash = this.hashToken(dto.refreshToken);
    const record = await this.refreshTokensRepository.findByTokenHash(tokenHash);

    // Only revoke if the token exists, is still active, and belongs to the
    // caller (defense in depth beyond the JwtAuthGuard on this route).
    // Otherwise, succeed anyway — logout must not leak whether a token
    // existed or whose it was.
    if (record && !record.revokedAt && record.userId === currentUserId) {
      await this.refreshTokensRepository.revokeById(record.id);
    }

    return { success: true };
  }

  private async issueTokenPair(user: User, familyId?: string): Promise<AuthTokensDto> {
    // `role` is a client-facing hint only — see `JwtPayload`'s doc comment.
    const payload: Omit<JwtPayload, 'iat' | 'exp'> = {
      sub: user.id,
      email: user.email,
      role: user.role,
    };
    const accessToken = await this.jwtService.signAsync(payload);

    const rawRefreshToken = randomBytes(40).toString('hex');
    const ttlMs = parseDurationMs(this.configService.get('jwt.refreshTokenTtl', { infer: true }));
    await this.refreshTokensRepository.create({
      userId: user.id,
      tokenHash: this.hashToken(rawRefreshToken),
      familyId: familyId ?? randomUUID(),
      expiresAt: new Date(Date.now() + ttlMs),
    });

    return { accessToken, refreshToken: rawRefreshToken };
  }

  /** SHA-256, not bcrypt — see the `RefreshToken` model's doc comment in `prisma/schema.prisma` for why. */
  private hashToken(rawToken: string): string {
    return createHash('sha256').update(rawToken).digest('hex');
  }

  private hashPassword(password: string): Promise<string> {
    const rounds = this.configService.get('auth.bcryptSaltRounds', { infer: true });
    return bcrypt.hash(password, rounds);
  }
}
