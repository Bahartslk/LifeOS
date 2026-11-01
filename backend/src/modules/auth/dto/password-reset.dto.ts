import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsString, Matches, MaxLength, MinLength } from 'class-validator';

/** `POST /auth/forgot-password`'s body. */
export class ForgotPasswordDto {
  @ApiProperty({ example: 'bahar@example.com' })
  @IsEmail()
  @MaxLength(254)
  email!: string;
}

/** `POST /auth/reset-password`'s body. `newPassword` follows the sign-up rule (`RegisterDto`). */
export class ResetPasswordDto {
  @ApiProperty({ example: 'bahar@example.com' })
  @IsEmail()
  @MaxLength(254)
  email!: string;

  @ApiProperty({ example: '042817', description: 'The 6-digit code from the email.' })
  @IsString()
  @Matches(/^\d{6}$/, { message: 'code must be 6 digits.' })
  code!: string;

  @ApiProperty({ example: 'correct-horse-battery-staple', minLength: 8, maxLength: 72 })
  @IsString()
  @MinLength(8)
  @MaxLength(72)
  newPassword!: string;
}

/** Both password reset routes answer with this; the forgot route says the same for every address. */
export class PasswordResetResponseDto {
  @ApiProperty({ example: true })
  success!: boolean;
}
