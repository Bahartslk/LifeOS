import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { AccommodationType, ItineraryItemType, TransportationType } from '@prisma/client';
import {
  IsBoolean,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  Matches,
  MaxLength,
} from 'class-validator';
import { DATE_PATTERN, TIME_PATTERN } from '../../../common/utils/date-time.util';

export class CreateItineraryItemDto {
  @ApiProperty({ example: 'Balon Turu', maxLength: 200 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(200)
  title!: string;

  @ApiPropertyOptional({ maxLength: 2000 })
  @IsOptional()
  @IsString()
  @MaxLength(2000)
  description?: string;

  @ApiProperty({ example: '2026-09-11', description: 'Date-only, "YYYY-MM-DD".' })
  @Matches(DATE_PATTERN, { message: 'date must be in "YYYY-MM-DD" format.' })
  date!: string;

  @ApiPropertyOptional({ example: '05:30', description: 'Optional 24-hour "HH:mm".' })
  @IsOptional()
  @Matches(TIME_PATTERN, { message: 'startTime must be in 24-hour "HH:mm" format.' })
  startTime?: string;

  @ApiPropertyOptional({ example: '08:00', description: 'Optional 24-hour "HH:mm".' })
  @IsOptional()
  @Matches(TIME_PATTERN, { message: 'endTime must be in 24-hour "HH:mm" format.' })
  endTime?: string;

  @ApiPropertyOptional({ maxLength: 200 })
  @IsOptional()
  @IsString()
  @MaxLength(200)
  location?: string;

  // No `orderIndex`: new items are appended to the end of the trip's
  // itinerary automatically (see `ItineraryService.createItem`); reordering
  // is the dedicated `PATCH /travel/trips/:id/reorder` endpoint only, so
  // there's exactly one way to change order, not two.

  @ApiProperty({ enum: ItineraryItemType })
  @IsEnum(ItineraryItemType)
  type!: ItineraryItemType;

  @ApiPropertyOptional({
    enum: TransportationType,
    description: 'Only meaningful (and only validated) when type = TRANSPORTATION.',
  })
  @IsOptional()
  @IsEnum(TransportationType)
  transportationType?: TransportationType;

  @ApiPropertyOptional({
    enum: AccommodationType,
    description: 'Only meaningful (and only validated) when type = ACCOMMODATION.',
  })
  @IsOptional()
  @IsEnum(AccommodationType)
  accommodationType?: AccommodationType;

  @ApiPropertyOptional({
    default: false,
    description:
      'When true, also creates a linked Planner task (source = TRAVEL) for this item — per this sprint\'s "when an itinerary item requires a task" integration. See ItineraryService.',
  })
  @IsOptional()
  @IsBoolean()
  createTask?: boolean;
}
