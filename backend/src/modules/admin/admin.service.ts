import { Injectable, UnauthorizedException } from '@nestjs/common';
import { UsersService } from '../users/users.service';
import { AdminSessionResponseDto } from './dto/admin-session-response.dto';

/**
 * Admin-panel business logic. Read-only by design in this first version:
 * nothing here creates, changes or deletes data.
 */
@Injectable()
export class AdminService {
  constructor(private readonly usersService: UsersService) {}

  /** The calling admin's own identity, read fresh from the database. */
  async getSession(adminId: string): Promise<AdminSessionResponseDto> {
    const user = await this.usersService.findById(adminId);
    if (!user) {
      throw new UnauthorizedException('Account no longer exists.');
    }
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      role: user.role,
    };
  }
}
