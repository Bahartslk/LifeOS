import { ApiProperty } from '@nestjs/swagger';
import { TaskPriority } from '@prisma/client';
import { AiCapabilityEnvelopeDto } from './ai-capability-envelope.dto';

export class TaskSuggestionDto {
  @ApiProperty()
  title!: string;

  @ApiProperty()
  reason!: string;

  /** The model's own priority judgment — a suggestion only; never written to a real `Task` by this endpoint (this sprint's "AI never owns business entities" principle). */
  @ApiProperty({ enum: TaskPriority })
  priority!: TaskPriority;
}

export class PlannerSuggestResultDto {
  @ApiProperty({ type: [TaskSuggestionDto] })
  suggestions!: TaskSuggestionDto[];
}

/** `POST /ai/planner/suggest`'s response. */
export class PlannerSuggestResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: PlannerSuggestResultDto })
  result!: PlannerSuggestResultDto;
}
