import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { UserRole } from '@prisma/client';
import { Request } from 'express';
import { UsersService } from '../../modules/users/users.service';
import { ROLES_KEY } from '../decorators/roles.decorator';
import { JwtPayload } from '../strategies/jwt.strategy';

/**
 * Role-based authorization, per docs/15-api-design.md#authorization. Runs
 * after `JwtAuthGuard` on routes marked with `@Roles(...)`.
 *
 * The decision is made from the caller's CURRENT role in the database,
 * never from the `role` claim inside the access token: that claim is only
 * a hint for clients, and a token stays valid for up to its TTL after an
 * account is demoted or deleted. One indexed primary-key lookup per
 * role-restricted request buys immediate revocation.
 *
 * - no authenticated user on the request, or the account no longer exists
 *   (soft-deleted) -> 401
 * - account exists but its role is not one of the required roles -> 403
 *
 * Deny by default: if this guard is applied but no `@Roles(...)` is
 * declared on the handler or its controller, every request is rejected
 * with 403. Putting the guard on a route is a statement that the route is
 * role-restricted, so a forgotten `@Roles` must fail closed rather than
 * silently open the route to every authenticated user.
 *
 * A module using this guard must import `UsersModule`.
 */
@Injectable()
export class RolesGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly usersService: UsersService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const requiredRoles = this.reflector.getAllAndOverride<UserRole[] | undefined>(ROLES_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (!requiredRoles || requiredRoles.length === 0) {
      throw new ForbiddenException('You do not have permission to access this resource.');
    }

    const request = context.switchToHttp().getRequest<Request & { user?: JwtPayload }>();
    const userId = request.user?.sub;
    if (!userId) {
      throw new UnauthorizedException();
    }

    const user = await this.usersService.findById(userId);
    if (!user) {
      throw new UnauthorizedException('Account no longer exists.');
    }

    if (!requiredRoles.includes(user.role)) {
      throw new ForbiddenException('You do not have permission to access this resource.');
    }

    return true;
  }
}
