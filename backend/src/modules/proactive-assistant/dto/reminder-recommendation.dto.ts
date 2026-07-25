import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export enum ReminderRelatedTo {
  PLANNER = 'PLANNER',
  TRAVEL = 'TRAVEL',
  BOTH = 'BOTH',
}

/**
 * A recommended reminder — `POST /proactive-assistant/reminders`'s result
 * item. This module never creates, schedules, or sends a reminder itself
 * (there is no reminder entity or notification channel in this codebase to
 * write to); it only recommends one the caller can act on.
 */
export class ReminderRecommendationDto {
  @ApiProperty()
  title!: string;

  @ApiProperty()
  reason!: string;

  @ApiProperty({ enum: ReminderRelatedTo })
  relatedTo!: ReminderRelatedTo;

  @ApiProperty({
    nullable: true,
    example: '2026-08-01',
    description:
      'The date the reminder should fire on/before, e.g. the day before a flight or deadline. `null` when no specific date applies.',
  })
  recommendedDate!: string | null;
}

export class RemindersResultDto {
  @ApiProperty({ type: [ReminderRecommendationDto] })
  reminders!: ReminderRecommendationDto[];
}

/** `POST /proactive-assistant/reminders`'s response — recommendations only; this module never creates or schedules a reminder itself. */
export class RemindersResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: RemindersResultDto })
  result!: RemindersResultDto;
}
