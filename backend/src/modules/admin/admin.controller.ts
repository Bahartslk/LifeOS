import { Controller, Get, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiForbiddenResponse,
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
import { AdminSessionResponseDto } from './dto/admin-session-response.dto';

/**
 * Every route here requires an authenticated ADMIN: `JwtAuthGuard` proves
 * the token, then `RolesGuard` checks the caller's current role in the
 * database. A regular user gets 403, a missing/invalid token 401.
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
}
