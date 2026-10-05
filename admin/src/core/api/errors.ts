/**
 * A response the backend answered with an error status. Its body follows
 * docs/15-api-design.md#error-handling: `{ statusCode, error, message,
 * path, timestamp }`, where `message` is a string or (for validation
 * errors) a list of strings.
 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    /** The backend's machine-readable `error` field, e.g. `FORBIDDEN`. */
    readonly code?: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

/** The request never produced a response: offline, DNS, blocked by CORS... */
export class NetworkError extends Error {
  constructor() {
    super('Network request failed');
    this.name = 'NetworkError';
  }
}

/** A 2xx response whose body is not the documented envelope. */
export class InvalidResponseError extends Error {
  constructor() {
    super('Unexpected response body');
    this.name = 'InvalidResponseError';
  }
}

export function isApiError(error: unknown, status?: number): error is ApiError {
  return error instanceof ApiError && (status === undefined || error.status === status);
}

/** A message safe and useful to show to an admin, in Turkish. */
export function describeError(error: unknown): string {
  if (error instanceof NetworkError) {
    return 'Sunucuya ulaşılamadı. Bağlantınızı kontrol edip tekrar deneyin.';
  }
  if (error instanceof ApiError) {
    if (error.status === 429)
      return 'Çok fazla istek gönderildi. Lütfen biraz bekleyip tekrar deneyin.';
    if (error.status === 404) return 'Aradığınız kayıt bulunamadı.';
    if (error.status === 403) return 'Bu işlem için yönetici yetkisi gerekiyor.';
    if (error.status === 401) return 'Oturumunuz sona erdi. Lütfen tekrar giriş yapın.';
    if (error.status >= 500) return 'Sunucuda bir hata oluştu. Lütfen daha sonra tekrar deneyin.';
    return 'İstek işlenemedi. Lütfen girdiğiniz değerleri kontrol edin.';
  }
  if (error instanceof InvalidResponseError) {
    return 'Sunucudan beklenmeyen bir yanıt alındı. Lütfen daha sonra tekrar deneyin.';
  }
  return 'Beklenmeyen bir hata oluştu.';
}
