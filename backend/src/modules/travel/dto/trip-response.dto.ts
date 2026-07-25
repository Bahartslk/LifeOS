import { ApiProperty } from '@nestjs/swagger';
import { TripStatus } from '@prisma/client';

/** The only shape a `Trip` is ever sent over the API — `userId`/`deletedAt` never leave the repository layer. */
export class TripResponseDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty()
  title!: string;

  @ApiProperty({ nullable: true, type: String })
  description!: string | null;

  @ApiProperty()
  destination!: string;

  @ApiProperty()
  country!: string;

  @ApiProperty({ example: '2026-09-10' })
  startDate!: string;

  @ApiProperty({ example: '2026-09-14' })
  endDate!: string;

  @ApiProperty({ enum: TripStatus })
  status!: TripStatus;

  @ApiProperty({ nullable: true, type: String })
  coverImageUrl!: string | null;

  @ApiProperty()
  createdAt!: Date;

  @ApiProperty()
  updatedAt!: Date;
}
