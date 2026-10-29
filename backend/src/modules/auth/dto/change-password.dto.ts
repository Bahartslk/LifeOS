import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString, MaxLength, MinLength } from 'class-validator';

/** `PATCH /users/me/password`'s body. `newPassword` follows the same length-only policy as sign-up (`RegisterDto`). */
export class ChangePasswordDto {
  @ApiProperty({ example: 'current-password', maxLength: 72 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(72)
  currentPassword!: string;

  @ApiProperty({ example: 'correct-horse-battery-staple', minLength: 8, maxLength: 72 })
  @IsString()
  @MinLength(8)
  @MaxLength(72)
  newPassword!: string;
}
