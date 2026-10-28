import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';
import { DailyMotivationResultDto } from './daily-motivation-response.dto';
import { DailyPriorityDto } from './daily-priorities-response.dto';
import { DailySummaryResultDto } from './daily-summary-response.dto';

const NULL_WHEN_INVALID =
  "`null` when the AI's answer for this part was missing or invalid; the other parts are still returned.";

export class DailyOverviewResultDto {
  @ApiProperty({ type: DailySummaryResultDto, nullable: true, description: NULL_WHEN_INVALID })
  summary!: DailySummaryResultDto | null;

  @ApiProperty({
    type: [DailyPriorityDto],
    nullable: true,
    description: `Recommended order of today's tasks; never changes a stored priority. An empty array means there is nothing to rank today. ${NULL_WHEN_INVALID}`,
  })
  priorities!: DailyPriorityDto[] | null;

  @ApiProperty({ type: DailyMotivationResultDto, nullable: true, description: NULL_WHEN_INVALID })
  motivation!: DailyMotivationResultDto | null;
}

/** `POST /daily-brief/overview`'s response — summary, priorities and motivation from one AI call. Suggestions only. */
export class DailyOverviewResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: DailyOverviewResultDto })
  result!: DailyOverviewResultDto;
}
