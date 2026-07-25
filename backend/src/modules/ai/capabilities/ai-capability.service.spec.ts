import { z } from 'zod';
import { AiService } from '../service/ai.service';
import { AiCapabilityService } from './ai-capability.service';
import { AiCapabilityDefinition } from './capability.types';
import { InvalidProviderResponseException } from '../exceptions/ai.exceptions';

const RESULT_SCHEMA = z.object({ title: z.string().min(1) });

function fakeDefinition(
  overrides: Partial<AiCapabilityDefinition<z.infer<typeof RESULT_SCHEMA>>> = {},
): AiCapabilityDefinition<z.infer<typeof RESULT_SCHEMA>> {
  return {
    id: 'test-capability',
    templateId: 'general',
    feature: 'planner',
    contextScope: {},
    basePrompt: 'Do the thing.',
    jsonShapeInstruction: '{"title": string}',
    outputSchema: RESULT_SCHEMA,
    ...overrides,
  };
}

function fakeAiService(generateResult: Record<string, unknown> = {}): AiService {
  return {
    generate: jest.fn().mockResolvedValue({
      text: '{"title": "Hello"}',
      providerId: 'gemini',
      model: 'gemini-2.0-flash',
      usage: { estimatedInputTokens: 10, estimatedOutputTokens: 5 },
      latencyMs: 42,
      retryCount: 0,
      fallbackCount: 0,
      templateId: 'general',
      templateVersion: '1',
      ...generateResult,
    }),
  } as unknown as AiService;
}

describe('AiCapabilityService', () => {
  it("passes the definition's routing/generation metadata through to AiService.generate", async () => {
    const aiService = fakeAiService();
    const service = new AiCapabilityService(aiService);
    const definition = fakeDefinition({
      preferredProvider: 'openrouter',
      fallbackAllowed: false,
      temperature: 0.2,
      maxTokens: 300,
      contextScope: { includeTravel: false },
    });

    await service.run(definition, 'user-1', 'focus on X', 'raw input text');

    expect(aiService.generate).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: 'user-1',
        templateId: 'general',
        feature: 'planner',
        contextOptions: { includeTravel: false },
        capabilityId: 'test-capability',
        preferredProviderId: 'openrouter',
        fallbackAllowed: false,
        generateOptions: { temperature: 0.2, maxTokens: 300 },
      }),
    );
  });

  it('composes the instruction from basePrompt, primaryInput, notes, and jsonShapeInstruction in order', async () => {
    const aiService = fakeAiService();
    const service = new AiCapabilityService(aiService);
    const definition = fakeDefinition();

    await service.run(definition, 'user-1', 'a note', 'the primary input');

    const call = (aiService.generate as jest.Mock).mock.calls[0][0];
    const basePromptIndex = call.instruction.indexOf('Do the thing.');
    const inputIndex = call.instruction.indexOf('the primary input');
    const notesIndex = call.instruction.indexOf('a note');
    const shapeIndex = call.instruction.indexOf('{"title": string}');
    expect(basePromptIndex).toBeGreaterThanOrEqual(0);
    expect(basePromptIndex).toBeLessThan(inputIndex);
    expect(inputIndex).toBeLessThan(notesIndex);
    expect(notesIndex).toBeLessThan(shapeIndex);
  });

  it('omits the Input/notes sections entirely when neither is given', async () => {
    const aiService = fakeAiService();
    const service = new AiCapabilityService(aiService);

    await service.run(fakeDefinition(), 'user-1');

    const call = (aiService.generate as jest.Mock).mock.calls[0][0];
    expect(call.instruction).not.toContain('Input:');
    expect(call.instruction).not.toContain('Additional focus');
  });

  it('validates and returns the parsed result in the {provider, model, result, usage, latencyMs} envelope', async () => {
    const aiService = fakeAiService();
    const service = new AiCapabilityService(aiService);

    const result = await service.run(fakeDefinition(), 'user-1');

    expect(result).toEqual({
      provider: 'gemini',
      model: 'gemini-2.0-flash',
      result: { title: 'Hello' },
      usage: { inputTokens: 10, outputTokens: 5, totalTokens: 15 },
      latencyMs: 42,
    });
  });

  it('throws InvalidProviderResponseException when the provider response fails schema validation', async () => {
    const aiService = fakeAiService({ text: '{"title": ""}' });
    const service = new AiCapabilityService(aiService);

    await expect(service.run(fakeDefinition(), 'user-1')).rejects.toThrow(
      InvalidProviderResponseException,
    );
  });
});
