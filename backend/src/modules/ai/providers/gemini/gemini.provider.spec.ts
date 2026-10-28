import { Logger } from '@nestjs/common';
import { ApiError, GoogleGenAI } from '@google/genai';
import { AiConfigService } from '../../config/ai-config.service';
import { ProviderRateLimitException } from '../../exceptions/ai.exceptions';
import { GeminiProvider } from './gemini.provider';

jest.mock('@google/genai', () => ({
  ...jest.requireActual<typeof import('@google/genai')>('@google/genai'),
  GoogleGenAI: jest.fn(),
}));

const generateContent = jest.fn();

function makeProvider(model: string): GeminiProvider {
  const config = {
    geminiApiKey: 'test-key',
    model,
    temperature: 0.7,
    topP: 0.95,
    topK: 40,
    maxTokens: 1024,
    timeoutMs: 15000,
  } as unknown as AiConfigService;
  return new GeminiProvider(config);
}

function sentConfig(): Record<string, unknown> {
  const [request] = generateContent.mock.calls[0] as [{ config: Record<string, unknown> }];
  return request.config;
}

describe('GeminiProvider', () => {
  beforeEach(() => {
    generateContent.mockReset();
    generateContent.mockResolvedValue({
      text: '{"ok":true}',
      usageMetadata: { promptTokenCount: 10, candidatesTokenCount: 5 },
    });
    jest
      .mocked(GoogleGenAI)
      .mockImplementation(() => ({ models: { generateContent } }) as unknown as GoogleGenAI);
  });

  afterEach(() => jest.restoreAllMocks());

  it('turns thinking off for a model that supports a thinking budget', async () => {
    await makeProvider('gemini-2.5-flash').generate({ systemPrompt: 's', userPrompt: 'u' });

    expect(sentConfig().thinkingConfig).toEqual({ thinkingBudget: 0 });
  });

  it('sends no thinking setting at all for a model that does not support it', async () => {
    await makeProvider('gemini-3.8-flash').generate({ systemPrompt: 's', userPrompt: 'u' });

    expect(sentConfig()).not.toHaveProperty('thinkingConfig');
  });

  it("keeps the request's own output token limit", async () => {
    await makeProvider('gemini-2.5-flash').generate({
      systemPrompt: 's',
      userPrompt: 'u',
      options: { maxTokens: 150 },
    });

    expect(sentConfig().maxOutputTokens).toBe(150);
  });

  it('maps a provider 429 to ProviderRateLimitException', async () => {
    generateContent.mockRejectedValue(new ApiError({ message: 'quota exceeded', status: 429 }));
    jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined);

    await expect(
      makeProvider('gemini-2.5-flash').generate({ systemPrompt: 's', userPrompt: 'u' }),
    ).rejects.toBeInstanceOf(ProviderRateLimitException);
    expect(generateContent).toHaveBeenCalledTimes(1);
  });
});
