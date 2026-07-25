import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export enum OpportunityType {
  FREE_TIME = 'FREE_TIME',
  LOW_WORKLOAD = 'LOW_WORKLOAD',
  PREPARATION_COMPLETE = 'PREPARATION_COMPLETE',
  GOOD_TIME_TO_STUDY = 'GOOD_TIME_TO_STUDY',
  GOOD_TIME_TO_RELAX = 'GOOD_TIME_TO_RELAX',
  OTHER = 'OTHER',
}

/** A single positive opportunity — `POST /proactive-assistant/opportunities`'s result item. Unlike `SuggestionDto`, never expires and carries no priority/confidence: an opportunity is a observation about the caller's current situation, not an action to take. */
export class OpportunityDto {
  @ApiProperty()
  title!: string;

  @ApiProperty()
  description!: string;

  @ApiProperty({ enum: OpportunityType })
  type!: OpportunityType;

  @ApiProperty()
  reason!: string;
}

export class OpportunitiesResultDto {
  @ApiProperty({ type: [OpportunityDto] })
  opportunities!: OpportunityDto[];
}

/** `POST /proactive-assistant/opportunities`'s response — observations only; never persisted, never acted on automatically. */
export class OpportunitiesResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: OpportunitiesResultDto })
  result!: OpportunitiesResultDto;
}
