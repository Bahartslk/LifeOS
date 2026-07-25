import { HttpStatus } from '@nestjs/common';
import { classifyProviderHttpError } from './classify-provider-http-error.util';
import {
  InvalidProviderResponseException,
  ProviderRateLimitException,
  ProviderUnavailableException,
} from './ai.exceptions';

describe('classifyProviderHttpError', () => {
  it('classifies 429 as ProviderRateLimitException', () => {
    expect(classifyProviderHttpError('gemini', HttpStatus.TOO_MANY_REQUESTS)).toBeInstanceOf(
      ProviderRateLimitException,
    );
  });

  it('classifies any 5xx as ProviderUnavailableException', () => {
    expect(classifyProviderHttpError('gemini', 500)).toBeInstanceOf(ProviderUnavailableException);
    expect(classifyProviderHttpError('gemini', 503)).toBeInstanceOf(ProviderUnavailableException);
  });

  it('classifies any other 4xx as InvalidProviderResponseException', () => {
    expect(classifyProviderHttpError('gemini', 400)).toBeInstanceOf(
      InvalidProviderResponseException,
    );
    expect(classifyProviderHttpError('gemini', 404)).toBeInstanceOf(
      InvalidProviderResponseException,
    );
  });

  it('carries the provider id through into the exception message', () => {
    const error = classifyProviderHttpError('openrouter', 400);
    expect(error.message).toContain('openrouter');
  });
});
