import { plainToInstance } from 'class-transformer';
import { validate } from 'class-validator';
import { AdminUsersQueryDto } from './admin-users-query.dto';

const VALID_UUID = '3f2b8c1e-5d4a-4b6f-9c7e-1a2b3c4d5e6f';

function parse(query: Record<string, unknown>): AdminUsersQueryDto {
  return plainToInstance(AdminUsersQueryDto, query);
}

async function errorsFor(query: Record<string, unknown>): Promise<string[]> {
  const errors = await validate(parse(query), { whitelist: true, forbidNonWhitelisted: true });
  return errors.flatMap((error) => Object.values(error.constraints ?? {}));
}

describe('AdminUsersQueryDto', () => {
  it('applies the defaults to an empty query', async () => {
    const dto = parse({});

    expect(await errorsFor({})).toEqual([]);
    expect(dto.limit).toBe(20);
    expect(dto.status).toBe('active');
    expect(dto.sort).toBe('-createdAt');
    expect(dto.cursor).toBeUndefined();
    expect(dto.q).toBeUndefined();
    expect(dto.role).toBeUndefined();
  });

  it('accepts a fully specified valid query', async () => {
    const query = {
      limit: '50',
      cursor: VALID_UUID,
      q: 'ada',
      role: 'ADMIN',
      status: 'all',
      sort: 'email',
    };

    expect(await errorsFor(query)).toEqual([]);
    expect(parse(query).limit).toBe(50);
  });

  it.each(['1', '100'])('accepts the boundary limit %s', async (limit) => {
    expect(await errorsFor({ limit })).toEqual([]);
  });

  it.each(['0', '101', '-1', '1.5', 'abc'])('rejects the limit %s', async (limit) => {
    expect(await errorsFor({ limit })).not.toEqual([]);
  });

  it.each(['abc', '123', "' OR 1=1 --", '3f2b8c1e-5d4a-4b6f-9c7e'])(
    'rejects the non-UUID cursor %s',
    async (cursor) => {
      expect(await errorsFor({ cursor })).toContain('cursor must be a UUID');
    },
  );

  it.each(['createdAt', '-createdAt', 'email', '-email'])('accepts sort=%s', async (sort) => {
    expect(await errorsFor({ sort })).toEqual([]);
  });

  it.each(['passwordHash', '-passwordHash', 'displayName', 'createdAt,email', ''])(
    'rejects sort=%s',
    async (sort) => {
      expect(await errorsFor({ sort })).toContain(
        'sort must be one of: createdAt, -createdAt, email, -email',
      );
    },
  );

  it.each(['USER', 'ADMIN'])('accepts role=%s', async (role) => {
    expect(await errorsFor({ role })).toEqual([]);
  });

  it.each(['admin', 'SUPERADMIN', ''])('rejects role=%s', async (role) => {
    expect(await errorsFor({ role })).not.toEqual([]);
  });

  it.each(['active', 'deleted', 'all'])('accepts status=%s', async (status) => {
    expect(await errorsFor({ status })).toEqual([]);
  });

  it.each(['ACTIVE', 'banned', ''])('rejects status=%s', async (status) => {
    expect(await errorsFor({ status })).toContain('status must be one of: active, deleted, all');
  });

  it('trims q and accepts 2 to 100 characters', async () => {
    expect(parse({ q: '  ada  ' }).q).toBe('ada');
    expect(await errorsFor({ q: 'ab' })).toEqual([]);
    expect(await errorsFor({ q: 'a'.repeat(100) })).toEqual([]);
  });

  it.each([
    ['a single character', 'a'],
    ['only whitespace around one character', '  a  '],
    ['an empty string', ''],
    ['101 characters', 'a'.repeat(101)],
  ])('rejects q that is %s', async (_name, q) => {
    expect(await errorsFor({ q })).not.toEqual([]);
  });

  it('rejects an unknown query parameter', async () => {
    expect(await errorsFor({ includeDeleted: 'true' })).toContain(
      'property includeDeleted should not exist',
    );
  });
});
