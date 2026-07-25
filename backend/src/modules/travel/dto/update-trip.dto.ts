import { ApiPropertyOptional, PartialType } from '@nestjs/swagger';
import { TripStatus } from '@prisma/client';
import { IsEnum, IsOptional } from 'class-validator';
import { CreateTripDto } from './create-trip.dto';

/** Every `CreateTripDto` field, optional (PATCH semantics) — via `PartialType`, per CLAUDE.md's "avoid duplicated code" rule. Adds `status`, which `CreateTripDto` deliberately omits (every trip is created `PLANNED`). */
export class UpdateTripDto extends PartialType(CreateTripDto) {
  @ApiPropertyOptional({ enum: TripStatus })
  @IsOptional()
  @IsEnum(TripStatus)
  status?: TripStatus;
}
