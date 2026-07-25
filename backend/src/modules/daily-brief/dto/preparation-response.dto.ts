import { ApiProperty } from '@nestjs/swagger';
import { TaskPriority } from '@prisma/client';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export class PreparationItemDto {
  @ApiProperty()
  title!: string;

  @ApiProperty()
  reason!: string;

  @ApiProperty({ enum: TaskPriority })
  priority!: TaskPriority;

  @ApiProperty({ enum: ['PLANNER', 'TRAVEL', 'BOTH'] })
  relatedTo!: 'PLANNER' | 'TRAVEL' | 'BOTH';
}

export class PreparationResultDto {
  @ApiProperty({ type: [PreparationItemDto] })
  items!: PreparationItemDto[];
}

/** `POST /daily-brief/preparation`'s response — suggestions only; never persisted. */
export class PreparationResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: PreparationResultDto })
  result!: PreparationResultDto;
}
