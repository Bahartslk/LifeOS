import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export enum InsightCategory {
  PATTERN = 'PATTERN',
  WORKLOAD = 'WORKLOAD',
  TRAVEL = 'TRAVEL',
  SCHEDULING = 'SCHEDULING',
  GENERAL = 'GENERAL',
}

/** A single short, actionable observation — `POST /proactive-assistant/insights`'s result item, e.g. "You usually schedule important work too close to travel." */
export class InsightDto {
  @ApiProperty({ maxLength: 300 })
  insight!: string;

  @ApiProperty({ enum: InsightCategory })
  category!: InsightCategory;
}

export class InsightsResultDto {
  @ApiProperty({ type: [InsightDto] })
  insights!: InsightDto[];
}

/** `POST /proactive-assistant/insights`'s response — suggestions only; never persisted. */
export class InsightsResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: InsightsResultDto })
  result!: InsightsResultDto;
}
