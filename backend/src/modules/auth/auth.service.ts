import { randomBytes, randomUUID, createHash } from 'crypto';
import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  Logger,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { RefreshToken, User } from '@prisma/client';
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
import { ChangePasswordDto } from './dto/change-password.dto';

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
  private readonly logger = new Logger(AuthService.name);

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
      this.logRefreshRejected('unknown token');
      throw new UnauthorizedException('Invalid refresh token.');
    }

    if (record.revokedAt) {
      this.logRefreshRejected('revoked token presented again; revoking its whole family', record);
      // A revoked token (whether from a prior rotation, a prior reuse
      // attempt, or an explicit logout) must never be usable again.
      // Presenting one is treated as a possible replay/theft signal
      // regardless of *why* it was revoked, so the whole family is revoked
      // defensively here too, per docs/12-project-architecture.md#jwt-strategy.
      await this.refreshTokensRepository.revokeFamily(record.familyId);
      throw new UnauthorizedException('Refresh token has been revoked. Please sign in again.');
    }

    if (record.expiresAt < new Date()) {
      this.logRefreshRejected('expired token', record);
      throw new UnauthorizedException('Refresh token has expired.');
    }

    const user = await this.usersService.findById(record.userId);
    if (!user) {
      this.logRefreshRejected('account no longer exists', record);
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

  /**
   * `PATCH /users/me/password`. After checking the current password, it
   * stores the new hash, revokes every refresh token of the user (all
   * sessions, this one included), and issues a fresh pair in a new family:
   * the caller swaps to it and stays signed in, every other device is
   * signed out at its next refresh. Access tokens already issued stay valid
   * until they expire (stateless JWTs, at most `JWT_ACCESS_TOKEN_TTL`).
   */
  async changePassword(userId: string, dto: ChangePasswordDto): Promise<AuthTokensDto> {
    const user = await this.usersService.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found.');
    }
    if (!(await bcrypt.compare(dto.currentPassword, user.passwordHash))) {
      throw new ForbiddenException('Current password is incorrect.');
    }
    if (dto.newPassword === dto.currentPassword) {
      throw new BadRequestException('The new password must be different from the current one.');
    }

    const updated = await this.usersService.updatePasswordHash(
      user.id,
      await this.hashPassword(dto.newPassword),
    );
    if (!updated) {
      throw new NotFoundException('User not found.');
    }
    const revoked = await this.refreshTokensRepository.revokeAllForUser(user.id);
    this.logger.log(`Password changed (user ${user.id}); ${revoked} session(s) signed out.`);
    return this.issueTokenPair(user);
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

  /**
   * Why a refresh was rejected, so a client that ends up signed out can be
   * diagnosed from the server log (the HTTP log records only successful
   * requests). Logs the token's id, family and user, never the token or its
   * hash.
   */
  private logRefreshRejected(reason: string, record?: RefreshToken): void {
    const ids = record
      ? ` (token ${record.id}, family ${record.familyId}, user ${record.userId})`
      : '';
    this.logger.warn(`Refresh rejected: ${reason}${ids}.`);
  }

  private hashPassword(password: string): Promise<string> {
    const rounds = this.configService.get('auth.bcryptSaltRounds', { infer: true });
    return bcrypt.hash(password, rounds);
  }
}
