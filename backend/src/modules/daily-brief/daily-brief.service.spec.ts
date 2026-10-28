import { AiCapabilityService } from '../ai/capabilities/ai-capability.service';
import { InvalidProviderResponseException } from '../ai/exceptions/ai.exceptions';
import { AiService } from '../ai/service/ai.service';
import { DailyBriefService } from './daily-brief.service';

const SUMMARY = {
  todayPriorities: ['Staj sunumu slaytlarını bitir'],
  keyEvents: ['Proje toplantısı'],
  suggestedFocus: 'Sunuma odaklan.',
};
const PRIORITIES = [
  { taskTitle: 'Staj sunumu slaytlarını bitir', suggestedRank: 1, reason: 'Yüksek öncelikli.' },
  { taskTitle: '30 dakika yürüyüş', suggestedRank: 2, reason: 'Düşük öncelikli.' },
];
const MOTIVATION = { message: 'Bugün sunumunu bitirince yürüyüşü hak edeceksin!' };

function makeService(aiText: string) {
  const generate = jest.fn().mockResolvedValue({
    text: aiText,
    providerId: 'gemini',
    model: 'gemini-2.5-flash',
    usage: { estimatedInputTokens: 400, estimatedOutputTokens: 200 },
    latencyMs: 1000,
    retryCount: 0,
    fallbackCount: 0,
    templateId: 'assistant',
    templateVersion: '1',
  });
  const aiService = { generate } as unknown as AiService;
  return { service: new DailyBriefService(new AiCapabilityService(aiService)), generate };
}

describe('DailyBriefService.getDailyOverview', () => {
  it('returns all three parts from a single AI call', async () => {
    const { service, generate } = makeService(
      JSON.stringify({ summary: SUMMARY, priorities: PRIORITIES, motivation: MOTIVATION }),
    );

    const response = await service.getDailyOverview('user-1');

    expect(generate).toHaveBeenCalledTimes(1);
    expect(response.result).toEqual({
      summary: SUMMARY,
      priorities: PRIORITIES,
      motivation: MOTIVATION,
    });
  });

  it('sends one combined request with the overview settings', async () => {
    const { service, generate } = makeService(
      JSON.stringify({ summary: SUMMARY, priorities: PRIORITIES, motivation: MOTIVATION }),
    );

    await service.getDailyOverview('user-1', 'focus on the presentation');

    expect(generate).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: 'user-1',
        capabilityId: 'daily-brief-overview',
        templateId: 'assistant',
        contextOptions: {},
        generateOptions: { temperature: 0.6, maxTokens: 1200 },
      }),
    );
    const { instruction } = generate.mock.calls[0][0] as { instruction: string };
    expect(instruction).toContain('different every day');
    expect(instruction).toContain('focus on the presentation');
  });

  it('turns an invalid part into null and keeps the valid ones', async () => {
    const { service } = makeService(
      JSON.stringify({
        summary: { ...SUMMARY, suggestedFocus: '' },
        priorities: PRIORITIES,
        motivation: MOTIVATION,
      }),
    );

    const response = await service.getDailyOverview('user-1');

    expect(response.result).toEqual({
      summary: null,
      priorities: PRIORITIES,
      motivation: MOTIVATION,
    });
  });

  it('turns missing parts and parts of the wrong type into null', async () => {
    const { service } = makeService(
      JSON.stringify({ summary: SUMMARY, priorities: 'first the slides' }),
    );

    const response = await service.getDailyOverview('user-1');

    expect(response.result).toEqual({ summary: SUMMARY, priorities: null, motivation: null });
  });

  it('accepts null parts from the AI', async () => {
    const { service } = makeService(
      JSON.stringify({ summary: null, priorities: [], motivation: MOTIVATION }),
    );

    const response = await service.getDailyOverview('user-1');

    // An empty ranking is not a valid priorities part (at least one task is required).
    expect(response.result).toEqual({ summary: null, priorities: null, motivation: MOTIVATION });
  });

  it('accepts a JSON answer wrapped in a code fence', async () => {
    const { service } = makeService(
      '```json\n' +
        JSON.stringify({ summary: SUMMARY, priorities: PRIORITIES, motivation: MOTIVATION }) +
        '\n```',
    );

    const response = await service.getDailyOverview('user-1');

    expect(response.result.summary).toEqual(SUMMARY);
  });

  it('fails with InvalidProviderResponseException (502) when the answer is not JSON', async () => {
    const { service } = makeService('{"summary": {"todayPriorities": ["cut off');

    await expect(service.getDailyOverview('user-1')).rejects.toBeInstanceOf(
      InvalidProviderResponseException,
    );
  });

  it('fails with InvalidProviderResponseException (502) when the answer is not a JSON object', async () => {
    const { service } = makeService('["not", "an", "object"]');

    await expect(service.getDailyOverview('user-1')).rejects.toBeInstanceOf(
      InvalidProviderResponseException,
    );
  });
});
