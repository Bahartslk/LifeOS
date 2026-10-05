const LOCALE = 'tr-TR';

const dateTimeFormat = new Intl.DateTimeFormat(LOCALE, { dateStyle: 'medium', timeStyle: 'short' });
const dateFormat = new Intl.DateTimeFormat(LOCALE, { dateStyle: 'medium' });
const numberFormat = new Intl.NumberFormat(LOCALE);

export const EMPTY_VALUE = '—';

function parse(value: string | null | undefined): Date | null {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

/** An ISO timestamp from the API as a Turkish date and time, in the viewer's timezone. */
export function formatDateTime(value: string | null | undefined): string {
  const date = parse(value);
  return date ? dateTimeFormat.format(date) : EMPTY_VALUE;
}

export function formatDate(value: string | null | undefined): string {
  const date = parse(value);
  return date ? dateFormat.format(date) : EMPTY_VALUE;
}

export function formatNumber(value: number): string {
  return numberFormat.format(value);
}

export function formatPercent(value: number): string {
  return `%${numberFormat.format(value)}`;
}

const ROLE_LABELS: Record<string, string> = { ADMIN: 'Yönetici', USER: 'Kullanıcı' };

/** An unknown role is shown as sent rather than hidden. */
export function roleLabel(role: string): string {
  return ROLE_LABELS[role] ?? role;
}

const LANGUAGE_LABELS: Record<string, string> = { tr: 'Türkçe', en: 'İngilizce' };

export function languageLabel(language: string): string {
  const label = LANGUAGE_LABELS[language.toLowerCase()];
  return label ? `${label} (${language})` : language;
}
