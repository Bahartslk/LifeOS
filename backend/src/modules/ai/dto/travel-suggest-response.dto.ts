import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from './ai-capability-envelope.dto';

export class TravelSuggestionDto {
  @ApiProperty()
  title!: string;

  @ApiProperty()
  description!: string;

  /** A free-text category (e.g. "activity", "meal") — not `ItineraryItemType`: this is a suggestion only, never written to a real `ItineraryItem` by this endpoint. */
  @ApiProperty()
  type!: string;
}

export class TravelSuggestResultDto {
  @ApiProperty({ type: [TravelSuggestionDto] })
  suggestions!: TravelSuggestionDto[];
}

/** `POST /ai/travel/suggest`'s response. */
export class TravelSuggestResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: TravelSuggestResultDto })
  result!: TravelSuggestResultDto;
}
