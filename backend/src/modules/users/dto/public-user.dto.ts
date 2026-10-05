import { ApiProperty } from '@nestjs/swagger';
import { UserRole } from '@prisma/client';

/**
 * The only shape of a `User` ever sent over the API — never the raw Prisma
 * entity, which carries `passwordHash`. Owned by `UsersModule` since it's
 * the canonical "safe User" representation; `AuthModule` composes it into
 * `LoginResponseDto` rather than defining its own copy, and the future
 * `GET /users/me` (docs/15-api-design.md#users--profile) returns this same
 * shape.
 */
export class PublicUserDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty()
  email!: string;

  @ApiProperty()
  displayName!: string;

  @ApiProperty({ nullable: true, type: String })
  avatarUrl!: string | null;

  @ApiProperty({
    enum: UserRole,
    description:
      'The account\'s role. Read-only: no API request can set it (see the "admin:promote" CLI).',
  })
  role!: UserRole;

  @ApiProperty()
  createdAt!: Date;
}
