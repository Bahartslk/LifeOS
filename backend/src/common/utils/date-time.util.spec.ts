import { dateInTimeZone, todayInTimeZone } from './date-time.util';

describe('dateInTimeZone', () => {
  it('Europe/Istanbul is already the next day at 22:30 UTC (01:30 local)', () => {
    expect(dateInTimeZone(new Date('2026-10-01T22:30:00Z'), 'Europe/Istanbul')).toBe('2026-10-02');
  });

  it('Europe/Istanbul at 00:30 UTC (03:30 local) is the same calendar day', () => {
    expect(dateInTimeZone(new Date('2026-10-02T00:30:00Z'), 'Europe/Istanbul')).toBe('2026-10-02');
  });

  it('Europe/Istanbul switches day exactly at 21:00 UTC (UTC+3)', () => {
    expect(dateInTimeZone(new Date('2026-10-01T20:59:59Z'), 'Europe/Istanbul')).toBe('2026-10-01');
    expect(dateInTimeZone(new Date('2026-10-01T21:00:00Z'), 'Europe/Istanbul')).toBe('2026-10-02');
  });

  it('UTC returns the UTC calendar date', () => {
    expect(dateInTimeZone(new Date('2026-10-01T22:30:00Z'), 'UTC')).toBe('2026-10-01');
  });

  it('handles zones behind and ahead of UTC', () => {
    // New York is UTC-4 in October (EDT): 03:00 UTC is still the previous evening.
    expect(dateInTimeZone(new Date('2026-10-02T03:00:00Z'), 'America/New_York')).toBe('2026-10-01');
    // Tokyo is UTC+9: 15:30 UTC is already 00:30 the next day.
    expect(dateInTimeZone(new Date('2026-10-01T15:30:00Z'), 'Asia/Tokyo')).toBe('2026-10-02');
  });

  it('crosses month and year boundaries', () => {
    expect(dateInTimeZone(new Date('2026-12-31T21:30:00Z'), 'Europe/Istanbul')).toBe('2027-01-01');
  });

  it('falls back to the UTC date for an invalid timezone instead of throwing', () => {
    expect(dateInTimeZone(new Date('2026-10-01T22:30:00Z'), 'Not/AZone')).toBe('2026-10-01');
    expect(dateInTimeZone(new Date('2026-10-01T22:30:00Z'), '')).toBe('2026-10-01');
  });
});

describe('todayInTimeZone', () => {
  afterEach(() => jest.useRealTimers());

  it('uses the given clock', () => {
    expect(todayInTimeZone('Europe/Istanbul', new Date('2026-10-01T22:30:00Z'))).toBe('2026-10-02');
  });

  it('defaults to the current time', () => {
    jest.useFakeTimers({ now: new Date('2026-10-01T22:30:00Z') });

    expect(todayInTimeZone('Europe/Istanbul')).toBe('2026-10-02');
    expect(todayInTimeZone('UTC')).toBe('2026-10-01');
  });
});
