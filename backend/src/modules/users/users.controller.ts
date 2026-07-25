import { Body, Controller, Get, Patch, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { UpdatePreferencesDto } from './dto/update-preferences.dto';
import { UserProfileResponseDto } from './dto/user-profile-response.dto';

/**
 * Thin per CLAUDE.md's "business logic stays in use cases, not
 * controllers" rule. Guarded at the controller level, mirroring
 * `PlannerController`/`TravelController`. No `:id` routes at all — every
 * route here operates on `@CurrentUser()`'s own `sub` claim, never a
 * client-supplied id, so there is no foreign-resource case to 404 on (a
 * simpler authorization model than Planner/Travel's ownership checks,
 * since "me" makes accessing another user's data structurally
 * impossible, not just checked-and-rejected).
 */
@ApiTags('users')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get('me')
  @ApiOperation({ summary: "Get the caller's own profile" })
  @ApiOkResponse({ type: UserProfileResponseDto })
  getMe(@CurrentUser() user: JwtPayload): Promise<UserProfileResponseDto> {
    return this.usersService.getProfile(user.sub);
  }

  @Patch('me')
  @ApiOperation({ summary: "Update the caller's own profile (partial)" })
  @ApiOkResponse({ type: UserProfileResponseDto })
  updateMe(
    @CurrentUser() user: JwtPayload,
    @Body() dto: UpdateProfileDto,
  ): Promise<UserProfileResponseDto> {
    return this.usersService.updateProfile(user.sub, dto);
  }

  @Patch('preferences')
  @ApiOperation({ summary: "Update the caller's preferences (partial)" })
  @ApiOkResponse({ type: UserProfileResponseDto })
  updatePreferences(
    @CurrentUser() user: JwtPayload,
    @Body() dto: UpdatePreferencesDto,
  ): Promise<UserProfileResponseDto> {
    return this.usersService.updatePreferences(user.sub, dto);
  }
}
