import { ApiProperty } from '@nestjs/swagger';
import { PaginationMetaDto } from '../../../common/dto/pagination-meta.dto';
import { TripResponseDto } from './trip-response.dto';

export class PaginatedTripsResponseDto {
  @ApiProperty({ type: [TripResponseDto] })
  data!: TripResponseDto[];

  @ApiProperty({ type: PaginationMetaDto })
  meta!: PaginationMetaDto;
}
