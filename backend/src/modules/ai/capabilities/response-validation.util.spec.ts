import { z } from 'zod';
import { InvalidProviderResponseException } from '../exceptions/ai.exceptions';
import { parseAndValidateStructuredResponse } from './response-validation.util';

const SCHEMA = z.object({ title: z.string().min(1) });

describe('parseAndValidateStructuredResponse', () => {
  it('parses and validates well-formed JSON matching the schema', () => {
    const result = parseAndValidateStructuredResponse('{"title": "Hello"}', SCHEMA, 'gemini');
    expect(result).toEqual({ title: 'Hello' });
  });

  it('strips a ```json code fence before parsing', () => {
    const result = parseAndValidateStructuredResponse(
      '```json\n{"title": "Hello"}\n```',
      SCHEMA,
      'gemini',
    );
    expect(result).toEqual({ title: 'Hello' });
  });

  it('strips a bare ``` fence (no "json" language tag) before parsing', () => {
    const result = parseAndValidateStructuredResponse(
      '```\n{"title": "Hello"}\n```',
      SCHEMA,
      'gemini',
    );
    expect(result).toEqual({ title: 'Hello' });
  });

  it('throws InvalidProviderResponseException for text that is not JSON at all', () => {
    expect(() => parseAndValidateStructuredResponse('not json', SCHEMA, 'gemini')).toThrow(
      InvalidProviderResponseException,
    );
  });

  it('throws InvalidProviderResponseException for JSON that violates a schema constraint', () => {
    expect(() => parseAndValidateStructuredResponse('{"title": ""}', SCHEMA, 'gemini')).toThrow(
      InvalidProviderResponseException,
    );
  });

  it('throws InvalidProviderResponseException for JSON missing a required field', () => {
    expect(() => parseAndValidateStructuredResponse('{}', SCHEMA, 'gemini')).toThrow(
      InvalidProviderResponseException,
    );
  });

  it('throws InvalidProviderResponseException for a JSON array where an object is expected', () => {
    expect(() => parseAndValidateStructuredResponse('[1,2,3]', SCHEMA, 'gemini')).toThrow(
      InvalidProviderResponseException,
    );
  });
});
