import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class TravelTagsResultDto {
  @ApiProperty({ type: [String] })
  tags!: string[];
}

/** `POST /travel/ai/tags`'s response. */
export class TravelTagsResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: TravelTagsResultDto })
  result!: TravelTagsResultDto;
}
