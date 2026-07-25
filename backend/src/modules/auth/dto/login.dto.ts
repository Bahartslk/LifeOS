import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsString, MaxLength } from 'class-validator';

export class LoginDto {
  @ApiProperty({ example: 'bahar@example.com' })
  @IsEmail()
  @MaxLength(254)
  email!: string;

  // No @MinLength here: an intentionally-wrong-length password must still
  // reach AuthService and fail as "invalid credentials" like any other
  // wrong password, not a distinct 400 that leaks a length-based signal.
  @ApiProperty({ example: 'correct-horse-battery-staple' })
  @IsString()
  @MaxLength(72)
  password!: string;
}
