import { ApiProperty } from '@nestjs/swagger';
import { TaskPriority } from '@prisma/client';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class PrioritySuggestionResultDto {
  @ApiProperty({ enum: TaskPriority })
  suggestedPriority!: TaskPriority;

  @ApiProperty()
  reason!: string;
}

/** `POST /planner/ai/priority-suggestion`'s response — a recommendation only; never writes to any `Task`. */
export class PrioritySuggestionResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: PrioritySuggestionResultDto })
  result!: PrioritySuggestionResultDto;
}
