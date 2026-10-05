import { ApiProperty } from '@nestjs/swagger';
import { UserRole } from '@prisma/client';

/**
 * `GET /admin/session`'s response — who the authenticated admin is. The
 * admin panel calls this right after login to confirm, against the
 * backend, that the account really is an admin before showing anything.
 */
export class AdminSessionResponseDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty()
  email!: string;

  @ApiProperty()
  displayName!: string;

  @ApiProperty({ enum: UserRole, example: UserRole.ADMIN })
  role!: UserRole;
}
