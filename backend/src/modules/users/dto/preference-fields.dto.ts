import { ApiPropertyOptional } from '@nestjs/swagger';
import { ThemeMode } from '@prisma/client';
import { Type } from 'class-transformer';
import { IsEnum, IsOptional, Matches, MaxLength, ValidateNested } from 'class-validator';
import { IsIanaTimeZone } from '../../../common/validators/is-iana-time-zone.validator';
import { NotificationPreferencesDto } from './notification-preferences.dto';

/** ISO 639-1, optionally with a region subtag (e.g. "tr", "en-US") — not a fixed enum, per `User.language`'s doc comment. */
export const LANGUAGE_PATTERN = /^[a-z]{2}(-[A-Z]{2})?$/;

/** Longest accepted time zone name; real IANA names are well under this. */
export const TIMEZONE_MAX_LENGTH = 64;

/**
 * The 4 fields `PATCH /users/preferences` allows, shared with (a subset
 * of) `PATCH /users/me` via `PickType` in `UpdateProfileDto` — a single
 * source of truth for each field's validation, per CLAUDE.md's "avoid
 * duplicated validation" rule, rather than copy-pasting these decorators
 * into two DTOs.
 */
export class PreferenceFieldsDto {
  @ApiPropertyOptional({ enum: ThemeMode })
  @IsOptional()
  @IsEnum(ThemeMode)
  themePreference?: ThemeMode;

  @ApiPropertyOptional({
    example: 'tr',
    description: 'ISO 639-1, optionally "-" + region (e.g. "en-US").',
  })
  @IsOptional()
  @Matches(LANGUAGE_PATTERN, {
    message: 'language must be an ISO 639-1 code, optionally with a region (e.g. "tr", "en-US").',
  })
  language?: string;

  @ApiPropertyOptional({
    example: 'Europe/Istanbul',
    maxLength: TIMEZONE_MAX_LENGTH,
    description:
      'Any IANA time zone name the server knows, including aliases such as "UTC", "Etc/UTC" and "GMT". UTC offsets (e.g. "+03:00") are rejected. Stored as sent.',
  })
  @IsOptional()
  @MaxLength(TIMEZONE_MAX_LENGTH)
  @IsIanaTimeZone()
  timezone?: string;

  @ApiPropertyOptional({ type: NotificationPreferencesDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => NotificationPreferencesDto)
  notificationPreferences?: NotificationPreferencesDto;
}
