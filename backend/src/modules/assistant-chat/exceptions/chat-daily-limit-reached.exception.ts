import { HttpException, HttpStatus } from '@nestjs/common';

export const CHAT_DAILY_LIMIT_REACHED_CODE = 'CHAT_DAILY_LIMIT_REACHED';

/**
 * The user has used all of today's chat messages. A `429` like the AI
 * provider's own quota error, but with `code: "CHAT_DAILY_LIMIT_REACHED"` in
 * the error body so a client can tell the two apart.
 */
export class ChatDailyLimitReachedException extends HttpException {
  constructor(dailyLimit: number) {
    super(
      {
        code: CHAT_DAILY_LIMIT_REACHED_CODE,
        message: `Daily chat limit of ${dailyLimit} messages reached. Try again tomorrow.`,
      },
      HttpStatus.TOO_MANY_REQUESTS,
    );
  }
}
