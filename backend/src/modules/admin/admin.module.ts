import { Module } from '@nestjs/common';
import { RolesGuard } from '../../common/guards/roles.guard';
import { UsersModule } from '../users/users.module';
import { AdminController } from './admin.controller';
import { AdminService } from './admin.service';
import { AdminRepository } from './repositories/admin.repository';

/**
 * The admin panel's API, under `/api/v1/admin/...` (docs/15-api-design.md
 * #admin): `session`, `dashboard`, `users` and `users/:id`. All read-only,
 * all behind `JwtAuthGuard` + `RolesGuard` + `@Roles(ADMIN)`.
 *
 * Imports `UsersModule` because `RolesGuard` resolves the caller's current
 * role through `UsersService`. `UsersModule` imports nothing, so this adds
 * no cycle. `AdminRepository` holds the module's cross-user read queries;
 * `PrismaService` comes from the global `PrismaModule`.
 */
@Module({
  imports: [UsersModule],
  controllers: [AdminController],
  providers: [AdminService, AdminRepository, RolesGuard],
})
export class AdminModule {}
