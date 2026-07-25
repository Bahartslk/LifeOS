import { ApiProperty } from '@nestjs/swagger';
import { ArrayMinSize, ArrayUnique, IsArray, IsUUID } from 'class-validator';

/**
 * `PATCH /travel/trips/:id/reorder`'s body — the full new order as an
 * ordered array of the trip's itinerary item ids; array position becomes
 * the new `orderIndex` (0-based). `ItineraryService.reorder` requires this
 * array to contain exactly the trip's current item ids (no more, no
 * fewer) — a well-defined, unambiguous full-list reorder, the common
 * convention for drag-and-drop reorder endpoints.
 */
export class ReorderItineraryDto {
  @ApiProperty({
    type: [String],
    description: 'Every itinerary item id belonging to this trip, in the new desired order.',
  })
  @IsArray()
  @ArrayMinSize(1)
  @ArrayUnique()
  @IsUUID(undefined, { each: true })
  itemIds!: string[];
}
