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
const REMINDERS = [
  {
    title: 'Otel rezervasyonunu kontrol et',
    reason: "31 Ekim'deki rezervasyondan bir gün önce.",
    relatedTo: 'TRAVEL',
    recommendedDate: '2026-10-30',
  },
  {
    title: 'Slaytları yedekle',
    reason: 'Sunumdan önce.',
    relatedTo: 'PLANNER',
    recommendedDate: null,
  },
];
const ALL_PARTS = {
  summary: SUMMARY,
  priorities: PRIORITIES,
  motivation: MOTIVATION,
  reminders: REMINDERS,
};

function makeService(aiText: string) {
  const generate = jest.fn().mockResolvedValue({
    text: aiText,
    providerId: 'gemini',
    model: 'gemini-3.5-flash',
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

async function overviewFor(answer: unknown) {
  const { service } = makeService(JSON.stringify(answer));
  return (await service.getDailyOverview('user-1')).result;
}

describe('DailyBriefService.getDailyOverview', () => {
  it('returns all four parts from a single AI call', async () => {
    const { service, generate } = makeService(JSON.stringify(ALL_PARTS));

    const response = await service.getDailyOverview('user-1');

    expect(generate).toHaveBeenCalledTimes(1);
    expect(response.result).toEqual(ALL_PARTS);
  });

  it('sends one combined request with the overview settings', async () => {
    const { service, generate } = makeService(JSON.stringify(ALL_PARTS));

    await service.getDailyOverview('user-1', 'focus on the presentation');

    expect(generate).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: 'user-1',
        capabilityId: 'daily-brief-overview',
        templateId: 'assistant',
        contextOptions: {},
        generateOptions: { temperature: 0.6, maxTokens: 1600 },
      }),
    );
    const { instruction } = generate.mock.calls[0][0] as { instruction: string };
    expect(instruction).toContain('different every day');
    expect(instruction).toContain('4) reminders');
    expect(instruction).toContain('"recommendedDate": "YYYY-MM-DD" | null');
    expect(instruction).toContain('focus on the presentation');
  });

  it('turns an invalid part into null and keeps the valid ones', async () => {
    const result = await overviewFor({ ...ALL_PARTS, summary: { ...SUMMARY, suggestedFocus: '' } });

    expect(result).toEqual({ ...ALL_PARTS, summary: null });
  });

  it('turns missing parts and parts of the wrong type into null', async () => {
    const result = await overviewFor({ summary: SUMMARY, priorities: 'first the slides' });

    expect(result).toEqual({
      summary: SUMMARY,
      priorities: null,
      motivation: null,
      reminders: null,
    });
  });

  it('accepts null parts from the AI', async () => {
    const result = await overviewFor({ ...ALL_PARTS, summary: null, motivation: null });

    expect(result).toEqual({ ...ALL_PARTS, summary: null, motivation: null });
  });

  it('keeps an empty ranking as a valid answer (no tasks to rank today)', async () => {
    const result = await overviewFor({ ...ALL_PARTS, priorities: [] });

    expect(result.priorities).toEqual([]);
  });

  it('turns a ranking with an invalid item into null', async () => {
    const result = await overviewFor({
      ...ALL_PARTS,
      priorities: [{ taskTitle: '', suggestedRank: 0, reason: 'x' }],
    });

    expect(result.priorities).toBeNull();
  });

  describe('reminders', () => {
    it('keeps an empty list as a valid answer (nothing needs a reminder)', async () => {
      const result = await overviewFor({ ...ALL_PARTS, reminders: [] });

      expect(result.reminders).toEqual([]);
    });

    it.each([
      ['an unknown relatedTo', { ...REMINDERS[0], relatedTo: 'WORK' }],
      ['a date in the wrong format', { ...REMINDERS[0], recommendedDate: '30.10.2026' }],
      ['an empty title', { ...REMINDERS[0], title: '' }],
      ['a missing reason', { title: 'x', relatedTo: 'PLANNER', recommendedDate: null }],
    ])('turns the list into null when an item has %s', async (_label, item) => {
      const result = await overviewFor({ ...ALL_PARTS, reminders: [REMINDERS[1], item] });

      expect(result.reminders).toBeNull();
      expect(result.summary).toEqual(SUMMARY);
    });

    it('turns more than 5 reminders into null', async () => {
      const result = await overviewFor({ ...ALL_PARTS, reminders: Array(6).fill(REMINDERS[1]) });

      expect(result.reminders).toBeNull();
    });
  });

  it('accepts a JSON answer wrapped in a code fence', async () => {
    const { service } = makeService('```json\n' + JSON.stringify(ALL_PARTS) + '\n```');

    const response = await service.getDailyOverview('user-1');

    expect(response.result).toEqual(ALL_PARTS);
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
