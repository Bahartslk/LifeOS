import { ApiProperty } from '@nestjs/swagger';

/** `DELETE /users/me`'s response: the account and all of its data are gone. */
export class DeleteAccountResponseDto {
  @ApiProperty({ example: true })
  success!: boolean;
}
