import { ApiPropertyOptional } from '@nestjs/swagger';
import { ThemeMode } from '@prisma/client';
import { Type } from 'class-transformer';
import { IsEnum, IsOptional, Matches, ValidateNested } from 'class-validator';
import { NotificationPreferencesDto } from './notification-preferences.dto';

/** ISO 639-1, optionally with a region subtag (e.g. "tr", "en-US") — not a fixed enum, per `User.language`'s doc comment. */
export const LANGUAGE_PATTERN = /^[a-z]{2}(-[A-Z]{2})?$/;

/** IANA "Area/Location" shape (e.g. "Europe/Istanbul") — not a fixed enum, per `User.timezone`'s doc comment. */
export const TIMEZONE_PATTERN = /^[A-Za-z_]+\/[A-Za-z_]+$/;

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

  @ApiPropertyOptional({ example: 'Europe/Istanbul', description: 'IANA time zone name.' })
  @IsOptional()
  @Matches(TIMEZONE_PATTERN, {
    message: 'timezone must be an IANA time zone name (e.g. "Europe/Istanbul").',
  })
  timezone?: string;

  @ApiPropertyOptional({ type: NotificationPreferencesDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => NotificationPreferencesDto)
  notificationPreferences?: NotificationPreferencesDto;
}
