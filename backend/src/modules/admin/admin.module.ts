import { Module } from '@nestjs/common';
import { RolesGuard } from '../../common/guards/roles.guard';
import { UsersModule } from '../users/users.module';
import { AdminController } from './admin.controller';
import { AdminService } from './admin.service';

/**
 * The admin panel's API, under `/api/v1/admin/...` (docs/15-api-design.md
 * #admin). This first version only exposes `GET /admin/session`; it exists
 * to carry the role-based access control every later admin route reuses.
 *
 * Imports `UsersModule` because `RolesGuard` resolves the caller's current
 * role through `UsersService`. `UsersModule` imports nothing, so this adds
 * no cycle.
 */
@Module({
  imports: [UsersModule],
  controllers: [AdminController],
  providers: [AdminService, RolesGuard],
})
export class AdminModule {}
