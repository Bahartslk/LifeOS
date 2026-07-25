import { IntersectionType, PickType } from '@nestjs/swagger';
import { ProfileFieldsDto } from './profile-fields.dto';
import { PreferenceFieldsDto } from './preference-fields.dto';

/**
 * `PATCH /users/me`'s body — `displayName`/`avatarUrl`/`bio` plus
 * `themePreference`/`language`/`timezone` (picked from
 * `PreferenceFieldsDto`, per Sprint 17.0's explicit allow-list — note
 * `notificationPreferences` is deliberately excluded here, unlike
 * `UpdatePreferencesDto` which allows it). No `email`, no password, no
 * security field: this class simply never declares them, and the global
 * `ValidationPipe`'s `forbidNonWhitelisted` rejects any such field in the
 * request body with a 400 — the same "don't declare it, whitelist rejects
 * it" pattern `CreateTaskDto` already established for `source`.
 */
export class UpdateProfileDto extends IntersectionType(
  ProfileFieldsDto,
  PickType(PreferenceFieldsDto, ['themePreference', 'language', 'timezone'] as const),
) {}
