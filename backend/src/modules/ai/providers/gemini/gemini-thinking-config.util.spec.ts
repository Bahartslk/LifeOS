import { thinkingConfigFor } from './gemini-thinking-config.util';

describe('thinkingConfigFor', () => {
  it.each([
    'gemini-2.5-flash',
    'gemini-2.5-flash-lite',
    'models/gemini-2.5-flash',
    'gemini-2.5-flash-preview-09-2025',
    'gemini-2.5-flash-lite-preview-06-2025',
  ])('turns thinking off for %s', (model) => {
    expect(thinkingConfigFor(model)).toEqual({ thinkingBudget: 0 });
  });

  it.each([
    'gemini-2.5-pro',
    'gemini-2.0-flash',
    'gemini-3.8-flash',
    'gemini-flash-latest',
    'gemini-2.5-flash-image',
    'gemini-2.5-flash-preview-tts',
    'gemini-2.5-flash-x',
    '',
  ])('sends no thinking setting for %s', (model) => {
    expect(thinkingConfigFor(model)).toBeUndefined();
  });
});
