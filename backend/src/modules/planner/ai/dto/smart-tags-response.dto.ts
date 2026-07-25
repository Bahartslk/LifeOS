import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class SmartTagsResultDto {
  @ApiProperty({ type: [String] })
  tags!: string[];
}

/** `POST /planner/ai/tags`'s response. */
export class SmartTagsResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: SmartTagsResultDto })
  result!: SmartTagsResultDto;
}
