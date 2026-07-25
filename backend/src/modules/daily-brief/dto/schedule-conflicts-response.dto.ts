import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../ai/dto/ai-capability-envelope.dto';

export class OverlappingEventDto {
  @ApiProperty()
  description!: string;

  @ApiProperty({ type: [String] })
  items!: string[];
}

export class ConflictOverloadedDayDto {
  @ApiProperty({ example: '2026-08-01' })
  date!: string;

  @ApiProperty()
  reason!: string;
}

export class ScheduleConflictsResultDto {
  @ApiProperty({ type: [OverlappingEventDto] })
  overlappingEvents!: OverlappingEventDto[];

  @ApiProperty({ type: [String] })
  impossibleSchedules!: string[];

  @ApiProperty({ type: [String] })
  insufficientTravelTime!: string[];

  @ApiProperty({ type: [ConflictOverloadedDayDto] })
  overloadedDays!: ConflictOverloadedDayDto[];

  @ApiProperty({ type: [String] })
  recommendations!: string[];
}

/** `POST /daily-brief/conflicts`'s response — read-only cross-module analysis; changes nothing. */
export class ScheduleConflictsResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: ScheduleConflictsResultDto })
  result!: ScheduleConflictsResultDto;
}
