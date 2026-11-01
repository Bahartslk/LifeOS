import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { AppConfig } from '../../config/configuration';
import { JwtStrategy } from '../../common/strategies/jwt.strategy';
import { MailModule } from '../mail/mail.module';
import { UsersModule } from '../users/users.module';
import { PasswordResetEmailLimiter } from './password-reset-email-limiter';
import { PasswordResetService } from './password-reset.service';
import { PasswordResetCodesRepository } from './repositories/password-reset-codes.repository';
import { AccountPasswordController } from './account-password.controller';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { RefreshTokensRepository } from './repositories/refresh-tokens.repository';

/**
 * Sprint 14.0 (Authentication Backend): register/login/refresh/logout, per
 * docs/15-api-design.md#authentication. Owns Passport/JWT (RS256)
 * configuration (unchanged from Sprint 13.0) plus this sprint's
 * `AuthController`/`AuthService`/`RefreshTokensRepository`. Imports
 * `UsersModule` for user lookup/creation — `AuthService` never touches
 * `PrismaService` directly.
 */
@Module({
  imports: [
    PassportModule.register({ defaultStrategy: 'jwt' }),
    JwtModule.registerAsync({
      global: true,
      inject: [ConfigService],
      useFactory: (configService: ConfigService<AppConfig, true>) => ({
        privateKey: configService.get('jwt.accessTokenPrivateKey', { infer: true }),
        publicKey: configService.get('jwt.accessTokenPublicKey', { infer: true }),
        signOptions: {
          algorithm: 'RS256',
          expiresIn: configService.get('jwt.accessTokenTtl', { infer: true }),
        },
      }),
    }),
    UsersModule,
    MailModule,
  ],
  controllers: [AuthController, AccountPasswordController],
  providers: [
    JwtStrategy,
    AuthService,
    RefreshTokensRepository,
    PasswordResetService,
    PasswordResetCodesRepository,
    PasswordResetEmailLimiter,
  ],
  exports: [JwtModule, PassportModule],
})
export class AuthModule {}
