import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class DeadlineExtractionResultDto {
  @ApiProperty({ example: '2026-08-01', nullable: true, type: String })
  date!: string | null;

  @ApiProperty({ example: '09:00', nullable: true, type: String })
  time!: string | null;

  @ApiProperty()
  isAllDay!: boolean;

  @ApiProperty({ enum: ['LOW', 'MEDIUM', 'HIGH'] })
  confidence!: 'LOW' | 'MEDIUM' | 'HIGH';

  @ApiProperty({ description: 'The portion of the input text the date/time was extracted from.' })
  originalPhrase!: string;
}

/** `POST /planner/ai/deadline-extraction`'s response. */
export class DeadlineExtractionResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: DeadlineExtractionResultDto })
  result!: DeadlineExtractionResultDto;
}
