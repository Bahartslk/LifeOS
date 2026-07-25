import { ApiProperty } from '@nestjs/swagger';
import { PublicUserDto } from '../../users/dto/public-user.dto';
import { AuthTokensDto } from './auth-tokens.dto';

export class LoginResponseDto extends AuthTokensDto {
  @ApiProperty({ type: PublicUserDto })
  user!: PublicUserDto;
}
