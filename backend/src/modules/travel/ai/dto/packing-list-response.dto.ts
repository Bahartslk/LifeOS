import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class PackingCategoryDto {
  @ApiProperty({ example: 'Documents' })
  category!: string;

  @ApiProperty({ type: [String] })
  items!: string[];
}

export class PackingListResultDto {
  @ApiProperty({ type: [PackingCategoryDto] })
  categories!: PackingCategoryDto[];
}

/** `POST /travel/ai/packing-list`'s response. */
export class PackingListResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: PackingListResultDto })
  result!: PackingListResultDto;
}
