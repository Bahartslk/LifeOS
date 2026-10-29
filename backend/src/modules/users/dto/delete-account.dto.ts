import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

/** `DELETE /users/me`'s body: the account's current password, to confirm the deletion. */
export class DeleteAccountDto {
  @ApiProperty({ example: 'correct-horse-battery-staple', maxLength: 72 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(72)
  password!: string;
}
