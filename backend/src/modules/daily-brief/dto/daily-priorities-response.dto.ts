import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export class DailyPriorityDto {
  @ApiProperty()
  taskTitle!: string;

  @ApiProperty({
    description:
      '1-based ordinal (1 = do first) — a recommendation, never the stored `TaskPriority` field.',
  })
  suggestedRank!: number;

  @ApiProperty()
  reason!: string;
}

export class DailyPrioritiesResultDto {
  @ApiProperty({ type: [DailyPriorityDto] })
  priorities!: DailyPriorityDto[];
}

/** `POST /daily-brief/priorities`'s response — a ranking recommendation only; never modifies any task's stored priority. */
export class DailyPrioritiesResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: DailyPrioritiesResultDto })
  result!: DailyPrioritiesResultDto;
}
