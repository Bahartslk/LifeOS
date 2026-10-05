import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import { UserRole } from '@prisma/client';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { AppConfig } from '../../config/configuration';

/**
 * Validates the cryptographic signature and expiry of an access token using
 * the RS256 public key, per docs/12-project-architecture.md#jwt-strategy.
 *
 * This strategy deliberately does not look up a user record — it only
 * proves the token is authentic and returns its decoded claims. Whether the
 * subject still exists, is active, etc. is the concern of Authentication's
 * own service layer (`AuthService`/`UsersService`), not this shared
 * infrastructure.
 *
 * No `tokenVersion` claim: revoking access ahead of natural expiry is
 * already handled at the refresh-token layer (rotation + family revocation
 * on reuse, per `modules/auth/repositories/refresh-tokens.repository.ts`).
 * Access tokens are short-lived (15 min default) and self-expire; a global
 * per-user version counter would duplicate that same revocation guarantee
 * for no added benefit this sprint — avoided per CLAUDE.md's "do not
 * over-engineer" and "avoid premature abstractions" rules.
 *
 * `role` is the account's role at the moment the token was issued — a
 * convenience for clients (e.g. the admin panel deciding what to render),
 * NOT an authorization source: `RolesGuard` always re-reads the current
 * role from the database. Tokens issued before this claim existed simply
 * don't carry it, hence optional.
 */
export interface JwtPayload {
  sub: string; // user id
  email: string;
  role?: UserRole;
  iat: number;
  exp: number;
}

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(configService: ConfigService<AppConfig, true>) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get('jwt.accessTokenPublicKey', { infer: true }),
      algorithms: ['RS256'],
    });
  }

  validate(payload: JwtPayload): JwtPayload {
    return payload;
  }
}
