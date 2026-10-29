import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';

/**
 * `POST /api/v1/ai/chat` over real HTTP (real AppModule, validation pipe,
 * guards and error filter; in-memory Prisma). The AI call itself is covered
 * in `assistant-chat.service.spec.ts`; here only requests that never reach
 * the AI are sent.
 */
describe('POST /api/v1/ai/chat (HTTP)', () => {
  const user = testUser({ email: 'chat@example.com' });
  let ctx: HttpTestApp;
  let token: string;
  const queryRaw = jest.fn();

  beforeAll(async () => {
    ctx = await createHttpTestApp([user]);
    Object.assign(ctx.prisma, { $queryRaw: queryRaw, $executeRaw: jest.fn() });
    token = await ctx.signAccessToken({ sub: user.id, email: user.email });
  });

  afterAll(async () => {
    await ctx.close();
  });

  beforeEach(() => queryRaw.mockReset());

  const chat = (body: unknown) => ctx.http.post('/api/v1/ai/chat', { token, body });

  it('requires authentication', async () => {
    const result = await ctx.http.post('/api/v1/ai/chat', { body: { message: 'Merhaba' } });

    expect(result.status).toBe(401);
  });

  it.each([
    ['an empty message', { message: '   ' }],
    ['a message over 500 characters', { message: 'a'.repeat(501) }],
    [
      'more than 8 history turns',
      { message: 'Soru', history: Array(9).fill({ role: 'user', text: 'x' }) },
    ],
    ['an unknown history role', { message: 'Soru', history: [{ role: 'system', text: 'x' }] }],
    ['an unexpected field', { message: 'Soru', model: 'gpt' }],
  ])('rejects %s with 400 before counting anything', async (_label, body) => {
    const result = await chat(body);

    expect(result.status).toBe(400);
    expect(queryRaw).not.toHaveBeenCalled();
  });

  it('answers 429 with code CHAT_DAILY_LIMIT_REACHED once the daily limit is used up', async () => {
    queryRaw.mockResolvedValue([]); // the upsert's WHERE matched nothing: limit reached

    const result = await chat({ message: 'Bugün neye odaklanmalıyım?' });

    expect(result.status).toBe(429);
    expect(result.body).toEqual(
      expect.objectContaining({
        statusCode: 429,
        error: 'TOO_MANY_REQUESTS',
        code: 'CHAT_DAILY_LIMIT_REACHED',
        message: 'Daily chat limit of 5 messages reached. Try again tomorrow.',
      }),
    );
  });
});
