import { ApiProperty } from '@nestjs/swagger';
import { AccommodationType, ItineraryItemType, TransportationType } from '@prisma/client';

export class ItineraryItemResponseDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty({ format: 'uuid' })
  tripId!: string;

  @ApiProperty()
  title!: string;

  @ApiProperty({ nullable: true, type: String })
  description!: string | null;

  @ApiProperty({ example: '2026-09-11' })
  date!: string;

  @ApiProperty({ example: '05:30', nullable: true, type: String })
  startTime!: string | null;

  @ApiProperty({ example: '08:00', nullable: true, type: String })
  endTime!: string | null;

  @ApiProperty({ nullable: true, type: String })
  location!: string | null;

  @ApiProperty()
  orderIndex!: number;

  @ApiProperty({ enum: ItineraryItemType })
  type!: ItineraryItemType;

  @ApiProperty({ enum: TransportationType, nullable: true })
  transportationType!: TransportationType | null;

  @ApiProperty({ enum: AccommodationType, nullable: true })
  accommodationType!: AccommodationType | null;

  @ApiProperty({
    format: 'uuid',
    nullable: true,
    type: String,
    description:
      "The linked Planner task's id, if this item was created/updated with createTask: true.",
  })
  taskId!: string | null;

  @ApiProperty()
  createdAt!: Date;

  @ApiProperty()
  updatedAt!: Date;
}
