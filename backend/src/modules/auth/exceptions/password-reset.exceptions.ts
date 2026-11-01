import { HttpException, HttpStatus } from '@nestjs/common';

export const INVALID_RESET_CODE = 'INVALID_RESET_CODE';
export const PASSWORD_RESET_EMAIL_LIMIT = 'PASSWORD_RESET_EMAIL_LIMIT';

/**
 * The email/code pair doesn't match a usable code: wrong code, expired,
 * already used, too many wrong tries, or no such account. One answer for
 * all of them, so nothing is revealed about the account.
 */
export class InvalidResetCodeException extends HttpException {
  constructor() {
    super(
      { code: INVALID_RESET_CODE, message: 'The code is invalid or has expired.' },
      HttpStatus.BAD_REQUEST,
    );
  }
}

/** Too many reset codes requested for this email address in the last hour. */
export class PasswordResetEmailLimitException extends HttpException {
  constructor() {
    super(
      {
        code: PASSWORD_RESET_EMAIL_LIMIT,
        message: 'Too many reset codes requested for this email. Try again later.',
      },
      HttpStatus.TOO_MANY_REQUESTS,
    );
  }
}
