import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export class DailyMotivationResultDto {
  @ApiProperty({ maxLength: 400 })
  message!: string;
}

/** `POST /daily-brief/motivation`'s response. */
export class DailyMotivationResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: DailyMotivationResultDto })
  result!: DailyMotivationResultDto;
}
