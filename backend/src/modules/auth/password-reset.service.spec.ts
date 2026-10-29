import { randomUUID } from 'crypto';
import { Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as bcrypt from 'bcrypt';
import { PasswordResetCode, ThemeMode, User, UserRole } from '@prisma/client';
import { AppConfig } from '../../config/configuration';
import { MailMessage, MailService } from '../mail/mail.service';
import { UsersService } from '../users/users.service';
import {
  InvalidResetCodeException,
  PasswordResetEmailLimitException,
} from './exceptions/password-reset.exceptions';
import { PasswordResetEmailLimiter } from './password-reset-email-limiter';
import { PasswordResetService } from './password-reset.service';
import { PasswordResetCodesRepository } from './repositories/password-reset-codes.repository';
import { RefreshTokensRepository } from './repositories/refresh-tokens.repository';

const user: User = {
  id: randomUUID(),
  email: 'ada@example.com',
  passwordHash: 'old-hash',
  displayName: 'Ada',
  avatarUrl: null,
  bio: null,
  timezone: 'Europe/Istanbul',
  language: 'tr',
  themePreference: ThemeMode.SYSTEM,
  notificationPreferences: {},
  role: UserRole.USER,
  createdAt: new Date(),
  updatedAt: new Date(),
  deletedAt: null,
};

function storedCode(code: string, overrides: Partial<PasswordResetCode> = {}): PasswordResetCode {
  return {
    id: randomUUID(),
    userId: user.id,
    codeHash: bcrypt.hashSync(code, 4),
    expiresAt: new Date(Date.now() + 60_000),
    attempts: 0,
    usedAt: null,
    createdAt: new Date(),
    ...overrides,
  };
}

describe('PasswordResetService', () => {
  let usersService: jest.Mocked<Pick<UsersService, 'findByEmail' | 'updatePasswordHash'>>;
  let codes: jest.Mocked<
    Pick<
      PasswordResetCodesRepository,
      'create' | 'findUsable' | 'recordFailedAttempt' | 'closeAllForUser'
    >
  >;
  let refreshTokens: jest.Mocked<Pick<RefreshTokensRepository, 'revokeAllForUser'>>;
  let limiter: jest.Mocked<Pick<PasswordResetEmailLimiter, 'tryConsume'>>;
  let mail: { send: jest.Mock<Promise<void>, [MailMessage]> };
  let service: PasswordResetService;

  beforeEach(() => {
    usersService = {
      findByEmail: jest.fn().mockResolvedValue(user),
      updatePasswordHash: jest.fn().mockResolvedValue(true),
    };
    codes = {
      create: jest.fn().mockResolvedValue({}),
      findUsable: jest.fn().mockResolvedValue(null),
      recordFailedAttempt: jest.fn().mockResolvedValue(1),
      closeAllForUser: jest.fn().mockResolvedValue(undefined),
    };
    refreshTokens = { revokeAllForUser: jest.fn().mockResolvedValue(2) };
    limiter = { tryConsume: jest.fn().mockReturnValue(true) };
    mail = { send: jest.fn().mockResolvedValue(undefined) };
    const values: Record<string, unknown> = {
      passwordReset: { codeTtlMinutes: 15, maxAttempts: 5, emailHourlyLimit: 3 },
      'auth.bcryptSaltRounds': 4,
    };
    const config = { get: jest.fn((key: string) => values[key]) } as unknown as ConfigService<
      AppConfig,
      true
    >;
    service = new PasswordResetService(
      usersService as unknown as UsersService,
      codes as unknown as PasswordResetCodesRepository,
      refreshTokens as unknown as RefreshTokensRepository,
      limiter as unknown as PasswordResetEmailLimiter,
      mail as unknown as MailService,
      config,
    );
    jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined);
    jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => jest.restoreAllMocks());

  describe('issueCode', () => {
    it('stores only a hash of a new 6-digit code, valid for 15 minutes, and emails the code', async () => {
      const before = Date.now();
      await service.issueCode('ADA@example.com');

      expect(codes.closeAllForUser).toHaveBeenCalledWith(user.id, expect.any(Date));
      const [{ codeHash, expiresAt, userId }] = codes.create.mock.calls[0];
      expect(userId).toBe(user.id);
      const message = mail.send.mock.calls[0][0];
      const code = /\b(\d{6})\b/.exec(message.text)![1];
      expect(codeHash).not.toContain(code);
      await expect(bcrypt.compare(code, codeHash)).resolves.toBe(true);
      expect(expiresAt.getTime() - before).toBeGreaterThanOrEqual(15 * 60_000 - 1000);
      expect(expiresAt.getTime() - before).toBeLessThanOrEqual(15 * 60_000 + 1000);
      expect(message.to.email).toBe(user.email);
      expect(message.subject).toContain(code);
    });

    it('does nothing for an unknown address', async () => {
      usersService.findByEmail.mockResolvedValue(null);

      await service.issueCode('nobody@example.com');

      expect(codes.create).not.toHaveBeenCalled();
      expect(mail.send).not.toHaveBeenCalled();
    });
  });

  describe('requestCode', () => {
    it('answers the same for every address and refuses an address over its hourly limit', () => {
      expect(service.requestCode('ada@example.com')).toEqual({ success: true });

      limiter.tryConsume.mockReturnValue(false);
      expect(() => service.requestCode('ada@example.com')).toThrow(
        PasswordResetEmailLimitException,
      );
    });

    it('still answers success when sending fails, and logs only the error message', async () => {
      mail.send.mockRejectedValue(new Error('Brevo rejected the email with status 401.'));

      expect(service.requestCode('ada@example.com')).toEqual({ success: true });
      // The code is issued after the response; wait for that background work to finish.
      for (let i = 0; i < 100 && !jest.mocked(Logger.prototype.error).mock.calls.length; i++) {
        await new Promise((resolve) => setTimeout(resolve, 10));
      }

      expect(Logger.prototype.error).toHaveBeenCalledWith(expect.stringContaining('status 401'));
    });
  });

  describe('resetPassword', () => {
    it('with the right code: closes the codes, stores the new password and signs out every session', async () => {
      codes.findUsable.mockResolvedValue(storedCode('123456'));

      await expect(service.resetPassword(user.email, '123456', 'new-password')).resolves.toEqual({
        success: true,
      });

      expect(codes.closeAllForUser).toHaveBeenCalledWith(user.id, expect.any(Date));
      const [, hash] = usersService.updatePasswordHash.mock.calls[0];
      await expect(bcrypt.compare('new-password', hash)).resolves.toBe(true);
      expect(refreshTokens.revokeAllForUser).toHaveBeenCalledWith(user.id);
    });

    it('with a wrong code: counts the attempt and changes nothing', async () => {
      const record = storedCode('123456');
      codes.findUsable.mockResolvedValue(record);

      await expect(service.resetPassword(user.email, '654321', 'new-password')).rejects.toThrow(
        InvalidResetCodeException,
      );

      expect(codes.recordFailedAttempt).toHaveBeenCalledWith(record.id);
      expect(usersService.updatePasswordHash).not.toHaveBeenCalled();
      expect(refreshTokens.revokeAllForUser).not.toHaveBeenCalled();
    });

    it('asks the repository only for usable codes (not used, not expired, under the attempt limit)', async () => {
      await expect(service.resetPassword(user.email, '123456', 'new-password')).rejects.toThrow(
        InvalidResetCodeException,
      );

      expect(codes.findUsable).toHaveBeenCalledWith(user.id, 5, expect.any(Date));
      expect(codes.recordFailedAttempt).not.toHaveBeenCalled();
    });

    it('gives the same answer for an unknown address', async () => {
      usersService.findByEmail.mockResolvedValue(null);

      await expect(
        service.resetPassword('nobody@example.com', '123456', 'new-password'),
      ).rejects.toThrow(InvalidResetCodeException);
      expect(codes.findUsable).not.toHaveBeenCalled();
    });
  });
});
