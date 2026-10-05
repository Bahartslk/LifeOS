import { Controller, Get, Param, ParseUUIDPipe, Query, UseGuards } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { UserRole } from '@prisma/client';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { AdminService } from './admin.service';
import { AdminDashboardResponseDto } from './dto/admin-dashboard-response.dto';
import { AdminSessionResponseDto } from './dto/admin-session-response.dto';
import { AdminUserDetailResponseDto, PaginatedAdminUsersResponseDto } from './dto/admin-user.dto';
import { AdminUsersQueryDto } from './dto/admin-users-query.dto';

/**
 * Every route here requires an authenticated ADMIN: `JwtAuthGuard` proves
 * the token, then `RolesGuard` checks the caller's current role in the
 * database. A regular user gets 403, a missing/invalid token 401. The
 * guards and `@Roles` are declared once on the class, so a route added
 * here is protected by default.
 *
 * All routes are read-only and return account fields and aggregate counts
 * only — never the content of a user's tasks or trips.
 */
@ApiTags('admin')
@ApiBearerAuth()
@ApiUnauthorizedResponse({ description: 'Missing, invalid or expired access token.' })
@ApiForbiddenResponse({ description: 'The authenticated account is not an admin.' })
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
@Controller('admin')
export class AdminController {
  constructor(private readonly adminService: AdminService) {}

  @Get('session')
  @ApiOperation({ summary: "The authenticated admin's own session (id, email, name, role)" })
  @ApiOkResponse({ type: AdminSessionResponseDto })
  getSession(@CurrentUser() user: JwtPayload): Promise<AdminSessionResponseDto> {
    return this.adminService.getSession(user.sub);
  }

  @Get('dashboard')
  @ApiOperation({
    summary: 'System-wide aggregate counts of users, tasks and trips (no personal content)',
  })
  @ApiOkResponse({ type: AdminDashboardResponseDto })
  getDashboard(): Promise<AdminDashboardResponseDto> {
    return this.adminService.getDashboard();
  }

  @Get('users')
  @ApiOperation({
    summary: 'Paginated list of users, with search, role/status filters and sort',
  })
  @ApiOkResponse({ type: PaginatedAdminUsersResponseDto })
  @ApiBadRequestResponse({ description: 'Invalid pagination, filter or sort value.' })
  listUsers(@Query() query: AdminUsersQueryDto): Promise<PaginatedAdminUsersResponseDto> {
    return this.adminService.listUsers(query);
  }

  @Get('users/:id')
  @ApiOperation({
    summary:
      "One user's account summary with aggregate task and trip counts (also for a soft-deleted account)",
  })
  @ApiOkResponse({ type: AdminUserDetailResponseDto })
  @ApiBadRequestResponse({ description: 'id is not a UUID.' })
  @ApiNotFoundResponse({ description: 'No user with this id.' })
  getUser(@Param('id', ParseUUIDPipe) id: string): Promise<AdminUserDetailResponseDto> {
    return this.adminService.getUserDetail(id);
  }
}
