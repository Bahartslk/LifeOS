import { ApiProperty } from '@nestjs/swagger';

/** Response shape for register and refresh — a bare token pair, no user info (see login for the one endpoint that also returns `user`). */
export class AuthTokensDto {
  @ApiProperty()
  accessToken!: string;

  @ApiProperty()
  refreshToken!: string;
}
