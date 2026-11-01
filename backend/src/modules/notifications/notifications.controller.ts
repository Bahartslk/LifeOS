import { Body, Controller, Delete, HttpCode, HttpStatus, Put, UseGuards } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { DeviceResponseDto, RegisterDeviceDto, UnregisterDeviceDto } from './dto/device.dto';
import { NotificationsService } from './notifications.service';

/** Push notification devices. Thin: every rule is in `NotificationsService`. */
@ApiTags('notifications')
@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  @Put('devices')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      "Register this device's FCM token for the caller's push notifications (idempotent; moves the token if another user had it)",
  })
  @ApiOkResponse({ type: DeviceResponseDto })
  @ApiBadRequestResponse({ description: 'Missing token or unknown platform.' })
  @ApiUnauthorizedResponse({ description: 'Missing or invalid access token.' })
  async registerDevice(
    @CurrentUser() user: JwtPayload,
    @Body() dto: RegisterDeviceDto,
  ): Promise<DeviceResponseDto> {
    await this.notificationsService.registerDevice(user.sub, dto.token, dto.platform);
    return { success: true };
  }

  @Delete('devices')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      'Forget an FCM token (at sign-out). No access token needed, so a device whose session already expired can still unregister; the answer is the same whether or not the token was known',
  })
  @ApiOkResponse({ type: DeviceResponseDto })
  @ApiBadRequestResponse({ description: 'Missing token.' })
  async unregisterDevice(@Body() dto: UnregisterDeviceDto): Promise<DeviceResponseDto> {
    await this.notificationsService.unregisterDevice(dto.token);
    return { success: true };
  }
}
