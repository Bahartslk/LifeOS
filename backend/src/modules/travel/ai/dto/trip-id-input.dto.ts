import { ApiProperty } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';
import { AiCapabilityRequestDto } from '../../../ai/dto/ai-capability-request.dto';

/** Shared body shape for every Travel AI capability that operates on an existing trip (itinerary generation, budget analysis, packing list, risk analysis) — `AiCapabilityRequestDto`'s optional `notes` plus the trip to read. */
export class TripIdInputDto extends AiCapabilityRequestDto {
  @ApiProperty({ format: 'uuid', description: "Must be one of the caller's own trips." })
  @IsUUID()
  tripId!: string;
}
