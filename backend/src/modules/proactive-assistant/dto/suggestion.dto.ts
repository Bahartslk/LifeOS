import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import {
  IsArray,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  Matches,
  MaxLength,
  ValidateNested,
} from 'class-validator';
import { DATE_PATTERN } from '../../../common/utils/date-time.util';

export enum SuggestionPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

export enum SuggestionConfidence {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

/**
 * A single proactive suggestion — `POST /proactive-assistant/suggestions`'s
 * result item, and also `POST /proactive-assistant/prioritize`'s request
 * and response item shape (the same object a caller gets back from
 * `suggestions` is what it later sends to `prioritize`). Never persisted;
 * never triggers an action on its own — this module is recommendation-only.
 */
export class SuggestionDto {
  @ApiProperty({ maxLength: 200 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(200)
  title!: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  description!: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  reason!: string;

  @ApiProperty({ enum: SuggestionPriority })
  @IsEnum(SuggestionPriority)
  priority!: SuggestionPriority;

  @ApiProperty({ enum: SuggestionConfidence })
  @IsEnum(SuggestionConfidence)
  confidence!: SuggestionConfidence;

  @ApiProperty({
    nullable: true,
    example: '2026-08-01',
    description:
      'The date after which this suggestion is no longer relevant (e.g. the day of the trip it prepares for). `null` when nothing bounds its relevance.',
  })
  @IsOptional()
  @Matches(DATE_PATTERN)
  expiresAt!: string | null;
}

/** `POST /proactive-assistant/prioritize`'s request body — typically the `suggestions` array returned by `POST /proactive-assistant/suggestions`, sent back for ranking/deduplication. */
export class PrioritizeRequestDto {
  @ApiProperty({ type: [SuggestionDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SuggestionDto)
  suggestions!: SuggestionDto[];
}
