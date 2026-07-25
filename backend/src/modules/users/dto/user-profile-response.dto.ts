import { ApiProperty } from '@nestjs/swagger';
import { ThemeMode } from '@prisma/client';
import { PublicUserDto } from './public-user.dto';
import { NotificationPreferencesDto } from './notification-preferences.dto';

/**
 * `GET /users/me`'s response — extends `PublicUserDto` (id, email,
 * displayName, avatarUrl, createdAt — the same shape Auth's login response
 * already returns) rather than duplicating those fields, per this sprint's
 * explicit "Use PublicUserDto where appropriate" instruction. Adds the
 * profile/preference fields `PublicUserDto` deliberately doesn't carry
 * (Auth's login response should stay small; not every caller of
 * `PublicUserDto` needs `bio`/preferences). Never includes
 * `passwordHash`/`refreshTokens`/`deletedAt` — nothing here comes from
 * anywhere but `UsersService.toProfileResponse`'s explicit field-by-field
 * mapping, never a raw Prisma `User` spread.
 */
export class UserProfileResponseDto extends PublicUserDto {
  @ApiProperty({ nullable: true, type: String })
  bio!: string | null;

  @ApiProperty({ example: 'Europe/Istanbul' })
  timezone!: string;

  @ApiProperty({ example: 'tr' })
  language!: string;

  @ApiProperty({ enum: ThemeMode })
  themePreference!: ThemeMode;

  @ApiProperty({ type: NotificationPreferencesDto })
  notificationPreferences!: NotificationPreferencesDto;

  @ApiProperty()
  updatedAt!: Date;
}
