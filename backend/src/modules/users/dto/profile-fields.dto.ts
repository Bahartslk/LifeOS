import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, IsUrl, MaxLength } from 'class-validator';

/** The non-preference fields `PATCH /users/me` allows — shared base for `UpdateProfileDto`, kept separate from `PreferenceFieldsDto` since these two never overlap (see `UpdateProfileDto`). */
export class ProfileFieldsDto {
  @ApiPropertyOptional({ example: 'Bahar Toslak', maxLength: 100 })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  displayName?: string;

  @ApiPropertyOptional({ description: 'External image URL — no upload/storage exists yet.' })
  @IsOptional()
  @IsUrl()
  avatarUrl?: string;

  @ApiPropertyOptional({ maxLength: 500 })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  bio?: string;
}
