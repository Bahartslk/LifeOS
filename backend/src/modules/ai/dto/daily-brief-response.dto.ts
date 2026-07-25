import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from './ai-capability-envelope.dto';

export class DailyBriefResultDto {
  @ApiProperty()
  greeting!: string;

  @ApiProperty()
  summary!: string;

  @ApiProperty({ type: [String] })
  focusItems!: string[];
}

/** `POST /ai/daily-brief`'s response. */
export class DailyBriefResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: DailyBriefResultDto })
  result!: DailyBriefResultDto;
}
