import {
  dateInTimeZone,
  isIanaTimeZone,
  todayInTimeZone,
  zonedDateTimeToUtc,
} from './date-time.util';

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

describe('isIanaTimeZone', () => {
  it.each([
    'Europe/Istanbul',
    'America/Argentina/Buenos_Aires',
    'America/Indiana/Indianapolis',
    'Etc/GMT-3',
    'UTC',
    'Etc/UTC',
    'GMT',
    'Asia/Calcutta',
  ])('accepts %s', (value) => {
    expect(isIanaTimeZone(value)).toBe(true);
  });

  it.each([
    ['an unknown zone', 'Mars/Olympus_Mons'],
    ['a UTC offset', '+03:00'],
    ['a negative UTC offset', '-05:00'],
    ['a GMT offset', 'GMT+03:00'],
    ['an empty string', ''],
    ['surrounding spaces', ' Europe/Istanbul '],
    ['a non-string', 3],
    ['null', null],
  ])('rejects %s', (_label, value) => {
    expect(isIanaTimeZone(value)).toBe(false);
  });
});

describe('zonedDateTimeToUtc', () => {
  it('converts a fixed-offset zone', () => {
    expect(zonedDateTimeToUtc('2026-11-02', '10:00', 'Europe/Istanbul').toISOString()).toBe(
      '2026-11-02T07:00:00.000Z',
    );
  });

  it('follows daylight saving time', () => {
    expect(zonedDateTimeToUtc('2026-07-01', '10:00', 'Europe/Berlin').toISOString()).toBe(
      '2026-07-01T08:00:00.000Z',
    );
    expect(zonedDateTimeToUtc('2026-12-01', '10:00', 'Europe/Berlin').toISOString()).toBe(
      '2026-12-01T09:00:00.000Z',
    );
  });

  it('handles a zone behind UTC and a local time on the previous UTC day', () => {
    expect(zonedDateTimeToUtc('2026-11-02', '20:30', 'America/New_York').toISOString()).toBe(
      '2026-11-03T01:30:00.000Z',
    );
    expect(zonedDateTimeToUtc('2026-11-02', '01:00', 'Asia/Tokyo').toISOString()).toBe(
      '2026-11-01T16:00:00.000Z',
    );
  });

  it('accepts aliases such as UTC and GMT', () => {
    expect(zonedDateTimeToUtc('2026-11-02', '08:00', 'GMT').toISOString()).toBe(
      '2026-11-02T08:00:00.000Z',
    );
  });
});
