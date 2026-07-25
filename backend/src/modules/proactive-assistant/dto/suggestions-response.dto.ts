import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';
import { SuggestionDto } from './suggestion.dto';

export class SuggestionsResultDto {
  @ApiProperty({ type: [SuggestionDto] })
  suggestions!: SuggestionDto[];
}

/** `POST /proactive-assistant/suggestions`'s response — suggestions only; never persisted, never acted on automatically. */
export class SuggestionsResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: SuggestionsResultDto })
  result!: SuggestionsResultDto;
}
