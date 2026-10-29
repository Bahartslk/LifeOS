import { ForbiddenException, Logger, NotFoundException } from '@nestjs/common';
import { User } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { UsersRepository } from './repositories/users.repository';
import { UsersService } from './users.service';

// Real bcrypt with a low cost factor, so the password check itself is exercised.
const PASSWORD = 'Correct-horse-1';
const PASSWORD_HASH = bcrypt.hashSync(PASSWORD, 4);

function makeService(user: Partial<User> | null) {
  const repository = {
    findById: jest
      .fn()
      .mockResolvedValue(
        user === null ? null : { id: 'user-1', passwordHash: PASSWORD_HASH, ...user },
      ),
    deleteById: jest.fn().mockResolvedValue(undefined),
  };
  return { service: new UsersService(repository as unknown as UsersRepository), repository };
}

describe('UsersService.deleteAccount', () => {
  beforeEach(() => {
    jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined);
  });

  afterEach(() => jest.restoreAllMocks());

  it('hard-deletes the account once the password is confirmed', async () => {
    const { service, repository } = makeService({});

    await expect(service.deleteAccount('user-1', PASSWORD)).resolves.toEqual({ success: true });

    expect(repository.findById).toHaveBeenCalledWith('user-1');
    expect(repository.deleteById).toHaveBeenCalledWith('user-1');
  });

  it('rejects a wrong password with 403 and deletes nothing', async () => {
    const { service, repository } = makeService({});

    const error = await service.deleteAccount('user-1', 'wrong-password').catch((e) => e);

    expect(error).toBeInstanceOf(ForbiddenException);
    expect(error.message).toBe('Password is incorrect.');
    expect(repository.deleteById).not.toHaveBeenCalled();
  });

  it('answers 404 when the account no longer exists', async () => {
    const { service, repository } = makeService(null);

    await expect(service.deleteAccount('user-1', PASSWORD)).rejects.toBeInstanceOf(
      NotFoundException,
    );
    expect(repository.deleteById).not.toHaveBeenCalled();
  });

  it('logs the deleted user id, never the password', async () => {
    const log = jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined);
    const { service } = makeService({});

    await service.deleteAccount('user-1', PASSWORD);

    expect(log).toHaveBeenCalledWith('Account deleted (user user-1).');
    expect(JSON.stringify(log.mock.calls)).not.toContain(PASSWORD);
  });
});
