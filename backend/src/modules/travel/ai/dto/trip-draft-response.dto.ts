import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class BudgetEstimateDto {
  @ApiProperty({ nullable: true, type: Number })
  estimate!: number | null;

  @ApiProperty()
  currency!: string;

  @ApiProperty({ nullable: true, type: String })
  uncertaintyNote!: string | null;
}

export class TripDraftResultDto {
  @ApiProperty()
  destination!: string;

  @ApiProperty({ nullable: true, type: String })
  country!: string | null;

  @ApiProperty({ example: '2026-10-10', nullable: true, type: String })
  startDate!: string | null;

  @ApiProperty({ example: '2026-10-15', nullable: true, type: String })
  endDate!: string | null;

  @ApiProperty({ nullable: true, type: Number })
  estimatedDurationDays!: number | null;

  @ApiProperty()
  travelPurpose!: string;

  @ApiProperty({ type: BudgetEstimateDto, nullable: true })
  estimatedBudgetRange!: BudgetEstimateDto | null;

  @ApiProperty()
  suggestedTravelStyle!: string;

  @ApiProperty({ enum: ['LOW', 'MEDIUM', 'HIGH'] })
  confidence!: 'LOW' | 'MEDIUM' | 'HIGH';
}

/** `POST /travel/ai/trips/draft`'s response — a draft only; never persisted by this endpoint. */
export class TripDraftResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: TripDraftResultDto })
  result!: TripDraftResultDto;
}
