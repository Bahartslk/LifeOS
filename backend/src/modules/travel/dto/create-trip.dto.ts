import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsOptional, IsString, IsUrl, Matches, MaxLength } from 'class-validator';
import { DATE_PATTERN } from '../../../common/utils/date-time.util';

export class CreateTripDto {
  @ApiProperty({ example: 'Kapadokya Seyahati', maxLength: 200 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(200)
  title!: string;

  @ApiPropertyOptional({ maxLength: 2000 })
  @IsOptional()
  @IsString()
  @MaxLength(2000)
  description?: string;

  @ApiProperty({ example: 'Cappadocia', maxLength: 100 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  destination!: string;

  @ApiProperty({ example: 'Turkey', maxLength: 100 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  country!: string;

  @ApiProperty({ example: '2026-09-10', description: 'Date-only, "YYYY-MM-DD".' })
  @Matches(DATE_PATTERN, { message: 'startDate must be in "YYYY-MM-DD" format.' })
  startDate!: string;

  @ApiProperty({
    example: '2026-09-14',
    description: 'Date-only, "YYYY-MM-DD". Must not be before startDate.',
  })
  @Matches(DATE_PATTERN, { message: 'endDate must be in "YYYY-MM-DD" format.' })
  endDate!: string;

  // No `status`: every trip is created `PLANNED` — see `TravelService.createTrip`.

  @ApiPropertyOptional({ description: 'External image URL — no upload/storage exists yet.' })
  @IsOptional()
  @IsUrl()
  coverImageUrl?: string;
}
