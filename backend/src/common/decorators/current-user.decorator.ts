import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { Request } from 'express';
import { JwtPayload } from '../strategies/jwt.strategy';

/**
 * Extracts the decoded JWT payload attached by JwtAuthGuard, so controllers
 * never read `request.user` directly. Returns only what the token carries
 * (the user id and token claims) — resolving that id to a full user record
 * is each consumer's own concern (`UsersService.findById`/`getProfile` do
 * this today for the routes that need it), not this decorator's.
 */
export const CurrentUser = createParamDecorator(
  (_data: unknown, ctx: ExecutionContext): JwtPayload => {
    const request = ctx.switchToHttp().getRequest<Request & { user: JwtPayload }>();
    return request.user;
  },
);
