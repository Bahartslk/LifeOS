import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

/**
 * Shared body shape for any AI capability endpoint that accepts a single
 * piece of free-form natural-language text — `POST /planner/ai/tasks/draft`,
 * `.../deadline-extraction`, and `POST /travel/ai/trips/draft`. Promoted
 * here in Sprint 21.5's architecture audit: Planner's and Travel's `ai/dto/`
 * folders each independently declared this identical `{text}` shape (Travel's
 * copy was deliberately flagged, at the time it was written, as a future
 * promotion candidate) — a single source of truth removes that duplication
 * per CLAUDE.md's "avoid duplicated code" rule, now that a second module
 * needs the identical shape.
 */
export class NaturalLanguageInputDto {
  @ApiProperty({
    example: 'Tomorrow at 9 AM dentist appointment',
    maxLength: 500,
    description: 'Free-form natural language to interpret.',
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(500)
  text!: string;
}
