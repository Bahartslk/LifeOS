import { randomInt } from 'crypto';
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as bcrypt from 'bcrypt';
import { User } from '@prisma/client';
import { AppConfig } from '../../config/configuration';
import { MailService } from '../mail/mail.service';
import { buildPasswordResetEmail } from '../mail/templates/password-reset.template';
import { UsersService } from '../users/users.service';
import {
  InvalidResetCodeException,
  PasswordResetEmailLimitException,
} from './exceptions/password-reset.exceptions';
import { PasswordResetEmailLimiter } from './password-reset-email-limiter';
import { PasswordResetCodesRepository } from './repositories/password-reset-codes.repository';
import { RefreshTokensRepository } from './repositories/refresh-tokens.repository';

/** Compared against when there is no usable code, so a miss takes as long as a real check. */
const DUMMY_CODE_HASH = bcrypt.hashSync('000000', 10);

/**
 * Forgotten-password reset with a 6-digit emailed code (FR-AUTH-04).
 *
 * - `requestCode` answers the same way for every address. For a known one,
 *   it closes the user's open codes, stores a bcrypt hash of a new code and
 *   emails it. That work runs after the response, so the response time
 *   doesn't reveal whether the account exists either.
 * - `resetPassword` accepts the newest usable code only. Each wrong guess
 *   counts; after `PASSWORD_RESET_MAX_ATTEMPTS` the code stops working. On
 *   success the password changes, every open code closes and every session
 *   is signed out (the user signs in again with the new password).
 */
@Injectable()
export class PasswordResetService {
  private readonly logger = new Logger(PasswordResetService.name);

  constructor(
    private readonly usersService: UsersService,
    private readonly codesRepository: PasswordResetCodesRepository,
    private readonly refreshTokensRepository: RefreshTokensRepository,
    private readonly emailLimiter: PasswordResetEmailLimiter,
    private readonly mailService: MailService,
    private readonly config: ConfigService<AppConfig, true>,
  ) {}

  /** Returns as soon as the request is accepted; see `issueCode` for the rest. */
  requestCode(email: string): { success: true } {
    if (!this.emailLimiter.tryConsume(email)) {
      throw new PasswordResetEmailLimitException();
    }
    void this.issueCode(email).catch((error: unknown) => {
      this.logger.error(
        `Password reset code could not be issued: ${error instanceof Error ? error.message : 'unknown error'}`,
      );
    });
    return { success: true };
  }

  /** Creates and emails a code if `email` belongs to an account; does nothing otherwise. */
  async issueCode(email: string): Promise<void> {
    const user = await this.usersService.findByEmail(email);
    if (!user) return;

    const { codeTtlMinutes } = this.settings();
    const code = randomInt(0, 1_000_000).toString().padStart(6, '0');
    const now = new Date();
    await this.codesRepository.closeAllForUser(user.id, now);
    await this.codesRepository.create({
      userId: user.id,
      codeHash: await bcrypt.hash(code, this.bcryptRounds()),
      expiresAt: new Date(now.getTime() + codeTtlMinutes * 60_000),
    });
    await this.mailService.send(
      buildPasswordResetEmail({
        email: user.email,
        displayName: user.displayName,
        code,
        validMinutes: codeTtlMinutes,
      }),
    );
    this.logger.log(`Password reset code sent (user ${user.id}).`);
  }

  async resetPassword(
    email: string,
    code: string,
    newPassword: string,
  ): Promise<{ success: true }> {
    const { maxAttempts } = this.settings();
    const now = new Date();
    const user = await this.usersService.findByEmail(email);
    const record = user ? await this.codesRepository.findUsable(user.id, maxAttempts, now) : null;
    const matches = await bcrypt.compare(code, record?.codeHash ?? DUMMY_CODE_HASH);

    if (!user || !record || !matches) {
      if (record) await this.codesRepository.recordFailedAttempt(record.id);
      throw new InvalidResetCodeException();
    }

    await this.completeReset(user, newPassword, now);
    return { success: true };
  }

  private async completeReset(user: User, newPassword: string, now: Date): Promise<void> {
    // Closing the codes first means a code can't be used twice, even by two requests at once.
    await this.codesRepository.closeAllForUser(user.id, now);
    const updated = await this.usersService.updatePasswordHash(
      user.id,
      await bcrypt.hash(newPassword, this.bcryptRounds()),
    );
    if (!updated) throw new InvalidResetCodeException();
    const revoked = await this.refreshTokensRepository.revokeAllForUser(user.id);
    this.logger.log(`Password reset (user ${user.id}); ${revoked} session(s) signed out.`);
  }

  private settings() {
    return this.config.get('passwordReset', { infer: true });
  }

  private bcryptRounds(): number {
    return this.config.get('auth.bcryptSaltRounds', { infer: true });
  }
}
