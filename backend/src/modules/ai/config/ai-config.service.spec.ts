import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../../config/configuration';
import { AiConfigService } from './ai-config.service';

function makeService(model: string, modelFallbacks: string[]): AiConfigService {
  const values: Record<string, unknown> = {
    'ai.model': model,
    'ai.modelFallbacks': modelFallbacks,
  };
  const configService = { get: (key: string) => values[key] } as ConfigService<AppConfig, true>;
  return new AiConfigService(configService);
}

describe('AiConfigService.modelChain', () => {
  it('puts AI_MODEL first, then the fallbacks in order', () => {
    const service = makeService('gemini-3.5-flash', ['gemini-2.5-flash', 'gemini-3.5-flash-lite']);

    expect(service.modelChain).toEqual([
      'gemini-3.5-flash',
      'gemini-2.5-flash',
      'gemini-3.5-flash-lite',
    ]);
  });

  it('drops duplicates, so listing AI_MODEL again does not call it twice', () => {
    const service = makeService('gemini-3.5-flash', ['gemini-3.5-flash', 'gemini-2.5-flash']);

    expect(service.modelChain).toEqual(['gemini-3.5-flash', 'gemini-2.5-flash']);
  });

  it('leaves preview models out of the fallbacks', () => {
    const service = makeService('gemini-3.5-flash', [
      'gemini-3-flash-preview',
      'gemini-2.5-flash-preview-09-2025',
      'gemini-2.5-flash',
    ]);

    expect(service.modelChain).toEqual(['gemini-3.5-flash', 'gemini-2.5-flash']);
  });

  it('keeps AI_MODEL even if it is a preview model (an explicit choice)', () => {
    const service = makeService('gemini-3-flash-preview', ['gemini-2.5-flash']);

    expect(service.modelChain).toEqual(['gemini-3-flash-preview', 'gemini-2.5-flash']);
  });
});
