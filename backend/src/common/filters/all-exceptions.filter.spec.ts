import { ArgumentsHost, BadRequestException, HttpException, HttpStatus } from '@nestjs/common';
import { AllExceptionsFilter } from './all-exceptions.filter';

function run(exception: unknown) {
  const json = jest.fn();
  const status = jest.fn().mockReturnValue({ json });
  const host = {
    switchToHttp: () => ({
      getResponse: () => ({ status }),
      getRequest: () => ({ url: '/api/v1/test' }),
    }),
  } as unknown as ArgumentsHost;

  new AllExceptionsFilter().catch(exception, host);
  return { status: status.mock.calls[0][0] as number, body: json.mock.calls[0][0] };
}

describe('AllExceptionsFilter', () => {
  it('passes an exception-provided code through to the error body', () => {
    const { status, body } = run(
      new HttpException(
        { code: 'CHAT_DAILY_LIMIT_REACHED', message: 'Limit reached.' },
        HttpStatus.TOO_MANY_REQUESTS,
      ),
    );

    expect(status).toBe(429);
    expect(body).toEqual(
      expect.objectContaining({
        statusCode: 429,
        error: 'TOO_MANY_REQUESTS',
        code: 'CHAT_DAILY_LIMIT_REACHED',
        message: 'Limit reached.',
        path: '/api/v1/test',
      }),
    );
  });

  it('has no code field when the exception sets none', () => {
    const { body } = run(new BadRequestException(['message must be a string']));

    expect(body).not.toHaveProperty('code');
    expect(body.message).toEqual(['message must be a string']);
  });
});
