import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { AiCapabilityService } from '../ai/capabilities/ai-capability.service';
import { ProviderRateLimitException } from '../ai/exceptions/ai.exceptions';
import { UsersService } from '../users/users.service';
import { AssistantChatService } from './assistant-chat.service';
import { ASSISTANT_CHAT } from './capabilities/assistant-chat.capability';
import {
  CHAT_DAILY_LIMIT_REACHED_CODE,
  ChatDailyLimitReachedException,
} from './exceptions/chat-daily-limit-reached.exception';
import { ChatUsageRepository } from './repositories/chat-usage.repository';

const DAILY_LIMIT = 5;

function makeService(options: { used?: number | null; timezone?: string | null } = {}) {
  const run = jest.fn().mockResolvedValue({
    provider: 'gemini',
    model: 'gemini-3.5-flash',
    result: { reply: 'Bugün önce sunum slaytlarını bitir.' },
    usage: { inputTokens: 1, outputTokens: 1, totalTokens: 2 },
    latencyMs: 10,
  });
  const reserve = jest.fn().mockResolvedValue(options.used === undefined ? 1 : options.used);
  const release = jest.fn().mockResolvedValue(undefined);
  const findById = jest
    .fn()
    .mockResolvedValue(
      options.timezone === null
        ? null
        : { id: 'user-1', timezone: options.timezone ?? 'Europe/Istanbul' },
    );

  const service = new AssistantChatService(
    { run } as unknown as AiCapabilityService,
    { findById } as unknown as UsersService,
    { reserve, release } as unknown as ChatUsageRepository,
    { get: () => DAILY_LIMIT } as unknown as ConfigService<AppConfig, true>,
  );
  return { service, run, reserve, release };
}

describe('AssistantChatService', () => {
  afterEach(() => jest.useRealTimers());

  it('returns the reply and the messages left today', async () => {
    const { service, run, reserve } = makeService({ used: 2 });

    const response = await service.sendMessage('user-1', {
      message: 'Bugün neye odaklanmalıyım?',
      history: [{ role: 'user', text: 'Merhaba' }],
    });

    expect(response).toEqual({
      reply: 'Bugün önce sunum slaytlarını bitir.',
      remainingMessages: 3,
    });
    expect(reserve).toHaveBeenCalledWith(
      'user-1',
      expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
      DAILY_LIMIT,
    );
    const [definition, userId, notes, primaryInput] = run.mock.calls[0];
    expect(definition).toBe(ASSISTANT_CHAT);
    expect(userId).toBe('user-1');
    expect(notes).toBeUndefined();
    expect(primaryInput).toContain('[user]: Merhaba');
    expect(primaryInput).toContain('<message>\nBugün neye odaklanmalıyım?\n</message>');
  });

  it('reports 0 left on the last allowed message', async () => {
    const { service } = makeService({ used: DAILY_LIMIT });

    const response = await service.sendMessage('user-1', { message: 'Son soru' });

    expect(response.remainingMessages).toBe(0);
  });

  it('rejects with CHAT_DAILY_LIMIT_REACHED (429) and calls no AI once the limit is used up', async () => {
    const { service, run, release } = makeService({ used: null });

    const error = await service.sendMessage('user-1', { message: 'Bir soru daha' }).catch((e) => e);

    expect(error).toBeInstanceOf(ChatDailyLimitReachedException);
    expect(error.getStatus()).toBe(429);
    expect(error.getResponse()).toEqual(
      expect.objectContaining({ code: CHAT_DAILY_LIMIT_REACHED_CODE }),
    );
    expect(run).not.toHaveBeenCalled();
    expect(release).not.toHaveBeenCalled();
  });

  it('gives the message back when the AI call fails, and rethrows', async () => {
    const { service, run, reserve, release } = makeService();
    run.mockRejectedValue(new ProviderRateLimitException('gemini'));

    await expect(service.sendMessage('user-1', { message: 'Soru' })).rejects.toBeInstanceOf(
      ProviderRateLimitException,
    );
    expect(release).toHaveBeenCalledWith('user-1', reserve.mock.calls[0][1]);
  });

  describe("counts the day in the user's own time zone", () => {
    // 22:30 UTC on 29 October is already 30 October in Istanbul (UTC+3).
    beforeEach(() => {
      jest.useFakeTimers({ now: new Date('2026-10-29T22:30:00Z'), doNotFake: ['nextTick'] });
    });

    it('Istanbul', async () => {
      const { service, reserve } = makeService({ timezone: 'Europe/Istanbul' });
      await service.sendMessage('user-1', { message: 'Soru' });
      expect(reserve.mock.calls[0][1]).toBe('2026-10-30');
    });

    it('New York', async () => {
      const { service, reserve } = makeService({ timezone: 'America/New_York' });
      await service.sendMessage('user-1', { message: 'Soru' });
      expect(reserve.mock.calls[0][1]).toBe('2026-10-29');
    });

    it('falls back to Istanbul when the user has no time zone', async () => {
      const { service, reserve } = makeService({ timezone: null });
      await service.sendMessage('user-1', { message: 'Soru' });
      expect(reserve.mock.calls[0][1]).toBe('2026-10-30');
    });
  });
});
