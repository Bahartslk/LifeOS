import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';

/** Body shape for `POST /travel/ai/tags` — standalone fields (not an existing trip id), so tags can be generated during trip creation, before anything is saved — the same "works on a draft, not just a saved record" precedent `modules/planner/ai/dto/task-content-input.dto.ts` set for Smart Tags. */
export class TravelTagsInputDto {
  @ApiProperty({ example: 'Rome', maxLength: 100 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  destination!: string;

  @ApiPropertyOptional({ maxLength: 2000 })
  @IsOptional()
  @IsString()
  @MaxLength(2000)
  description?: string;
}
