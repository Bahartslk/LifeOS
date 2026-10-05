import { SetMetadata } from '@nestjs/common';
import { UserRole } from '@prisma/client';

export const ROLES_KEY = 'roles';

/**
 * Restricts a controller or handler to accounts holding one of `roles`,
 * enforced by `RolesGuard` (which must run after `JwtAuthGuard`). A
 * handler-level `@Roles` overrides a controller-level one.
 *
 * `RolesGuard` denies by default: a route it guards with no `@Roles` at
 * all rejects everyone. A route that does not use `RolesGuard` is not
 * role-restricted and ignores this decorator.
 */
export const Roles = (...roles: UserRole[]) => SetMetadata(ROLES_KEY, roles);
