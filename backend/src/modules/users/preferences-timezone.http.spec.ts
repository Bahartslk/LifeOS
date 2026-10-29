import { createHttpTestApp, HttpTestApp, testUser } from '../../../test/support/http-test-app';

/**
 * The time zone rules of `PATCH /api/v1/users/preferences` and
 * `PATCH /api/v1/users/me` over real HTTP (real validation pipe; in-memory
 * Prisma whose update only records what would be written).
 */
describe('time zone validation (HTTP)', () => {
  const user = testUser({ email: 'tz@example.com' });
  let ctx: HttpTestApp;
  let token: string;
  const updateMany = jest.fn();

  beforeAll(async () => {
    ctx = await createHttpTestApp([user]);
    Object.assign(ctx.prisma.user, { updateMany });
    token = await ctx.signAccessToken({ sub: user.id, email: user.email });
  });

  afterAll(async () => {
    await ctx.close();
  });

  beforeEach(() => updateMany.mockReset().mockResolvedValue({ count: 1 }));

  describe.each(['/api/v1/users/preferences', '/api/v1/users/me'])('%s', (path) => {
    const patch = (timezone: unknown) => ctx.http.patch(path, { token, body: { timezone } });

    it.each(['Europe/Istanbul', 'GMT', 'UTC', 'Etc/UTC', 'America/Argentina/Buenos_Aires'])(
      'accepts %s and stores it as sent',
      async (timezone) => {
        const result = await patch(timezone);

        expect(result.status).toBe(200);
        expect(updateMany).toHaveBeenCalledWith({
          where: { id: user.id, deletedAt: null },
          data: { timezone },
        });
      },
    );

    it.each([
      ['an unknown zone', 'Mars/Olympus_Mons'],
      ['a UTC offset', '+03:00'],
      ['a GMT offset', 'GMT+03:00'],
      ['a non-string', 42],
      ['an overlong value', `Europe/${'A'.repeat(80)}`],
    ])('rejects %s with 400', async (_label, timezone) => {
      const result = await patch(timezone);

      expect(result.status).toBe(400);
      expect(updateMany).not.toHaveBeenCalled();
    });
  });
});
