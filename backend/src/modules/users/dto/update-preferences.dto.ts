import { PreferenceFieldsDto } from './preference-fields.dto';

/** `PATCH /users/preferences`'s body — all 4 preference fields, no profile fields (no `displayName`/`avatarUrl`/`bio`). */
export class UpdatePreferencesDto extends PreferenceFieldsDto {}
