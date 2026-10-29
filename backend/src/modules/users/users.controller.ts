import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  HttpStatus,
  Patch,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiTooManyRequestsResponse,
} from '@nestjs/swagger';
import { Throttle } from '@nestjs/throttler';
import { THROTTLE_WINDOW_MS, throttleLimits } from '../../config/throttle.config';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { UsersService } from './users.service';
import { DeleteAccountDto } from './dto/delete-account.dto';
import { DeleteAccountResponseDto } from './dto/delete-account-response.dto';
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

  @Delete('me')
  @HttpCode(HttpStatus.OK)
  @Throttle({ default: { ttl: THROTTLE_WINDOW_MS, limit: throttleLimits.accountDeletion } })
  @ApiOperation({
    summary:
      "Permanently delete the caller's account and all of its data (tasks, trips, sessions, AI chat usage), after confirming the password",
  })
  @ApiOkResponse({ type: DeleteAccountResponseDto })
  @ApiBadRequestResponse({ description: 'Missing or empty password.' })
  @ApiForbiddenResponse({
    description: 'Password is incorrect. Nothing is deleted. (403, not 401: the session is valid.)',
  })
  @ApiNotFoundResponse({ description: 'The account no longer exists.' })
  @ApiTooManyRequestsResponse({ description: 'Too many deletion attempts from this IP.' })
  deleteMe(
    @CurrentUser() user: JwtPayload,
    @Body() dto: DeleteAccountDto,
  ): Promise<DeleteAccountResponseDto> {
    return this.usersService.deleteAccount(user.sub, dto.password);
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
