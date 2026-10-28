import { Logger } from '@nestjs/common';
import { ApiError, GoogleGenAI, ThinkingLevel } from '@google/genai';
import { AiConfigService } from '../../config/ai-config.service';
import {
  InvalidProviderResponseException,
  ProviderRateLimitException,
  ProviderUnavailableException,
} from '../../exceptions/ai.exceptions';
import { GeminiProvider } from './gemini.provider';

jest.mock('@google/genai', () => ({
  ...jest.requireActual<typeof import('@google/genai')>('@google/genai'),
  GoogleGenAI: jest.fn(),
}));

const generateContent = jest.fn();
const CHAIN = ['gemini-3.5-flash', 'gemini-2.5-flash', 'gemini-3.5-flash-lite'];
const REQUEST = { systemPrompt: 's', userPrompt: 'u' };
const OK_RESPONSE = {
  text: '{"ok":true}',
  usageMetadata: { promptTokenCount: 10, candidatesTokenCount: 5 },
};

function makeProvider(modelChain: string[] = CHAIN): GeminiProvider {
  const config = {
    geminiApiKey: 'test-key',
    model: modelChain[0],
    modelChain,
    temperature: 0.7,
    topP: 0.95,
    topK: 40,
    maxTokens: 1024,
    timeoutMs: 15000,
  } as unknown as AiConfigService;
  return new GeminiProvider(config);
}

type SentRequest = { model: string; config: Record<string, unknown> };

function sentRequests(): SentRequest[] {
  return generateContent.mock.calls.map(([request]) => request as SentRequest);
}

function apiError(status: number): ApiError {
  return new ApiError({ message: `status ${status}`, status });
}

describe('GeminiProvider', () => {
  let warn: jest.SpyInstance;

  beforeEach(() => {
    generateContent.mockReset();
    generateContent.mockResolvedValue(OK_RESPONSE);
    jest
      .mocked(GoogleGenAI)
      .mockImplementation(() => ({ models: { generateContent } }) as unknown as GoogleGenAI);
    warn = jest.spyOn(Logger.prototype, 'warn').mockImplementation(() => undefined);
    jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => jest.restoreAllMocks());

  describe('thinking setting per model', () => {
    it.each([
      ['gemini-3.5-flash', { thinkingLevel: ThinkingLevel.MINIMAL }],
      ['gemini-2.5-flash', { thinkingBudget: 0 }],
    ])('sends the right setting for %s', async (model, expected) => {
      await makeProvider([model]).generate(REQUEST);

      expect(sentRequests()[0].config.thinkingConfig).toEqual(expected);
    });

    it('sends no thinking setting at all for gemini-3.5-flash-lite', async () => {
      await makeProvider(['gemini-3.5-flash-lite']).generate(REQUEST);

      expect(sentRequests()[0].config).not.toHaveProperty('thinkingConfig');
    });

    it("uses each fallback model's own setting", async () => {
      generateContent
        .mockRejectedValueOnce(apiError(429))
        .mockRejectedValueOnce(apiError(429))
        .mockResolvedValueOnce(OK_RESPONSE);

      await makeProvider().generate(REQUEST);

      expect(sentRequests().map((r) => r.config.thinkingConfig)).toEqual([
        { thinkingLevel: ThinkingLevel.MINIMAL },
        { thinkingBudget: 0 },
        undefined,
      ]);
    });
  });

  it("keeps the request's own output token limit", async () => {
    await makeProvider().generate({ ...REQUEST, options: { maxTokens: 150 } });

    expect(sentRequests()[0].config.maxOutputTokens).toBe(150);
  });

  describe('model chain', () => {
    it('uses only the first model when it answers, and reports it', async () => {
      const result = await makeProvider().generate(REQUEST);

      expect(sentRequests().map((r) => r.model)).toEqual(['gemini-3.5-flash']);
      expect(result.model).toBe('gemini-3.5-flash');
    });

    it.each([429, 503])(
      'moves to the next model on %i and reports the model that answered',
      async (status) => {
        generateContent.mockRejectedValueOnce(apiError(status)).mockResolvedValueOnce(OK_RESPONSE);

        const result = await makeProvider().generate(REQUEST);

        expect(sentRequests().map((r) => r.model)).toEqual([
          'gemini-3.5-flash',
          'gemini-2.5-flash',
        ]);
        expect(result.model).toBe('gemini-2.5-flash');
        expect(warn).toHaveBeenCalledWith(expect.stringContaining('trying "gemini-2.5-flash"'));
      },
    );

    it('walks the whole chain in order', async () => {
      generateContent
        .mockRejectedValueOnce(apiError(429))
        .mockRejectedValueOnce(apiError(503))
        .mockResolvedValueOnce(OK_RESPONSE);

      const result = await makeProvider().generate(REQUEST);

      expect(sentRequests().map((r) => r.model)).toEqual(CHAIN);
      expect(result.model).toBe('gemini-3.5-flash-lite');
    });

    it('throws ProviderRateLimitException when every model is out of quota', async () => {
      generateContent.mockRejectedValue(apiError(429));

      await expect(makeProvider().generate(REQUEST)).rejects.toBeInstanceOf(
        ProviderRateLimitException,
      );
      expect(generateContent).toHaveBeenCalledTimes(3);
    });

    it('throws ProviderUnavailableException when the last model is overloaded', async () => {
      generateContent.mockRejectedValueOnce(apiError(429)).mockRejectedValue(apiError(503));

      await expect(makeProvider().generate(REQUEST)).rejects.toBeInstanceOf(
        ProviderUnavailableException,
      );
      expect(generateContent).toHaveBeenCalledTimes(3);
    });

    it.each([
      ['a rejected request (400)', apiError(400), InvalidProviderResponseException],
      ['a server error (500)', apiError(500), ProviderUnavailableException],
      ['a non-HTTP failure', new Error('socket hang up'), ProviderUnavailableException],
    ])('does not move to the next model on %s', async (_label, error, expected) => {
      generateContent.mockRejectedValueOnce(error);

      await expect(makeProvider().generate(REQUEST)).rejects.toBeInstanceOf(expected);
      expect(generateContent).toHaveBeenCalledTimes(1);
    });
  });
});
