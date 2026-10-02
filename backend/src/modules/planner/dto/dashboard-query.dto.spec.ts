import { plainToInstance } from 'class-transformer';
import { validate } from 'class-validator';
import { DashboardQueryDto } from './dashboard-query.dto';

async function errorsFor(query: Record<string, unknown>): Promise<string[]> {
  const errors = await validate(plainToInstance(DashboardQueryDto, query));
  return errors.flatMap((error) => Object.values(error.constraints ?? {}));
}

describe('DashboardQueryDto', () => {
  it('accepts a missing date', async () => {
    expect(await errorsFor({})).toEqual([]);
  });

  it('accepts a valid calendar date', async () => {
    expect(await errorsFor({ date: '2026-10-02' })).toEqual([]);
    expect(await errorsFor({ date: '2028-02-29' })).toEqual([]);
  });

  it.each(['2026-10', '02-10-2026', '2026-10-02T00:00:00Z', 'today'])(
    'rejects the malformed date %s',
    async (date) => {
      expect(await errorsFor({ date })).toContain('date must be in "YYYY-MM-DD" format.');
    },
  );

  it.each(['2026-02-30', '2026-13-01', '2027-02-29'])(
    'rejects the impossible calendar date %s',
    async (date) => {
      expect(await errorsFor({ date })).toContain('date must be a valid calendar date.');
    },
  );
});
