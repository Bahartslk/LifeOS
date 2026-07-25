import { ApiProperty } from '@nestjs/swagger';
import { SuggestionDto } from './suggestion.dto';

/**
 * `POST /proactive-assistant/prioritize`'s response. Deliberately does NOT
 * extend `AiCapabilityEnvelopeDto` — unlike every other endpoint in this
 * module, prioritization is a deterministic algorithm over caller-supplied
 * data (see `prioritization/suggestion-prioritizer.util.ts`), not another AI
 * call, so there is no `provider`/`model`/`usage`/`latencyMs` to report.
 */
export class PrioritizeResponseDto {
  @ApiProperty({ type: [SuggestionDto], description: 'The final, ranked, deduplicated list.' })
  suggestions!: SuggestionDto[];

  @ApiProperty({
    description:
      'How many input suggestions were dropped as duplicates of a higher-ranked survivor.',
  })
  removedDuplicateCount!: number;

  @ApiProperty({
    description:
      'How many input suggestions were dropped as contradicting a higher-ranked survivor.',
  })
  removedContradictionCount!: number;
}
