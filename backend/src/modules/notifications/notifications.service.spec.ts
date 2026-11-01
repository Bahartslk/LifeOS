import { Logger } from '@nestjs/common';
import { DevicePlatform } from '@prisma/client';
import { NotificationsService } from './notifications.service';
import { PushMessage, PushSender } from './push/push-sender';
import { DeviceTokensRepository } from './repositories/device-tokens.repository';

const MESSAGE: PushMessage = { title: 'T', body: 'B', channelId: 'reminders' };

describe('NotificationsService', () => {
  let repository: jest.Mocked<
    Pick<
      DeviceTokensRepository,
      'upsert' | 'findTokensForUser' | 'deleteTokens' | 'deleteAllForUser'
    >
  >;
  let sender: { send: jest.Mock };
  let service: NotificationsService;

  beforeEach(() => {
    repository = {
      upsert: jest.fn().mockResolvedValue(undefined),
      findTokensForUser: jest.fn().mockResolvedValue(['a', 'b']),
      deleteTokens: jest.fn().mockResolvedValue(1),
      deleteAllForUser: jest.fn().mockResolvedValue(2),
    };
    sender = { send: jest.fn().mockResolvedValue({ sent: 2, invalidTokens: [], failed: 0 }) };
    service = new NotificationsService(
      repository as unknown as DeviceTokensRepository,
      sender as unknown as PushSender,
    );
    jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined);
    jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => jest.restoreAllMocks());

  it('registers a device for the user', async () => {
    await service.registerDevice('user-1', 'token-1', DevicePlatform.ANDROID);

    expect(repository.upsert).toHaveBeenCalledWith(
      'user-1',
      'token-1',
      DevicePlatform.ANDROID,
      expect.any(Date),
    );
  });

  it('sends to every device of the user', async () => {
    const result = await service.sendToUser('user-1', MESSAGE);

    expect(sender.send).toHaveBeenCalledWith(['a', 'b'], MESSAGE);
    expect(result.sent).toBe(2);
    expect(repository.deleteTokens).not.toHaveBeenCalled();
  });

  it('deletes the tokens FCM reports as dead', async () => {
    sender.send.mockResolvedValue({ sent: 1, invalidTokens: ['b'], failed: 0 });

    await service.sendToUser('user-1', MESSAGE);

    expect(repository.deleteTokens).toHaveBeenCalledWith(['b']);
  });

  it('does not call the sender for a user without devices', async () => {
    repository.findTokensForUser.mockResolvedValue([]);

    expect(await service.sendToUser('user-1', MESSAGE)).toEqual({
      sent: 0,
      invalidTokens: [],
      failed: 0,
    });
    expect(sender.send).not.toHaveBeenCalled();
  });

  it('never throws when sending fails, so the caller carries on', async () => {
    sender.send.mockRejectedValue(new Error('Google rejected the FCM access token request'));

    await expect(service.sendToUser('user-1', MESSAGE)).resolves.toEqual({
      sent: 0,
      invalidTokens: [],
      failed: 2,
    });
  });

  it('forgets all devices of a user', async () => {
    await service.forgetAllDevices('user-1');

    expect(repository.deleteAllForUser).toHaveBeenCalledWith('user-1');
  });
});
