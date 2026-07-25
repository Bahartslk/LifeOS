import { Module } from '@nestjs/common';
import { UsersController } from './users.controller';
import { UsersRepository } from './repositories/users.repository';
import { UsersService } from './users.service';

/**
 * Sprint 14.0 (Authentication Backend) added `UsersRepository`/
 * `UsersService` (find/create/toPublicUser) for `AuthModule` to depend on
 * — still exported for that same reason. Sprint 17.0 (Users / Profile
 * Backend) added `UsersController`: `GET /users/me`,
 * `PATCH /users/me`, `PATCH /users/preferences`, per
 * docs/15-api-design.md#users--profile.
 */
@Module({
  controllers: [UsersController],
  providers: [UsersRepository, UsersService],
  exports: [UsersService],
})
export class UsersModule {}
