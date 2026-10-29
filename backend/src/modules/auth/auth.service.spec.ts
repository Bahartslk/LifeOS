import { createHash, randomUUID } from 'crypto';
import { ConflictException, Logger, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { RefreshToken, ThemeMode, User, UserRole } from '@prisma/client';
import { AppConfig } from '../../config/configuration';
import { UsersService } from '../users/users.service';
import { AuthService } from './auth.service';
import { RefreshTokensRepository } from './repositories/refresh-tokens.repository';

// The CommonJS module object itself, from the same module registry the service
// loads it from: `AuthService`'s `import * as bcrypt` reads its functions
// through this object, so a spy installed here is the one the service calls.
const bcryptModule = jest.requireActual<typeof import('bcrypt')>('bcrypt');

/**
 * Pins down `AuthService`'s CURRENT behavior, before F2 changes any of it.
 * Every collaborator is a mock; no database or real JWT signing is involved.
 * Password hashing uses real bcrypt with a low cost factor.
 */

const DAY_MS = 24 * 60 * 60 * 1000;
const TEST_BCRYPT_ROUNDS = 4;
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

const sha256 = (value: string): string => createHash('sha256').update(value).digest('hex');

function makeUser(overrides: Partial<User> = {}): User {
  return {
    id: randomUUID(),
    email: 'ada@example.com',
    passwordHash: bcryptModule.hashSync('correct-password', TEST_BCRYPT_ROUNDS),
    displayName: 'Ada',
    avatarUrl: null,
    bio: null,
    timezone: 'Europe/Istanbul',
    language: 'tr',
    themePreference: ThemeMode.SYSTEM,
    notificationPreferences: { taskReminders: true, tripReminders: true, aiInsights: true },
    role: UserRole.USER,
    createdAt: new Date('2026-10-01T10:00:00.000Z'),
    updatedAt: new Date('2026-10-01T10:00:00.000Z'),
    deletedAt: null,
    ...overrides,
  };
}

function makeStoredToken(overrides: Partial<RefreshToken> = {}): RefreshToken {
  return {
    id: randomUUID(),
    userId: randomUUID(),
    tokenHash: sha256('presented-refresh-token'),
    familyId: randomUUID(),
    expiresAt: new Date(Date.now() + DAY_MS),
    revokedAt: null,
    createdAt: new Date(),
    ...overrides,
  };
}

describe('AuthService', () => {
  let usersService: jest.Mocked<
    Pick<UsersService, 'findByEmail' | 'findById' | 'create' | 'toPublicUser'>
  >;
  let refreshTokens: jest.Mocked<
    Pick<RefreshTokensRepository, 'create' | 'findByTokenHash' | 'revokeById' | 'revokeFamily'>
  >;
  let jwtService: jest.Mocked<Pick<JwtService, 'signAsync'>>;
  let configValues: Record<string, unknown>;
  let service: AuthService;

  beforeEach(() => {
    usersService = {
      findByEmail: jest.fn().mockResolvedValue(null),
      findById: jest.fn().mockResolvedValue(null),
      create: jest.fn(),
      toPublicUser: jest.fn(),
    };
    refreshTokens = {
      create: jest.fn().mockResolvedValue({}),
      findByTokenHash: jest.fn().mockResolvedValue(null),
      revokeById: jest.fn().mockResolvedValue(undefined),
      revokeFamily: jest.fn().mockResolvedValue(undefined),
    };
    jwtService = { signAsync: jest.fn().mockResolvedValue('signed-access-token') };
    configValues = {
      'jwt.refreshTokenTtl': '30d',
      'auth.bcryptSaltRounds': TEST_BCRYPT_ROUNDS,
    };
    const configService = {
      get: jest.fn((key: string) => configValues[key]),
    } as unknown as ConfigService<AppConfig, true>;

    service = new AuthService(
      usersService as unknown as UsersService,
      refreshTokens as unknown as RefreshTokensRepository,
      jwtService as unknown as JwtService,
      configService,
    );
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  /** The single object passed to `RefreshTokensRepository.create` in this test. */
  function storedTokenData() {
    expect(refreshTokens.create).toHaveBeenCalledTimes(1);
    return refreshTokens.create.mock.calls[0][0];
  }

  describe('register', () => {
    const dto = { email: 'new@example.com', password: 'a-strong-password', displayName: 'New' };

    it('creates the user with a bcrypt hash of the password and only email, hash and display name', async () => {
      usersService.create.mockImplementation(async (data) => makeUser({ ...data }));

      const tokens = await service.register(dto);

      expect(usersService.findByEmail).toHaveBeenCalledWith(dto.email);
      expect(usersService.create).toHaveBeenCalledTimes(1);
      const created = usersService.create.mock.calls[0][0];
      expect(Object.keys(created).sort()).toEqual(['displayName', 'email', 'passwordHash']);
      expect(created.email).toBe(dto.email);
      expect(created.displayName).toBe(dto.displayName);
      expect(created.passwordHash).not.toBe(dto.password);
      expect(await bcryptModule.compare(dto.password, created.passwordHash)).toBe(true);
      expect(tokens).toEqual({
        accessToken: 'signed-access-token',
        refreshToken: expect.any(String),
      });
    });

    it('stores only the SHA-256 hash of a new refresh token, in a new family, expiring after the configured TTL', async () => {
      usersService.create.mockImplementation(async (data) => makeUser({ ...data }));
      const before = Date.now();

      const { refreshToken } = await service.register(dto);

      const after = Date.now();
      const stored = storedTokenData();
      expect(refreshToken).toMatch(/^[0-9a-f]{80}$/);
      expect(stored.tokenHash).toBe(sha256(refreshToken));
      expect(stored.tokenHash).not.toBe(refreshToken);
      expect(stored.familyId).toMatch(UUID_PATTERN);
      expect(stored.expiresAt.getTime()).toBeGreaterThanOrEqual(before + 30 * DAY_MS);
      expect(stored.expiresAt.getTime()).toBeLessThanOrEqual(after + 30 * DAY_MS);
    });

    it('rejects an email that is already registered without creating anything', async () => {
      usersService.findByEmail.mockResolvedValue(makeUser({ email: dto.email }));

      await expect(service.register(dto)).rejects.toThrow(ConflictException);

      expect(usersService.create).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
      expect(jwtService.signAsync).not.toHaveBeenCalled();
    });

    it('fails when the configured refresh token TTL cannot be parsed', async () => {
      usersService.create.mockImplementation(async (data) => makeUser({ ...data }));
      configValues['jwt.refreshTokenTtl'] = 'abc';

      await expect(service.register(dto)).rejects.toThrow('Invalid duration format');

      expect(refreshTokens.create).not.toHaveBeenCalled();
    });
  });

  describe('login', () => {
    it('returns a token pair and the public user, signing sub, email and role', async () => {
      const user = makeUser({ role: UserRole.ADMIN });
      const publicUser = { id: user.id, email: user.email } as ReturnType<
        UsersService['toPublicUser']
      >;
      usersService.findByEmail.mockResolvedValue(user);
      usersService.toPublicUser.mockReturnValue(publicUser);

      const result = await service.login({ email: user.email, password: 'correct-password' });

      expect(jwtService.signAsync).toHaveBeenCalledWith({
        sub: user.id,
        email: user.email,
        role: UserRole.ADMIN,
      });
      expect(usersService.toPublicUser).toHaveBeenCalledWith(user);
      expect(result).toEqual({
        accessToken: 'signed-access-token',
        refreshToken: expect.any(String),
        user: publicUser,
      });
      const stored = storedTokenData();
      expect(stored.userId).toBe(user.id);
      expect(stored.tokenHash).toBe(sha256(result.refreshToken));
      expect(stored.familyId).toMatch(UUID_PATTERN);
    });

    it('starts a new token family and a different refresh token for every sign-in', async () => {
      const user = makeUser();
      usersService.findByEmail.mockResolvedValue(user);

      const first = await service.login({ email: user.email, password: 'correct-password' });
      const second = await service.login({ email: user.email, password: 'correct-password' });

      expect(refreshTokens.create).toHaveBeenCalledTimes(2);
      const [firstStored, secondStored] = refreshTokens.create.mock.calls.map(([data]) => data);
      expect(first.refreshToken).not.toBe(second.refreshToken);
      expect(firstStored.familyId).not.toBe(secondStored.familyId);
    });

    it('rejects a wrong password with the generic message and issues no tokens', async () => {
      const user = makeUser();
      usersService.findByEmail.mockResolvedValue(user);

      await expect(
        service.login({ email: user.email, password: 'wrong-password' }),
      ).rejects.toThrow(new UnauthorizedException('Invalid email or password.'));

      expect(jwtService.signAsync).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
    });

    it('rejects an unknown email with the same message and still runs exactly one bcrypt comparison', async () => {
      const compareSpy = jest.spyOn(bcryptModule, 'compare');
      usersService.findByEmail.mockResolvedValue(null);

      await expect(
        service.login({ email: 'nobody@example.com', password: 'any-password' }),
      ).rejects.toThrow(new UnauthorizedException('Invalid email or password.'));

      expect(compareSpy).toHaveBeenCalledTimes(1);
      expect(compareSpy).toHaveBeenCalledWith('any-password', expect.stringMatching(/^\$2[aby]\$/));
      expect(jwtService.signAsync).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
    });
  });

  describe('refresh', () => {
    const presented = 'presented-refresh-token';

    it('looks the token up by its SHA-256 hash and rejects an unknown token without side effects', async () => {
      await expect(service.refresh({ refreshToken: presented })).rejects.toThrow(
        new UnauthorizedException('Invalid refresh token.'),
      );

      expect(refreshTokens.findByTokenHash).toHaveBeenCalledWith(sha256(presented));
      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
    });

    it('revokes the whole family when a revoked token is presented again', async () => {
      const record = makeStoredToken({ revokedAt: new Date(Date.now() - 1000) });
      refreshTokens.findByTokenHash.mockResolvedValue(record);

      await expect(service.refresh({ refreshToken: presented })).rejects.toThrow(
        new UnauthorizedException('Refresh token has been revoked. Please sign in again.'),
      );

      expect(refreshTokens.revokeFamily).toHaveBeenCalledWith(record.familyId);
      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
    });

    it('treats a token that is both revoked and expired as reuse (revocation is checked first)', async () => {
      const record = makeStoredToken({
        revokedAt: new Date(Date.now() - 2 * DAY_MS),
        expiresAt: new Date(Date.now() - DAY_MS),
      });
      refreshTokens.findByTokenHash.mockResolvedValue(record);

      await expect(service.refresh({ refreshToken: presented })).rejects.toThrow(
        'Refresh token has been revoked. Please sign in again.',
      );

      expect(refreshTokens.revokeFamily).toHaveBeenCalledWith(record.familyId);
    });

    // Documents current behavior only: an expired (not revoked) token is
    // rejected without revoking the token or its family.
    it('rejects an expired token and, in the current implementation, revokes nothing', async () => {
      refreshTokens.findByTokenHash.mockResolvedValue(
        makeStoredToken({ expiresAt: new Date(Date.now() - 1000) }),
      );

      await expect(service.refresh({ refreshToken: presented })).rejects.toThrow(
        new UnauthorizedException('Refresh token has expired.'),
      );

      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
    });

    // Documents current behavior only: when the account no longer exists the
    // request is rejected, and the presented token is left as it is.
    it('rejects a token whose account no longer exists and, in the current implementation, revokes nothing', async () => {
      const record = makeStoredToken();
      refreshTokens.findByTokenHash.mockResolvedValue(record);
      usersService.findById.mockResolvedValue(null);

      await expect(service.refresh({ refreshToken: presented })).rejects.toThrow(
        new UnauthorizedException('Account no longer exists.'),
      );

      expect(usersService.findById).toHaveBeenCalledWith(record.userId);
      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
      expect(refreshTokens.create).not.toHaveBeenCalled();
    });

    it('rotates a valid token: revokes it first, then issues a new pair in the same family', async () => {
      const user = makeUser();
      const record = makeStoredToken({ userId: user.id });
      refreshTokens.findByTokenHash.mockResolvedValue(record);
      usersService.findById.mockResolvedValue(user);

      const tokens = await service.refresh({ refreshToken: presented });

      expect(refreshTokens.revokeById).toHaveBeenCalledWith(record.id);
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
      const stored = storedTokenData();
      expect(refreshTokens.revokeById.mock.invocationCallOrder[0]).toBeLessThan(
        refreshTokens.create.mock.invocationCallOrder[0],
      );
      expect(stored.familyId).toBe(record.familyId);
      expect(stored.userId).toBe(user.id);
      expect(stored.tokenHash).toBe(sha256(tokens.refreshToken));
      expect(tokens.refreshToken).not.toBe(presented);
      expect(tokens.accessToken).toBe('signed-access-token');
      expect(jwtService.signAsync).toHaveBeenCalledWith({
        sub: user.id,
        email: user.email,
        role: user.role,
      });
    });

    it('uses the configured TTL for the rotated token', async () => {
      const user = makeUser();
      refreshTokens.findByTokenHash.mockResolvedValue(makeStoredToken({ userId: user.id }));
      usersService.findById.mockResolvedValue(user);
      configValues['jwt.refreshTokenTtl'] = '15m';
      const before = Date.now();

      await service.refresh({ refreshToken: presented });

      const after = Date.now();
      const { expiresAt } = storedTokenData();
      expect(expiresAt.getTime()).toBeGreaterThanOrEqual(before + 15 * 60 * 1000);
      expect(expiresAt.getTime()).toBeLessThanOrEqual(after + 15 * 60 * 1000);
    });

    describe('logs why a refresh was rejected, never the token', () => {
      let warn: jest.SpyInstance;

      beforeEach(() => {
        warn = jest.spyOn(Logger.prototype, 'warn').mockImplementation(() => undefined);
      });

      function loggedLine(): string {
        expect(warn).toHaveBeenCalledTimes(1);
        const [line] = warn.mock.calls[0] as [string];
        expect(line).not.toContain(presented);
        expect(line).not.toContain(sha256(presented));
        return line;
      }

      it('unknown token', async () => {
        await expect(service.refresh({ refreshToken: presented })).rejects.toThrow();

        expect(loggedLine()).toBe('Refresh rejected: unknown token.');
      });

      it.each([
        [
          'a revoked token',
          { revokedAt: new Date(Date.now() - 1000) },
          'revoked token presented again',
        ],
        ['an expired token', { expiresAt: new Date(Date.now() - 1000) }, 'expired token'],
      ])('%s, with the token id, family and user', async (_label, overrides, reason) => {
        const record = makeStoredToken(overrides);
        refreshTokens.findByTokenHash.mockResolvedValue(record);

        await expect(service.refresh({ refreshToken: presented })).rejects.toThrow();

        const line = loggedLine();
        expect(line).toContain(reason);
        expect(line).toContain(
          `(token ${record.id}, family ${record.familyId}, user ${record.userId})`,
        );
      });

      it('an account that no longer exists', async () => {
        refreshTokens.findByTokenHash.mockResolvedValue(makeStoredToken());
        usersService.findById.mockResolvedValue(null);

        await expect(service.refresh({ refreshToken: presented })).rejects.toThrow();

        expect(loggedLine()).toContain('account no longer exists');
      });

      it('nothing for a successful refresh', async () => {
        const user = makeUser();
        refreshTokens.findByTokenHash.mockResolvedValue(makeStoredToken({ userId: user.id }));
        usersService.findById.mockResolvedValue(user);

        await service.refresh({ refreshToken: presented });

        expect(warn).not.toHaveBeenCalled();
      });
    });
  });

  describe('logout', () => {
    const presented = 'presented-refresh-token';
    const callerId = randomUUID();

    it("revokes the caller's own active token", async () => {
      const record = makeStoredToken({ userId: callerId });
      refreshTokens.findByTokenHash.mockResolvedValue(record);

      await expect(service.logout({ refreshToken: presented }, callerId)).resolves.toEqual({
        success: true,
      });

      expect(refreshTokens.findByTokenHash).toHaveBeenCalledWith(sha256(presented));
      expect(refreshTokens.revokeById).toHaveBeenCalledWith(record.id);
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
    });

    // Relies on `logout`'s current `currentUserId` parameter; update together
    // with any change to how logout identifies the token's owner.
    it("leaves another user's token alone and still reports success", async () => {
      refreshTokens.findByTokenHash.mockResolvedValue(makeStoredToken({ userId: randomUUID() }));

      await expect(service.logout({ refreshToken: presented }, callerId)).resolves.toEqual({
        success: true,
      });

      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
    });

    it('does not revoke an already revoked token again and still reports success', async () => {
      refreshTokens.findByTokenHash.mockResolvedValue(
        makeStoredToken({ userId: callerId, revokedAt: new Date(Date.now() - 1000) }),
      );

      await expect(service.logout({ refreshToken: presented }, callerId)).resolves.toEqual({
        success: true,
      });

      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
    });

    it('reports success for an unknown token without revoking anything', async () => {
      await expect(service.logout({ refreshToken: presented }, callerId)).resolves.toEqual({
        success: true,
      });

      expect(refreshTokens.revokeById).not.toHaveBeenCalled();
      expect(refreshTokens.revokeFamily).not.toHaveBeenCalled();
    });
  });
});
