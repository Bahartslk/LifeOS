import { ApiProperty } from '@nestjs/swagger';
import { DevicePlatform } from '@prisma/client';
import { IsEnum, IsNotEmpty, IsString, MaxLength } from 'class-validator';

/** FCM registration tokens are ~150–200 characters; this leaves room without accepting junk. */
const MAX_TOKEN_LENGTH = 4096;

/** `PUT /notifications/devices`'s body. */
export class RegisterDeviceDto {
  @ApiProperty({ description: "The device's Firebase Cloud Messaging registration token." })
  @IsString()
  @IsNotEmpty()
  @MaxLength(MAX_TOKEN_LENGTH)
  token!: string;

  @ApiProperty({ enum: DevicePlatform, example: DevicePlatform.ANDROID })
  @IsEnum(DevicePlatform)
  platform!: DevicePlatform;
}

/** `DELETE /notifications/devices`'s body. */
export class UnregisterDeviceDto {
  @ApiProperty({ description: 'The FCM registration token to forget.' })
  @IsString()
  @IsNotEmpty()
  @MaxLength(MAX_TOKEN_LENGTH)
  token!: string;
}

export class DeviceResponseDto {
  @ApiProperty({ example: true })
  success!: boolean;
}
