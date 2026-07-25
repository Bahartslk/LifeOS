import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class ItineraryDayDto {
  @ApiProperty()
  day!: number;

  @ApiProperty({ example: '2026-10-10', nullable: true, type: String })
  date!: string | null;

  @ApiProperty()
  morning!: string;

  @ApiProperty()
  afternoon!: string;

  @ApiProperty()
  evening!: string;
}

export class ItineraryGenerationResultDto {
  @ApiProperty({ type: [ItineraryDayDto] })
  days!: ItineraryDayDto[];
}

/** `POST /travel/ai/itinerary`'s response — suggestions only; never persisted as real `ItineraryItem`s by this endpoint. */
export class ItineraryGenerationResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: ItineraryGenerationResultDto })
  result!: ItineraryGenerationResultDto;
}
