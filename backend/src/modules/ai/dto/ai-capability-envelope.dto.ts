import { ApiProperty } from '@nestjs/swagger';

export class AiUsageDto {
  @ApiProperty() inputTokens!: number;
  @ApiProperty() outputTokens!: number;
  @ApiProperty() totalTokens!: number;
}

/**
 * The shared envelope fields every capability response DTO extends —
 * `provider`/`model`/`usage`/`latencyMs`, exactly this sprint's
 * "Structured Responses" example shape minus `result` (each concrete
 * subclass types `result` to its own capability's JSON shape). Reusing
 * this base instead of repeating these four fields on every response DTO
 * follows the same `PickType`/`IntersectionType`/inheritance convention
 * already established across Auth/Planner/Travel/Users DTOs.
 */
export abstract class AiCapabilityEnvelopeDto {
  @ApiProperty({
    description:
      'The provider id that actually produced this result (e.g. "gemini", "openrouter") — never a provider-specific response object.',
  })
  provider!: string;

  @ApiProperty()
  model!: string;

  @ApiProperty({ type: AiUsageDto })
  usage!: AiUsageDto;

  @ApiProperty()
  latencyMs!: number;
}
