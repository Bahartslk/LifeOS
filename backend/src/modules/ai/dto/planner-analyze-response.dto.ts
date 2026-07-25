import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from './ai-capability-envelope.dto';

export class PlannerAnalyzeResultDto {
  @ApiProperty()
  summary!: string;

  @ApiProperty({ type: [String] })
  insights!: string[];

  @ApiProperty({ type: [String] })
  riskFlags!: string[];
}

/** `POST /ai/planner/analyze`'s response. */
export class PlannerAnalyzeResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: PlannerAnalyzeResultDto })
  result!: PlannerAnalyzeResultDto;
}
