import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export class DailySummaryResultDto {
  @ApiProperty({ type: [String] })
  todayPriorities!: string[];

  @ApiProperty({ type: [String] })
  keyEvents!: string[];

  @ApiProperty()
  suggestedFocus!: string;
}

/** `POST /daily-brief/summary`'s response — a suggestion only; changes nothing in Planner or Travel. */
export class DailySummaryResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: DailySummaryResultDto })
  result!: DailySummaryResultDto;
}
