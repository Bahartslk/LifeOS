import { describe, expect, it } from 'vitest';
import {
  DEFAULT_FILTERS,
  hasActiveFilters,
  normalizeSearch,
  parseFilters,
  toSearchParams,
} from './usersQuery';

const parse = (query: string) => parseFilters(new URLSearchParams(query));

describe('usersQuery', () => {
  it('reads the defaults from an empty URL', () => {
    expect(parse('')).toEqual({ q: '', role: '', status: 'active', sort: '-createdAt' });
    expect(hasActiveFilters(DEFAULT_FILTERS)).toBe(false);
  });

  it('reads every filter from the URL', () => {
    expect(parse('q=bahar&role=ADMIN&status=all&sort=-email')).toEqual({
      q: 'bahar',
      role: 'ADMIN',
      status: 'all',
      sort: '-email',
    });
  });

  it('falls back to the default for values the backend would reject with 400', () => {
    expect(parse('role=ROOT&status=banned&sort=displayName&q=a')).toEqual(DEFAULT_FILTERS);
    expect(parse('sort=passwordHash').sort).toBe('-createdAt');
  });

  it('accepts a search of 2 to 100 characters, trimmed', () => {
    expect(normalizeSearch('  ab  ')).toBe('ab');
    expect(normalizeSearch('a')).toBe('');
    expect(normalizeSearch('   ')).toBe('');
    expect(normalizeSearch('x'.repeat(150))).toHaveLength(100);
  });

  it('writes only non-default filters to the URL', () => {
    expect(toSearchParams(DEFAULT_FILTERS).toString()).toBe('');
    expect(
      toSearchParams({
        q: 'bahar',
        role: 'ADMIN',
        status: 'active',
        sort: '-createdAt',
      }).toString(),
    ).toBe('q=bahar&role=ADMIN');
    expect(toSearchParams({ q: '', role: '', status: 'deleted', sort: 'email' }).toString()).toBe(
      'status=deleted&sort=email',
    );
  });

  it('round-trips through the URL unchanged', () => {
    const filters = { q: 'ada', role: 'USER', status: 'all', sort: 'createdAt' } as const;

    expect(parseFilters(toSearchParams(filters))).toEqual(filters);
  });
});
