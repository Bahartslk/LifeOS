import { ApiProperty } from '@nestjs/swagger';
import { IsString, MinLength } from 'class-validator';

/** Shared by both `POST /auth/refresh` and `POST /auth/logout` — both bodies are exactly `{ refreshToken }`. */
export class RefreshTokenDto {
  @ApiProperty({ description: 'The raw refresh token issued at login/register/refresh.' })
  @IsString()
  @MinLength(1)
  refreshToken!: string;
}
