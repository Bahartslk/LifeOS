import { Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

/**
 * Generic authentication guard: proves the caller presented a valid JWT.
 * Per docs/15-api-design.md#authorization, ownership checks (does this user
 * own this specific resource) are deliberately NOT a second guard — Planner
 * and Travel each enforce ownership directly in their service and
 * repository layers (`WHERE id = ? AND userId = ?`, 404 rather than 403 for
 * a foreign resource), since a generic resource-aware guard would need to
 * know each feature's entity shape anyway. This guard has no knowledge of
 * Users, Trips, or Tasks.
 */
@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {}
