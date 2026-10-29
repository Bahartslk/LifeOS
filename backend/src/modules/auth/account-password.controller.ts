import { Body, Controller, HttpCode, HttpStatus, Patch, UseGuards } from '@nestjs/common';
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
import { AuthService } from './auth.service';
import { AuthTokensDto } from './dto/auth-tokens.dto';
import { ChangePasswordDto } from './dto/change-password.dto';

/**
 * `PATCH /users/me/password`. The path sits under `users` (docs/15-api-design.md),
 * but the work is sessions and tokens, which belong to `AuthModule`; living
 * here keeps `UsersModule` free of a dependency on `AuthModule` (which
 * already depends on it).
 */
@ApiTags('users')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('users')
export class AccountPasswordController {
  constructor(private readonly authService: AuthService) {}

  @Patch('me/password')
  @HttpCode(HttpStatus.OK)
  @Throttle({ default: { ttl: THROTTLE_WINDOW_MS, limit: throttleLimits.passwordChange } })
  @ApiOperation({
    summary:
      "Change the caller's password. Signs out every session and returns a new token pair, so only the calling device stays signed in",
  })
  @ApiOkResponse({ type: AuthTokensDto })
  @ApiBadRequestResponse({
    description:
      'Missing fields, a new password outside 8–72 characters, or a new password equal to the current one.',
  })
  @ApiForbiddenResponse({
    description:
      'Current password is incorrect. Nothing changes. (403, not 401: the session is valid.)',
  })
  @ApiNotFoundResponse({ description: 'The account no longer exists.' })
  @ApiTooManyRequestsResponse({ description: 'Too many attempts from this IP.' })
  changePassword(
    @CurrentUser() user: JwtPayload,
    @Body() dto: ChangePasswordDto,
  ): Promise<AuthTokensDto> {
    return this.authService.changePassword(user.sub, dto);
  }
}
