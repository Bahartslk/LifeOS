import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, MaxLength } from 'class-validator';

/** Shared body shape for all 4 capability endpoints — deliberately minimal, per this sprint's "avoid feature creep" rule; every capability's real input is the caller's own Planner/Travel data via `ContextBuilder`, not a request payload. */
export class AiCapabilityRequestDto {
  @ApiPropertyOptional({
    maxLength: 500,
    description:
      'Optional extra focus/instruction for this request, e.g. "focus on work tasks only".',
  })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  notes?: string;
}
