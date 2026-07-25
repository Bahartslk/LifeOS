import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class OverloadedDayDto {
  @ApiProperty({ example: '2026-08-01' })
  date!: string;

  @ApiProperty()
  taskCount!: number;

  @ApiProperty()
  reason!: string;
}

export class ScheduleConflictDto {
  @ApiProperty()
  description!: string;

  @ApiProperty({ type: [String] })
  taskTitles!: string[];
}

export class ScheduleAnalysisResultDto {
  @ApiProperty({ type: [OverloadedDayDto] })
  overloadedDays!: OverloadedDayDto[];

  @ApiProperty({ type: [String] })
  freeDays!: string[];

  @ApiProperty({ type: [ScheduleConflictDto] })
  conflicts!: ScheduleConflictDto[];

  @ApiProperty()
  workloadBalance!: string;

  @ApiProperty({ type: [String] })
  suggestions!: string[];
}

/** `POST /planner/ai/schedule/analysis`'s response — read-only analysis of the caller's own Planner data; changes nothing. */
export class ScheduleAnalysisResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: ScheduleAnalysisResultDto })
  result!: ScheduleAnalysisResultDto;
}
