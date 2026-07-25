import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsString, MaxLength, MinLength } from 'class-validator';

export class RegisterDto {
  @ApiProperty({ example: 'bahar@example.com' })
  @IsEmail()
  @MaxLength(254) // RFC 5321 max mailbox length
  email!: string;

  /**
   * Length-only policy (min 8, max 72), not composition rules
   * (uppercase/digit/symbol). Current NIST 800-63B guidance favors length
   * over forced composition, which mostly pushes users toward predictable
   * substitutions rather than real entropy. 72 is bcrypt's own input limit
   * — bytes beyond it are silently ignored, so this is enforced explicitly
   * rather than letting it fail silently.
   */
  @ApiProperty({ example: 'correct-horse-battery-staple', minLength: 8, maxLength: 72 })
  @IsString()
  @MinLength(8)
  @MaxLength(72)
  password!: string;

  @ApiProperty({ example: 'Bahar Toslak' })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  displayName!: string;
}
