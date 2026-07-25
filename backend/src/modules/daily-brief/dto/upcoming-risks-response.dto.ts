import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export class UpcomingRiskDto {
  @ApiProperty({ enum: ['DEADLINE', 'WORKLOAD', 'TRAVEL_CONFLICT', 'PREPARATION'] })
  category!: 'DEADLINE' | 'WORKLOAD' | 'TRAVEL_CONFLICT' | 'PREPARATION';

  @ApiProperty({ enum: ['LOW', 'MEDIUM', 'HIGH'] })
  severity!: 'LOW' | 'MEDIUM' | 'HIGH';

  @ApiProperty()
  recommendation!: string;

  @ApiProperty()
  reason!: string;
}

export class UpcomingRiskAnalysisResultDto {
  @ApiProperty({ type: [UpcomingRiskDto] })
  risks!: UpcomingRiskDto[];
}

/** `POST /daily-brief/risks`'s response — analyzes the next 7 days across Planner and Travel; changes nothing. */
export class UpcomingRiskAnalysisResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: UpcomingRiskAnalysisResultDto })
  result!: UpcomingRiskAnalysisResultDto;
}
