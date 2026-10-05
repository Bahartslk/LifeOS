const DEFAULT_API_BASE_URL = 'http://localhost:3000';

/** Per docs/15-api-design.md#versioning. */
export const API_PREFIX = '/api/v1';

/** The backend origin, without a trailing slash and without the API prefix. */
export function resolveApiBaseUrl(value: string | undefined): string {
  const trimmed = value?.trim();
  return (trimmed ? trimmed : DEFAULT_API_BASE_URL).replace(/\/+$/, '');
}

export const API_BASE_URL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL);
