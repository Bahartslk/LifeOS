import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class OverlyBusyDayDto {
  @ApiProperty({ example: '2026-10-12' })
  date!: string;

  @ApiProperty()
  itemCount!: number;

  @ApiProperty()
  reason!: string;
}

export class UnrealisticScheduleDto {
  @ApiProperty()
  description!: string;

  @ApiProperty({ type: [String] })
  itemTitles!: string[];
}

export class RiskAnalysisResultDto {
  @ApiProperty({ type: [OverlyBusyDayDto] })
  overlyBusyDays!: OverlyBusyDayDto[];

  @ApiProperty({ type: [UnrealisticScheduleDto] })
  unrealisticSchedules!: UnrealisticScheduleDto[];

  @ApiProperty({ type: [String] })
  missingTransportation!: string[];

  @ApiProperty({ type: [String] })
  budgetRisks!: string[];

  @ApiProperty({ type: [String] })
  missingPreparation!: string[];

  @ApiProperty({ type: [String] })
  recommendations!: string[];
}

/** `POST /travel/ai/risk-analysis`'s response — read-only analysis of the caller's own trip + itinerary; changes nothing. */
export class RiskAnalysisResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: RiskAnalysisResultDto })
  result!: RiskAnalysisResultDto;
}
