import { Injectable, NotFoundException } from '@nestjs/common';
import { Prisma, User } from '@prisma/client';
import { CreateUserData, UsersRepository } from './repositories/users.repository';
import { PublicUserDto } from './dto/public-user.dto';
import { UserProfileResponseDto } from './dto/user-profile-response.dto';
import { NotificationPreferencesDto } from './dto/notification-preferences.dto';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { UpdatePreferencesDto } from './dto/update-preferences.dto';

/**
 * `findByEmail`/`findById` intentionally return the full `User` (including
 * `passwordHash`) — `AuthService` needs the hash to verify a password —
 * every other caller must go through `toPublicUser`/`toProfileResponse`
 * before returning a user over the API. Sprint 17.0 (Users / Profile
 * Backend) added `getProfile`/`updateProfile`/`updatePreferences` —
 * `UsersController`'s three routes — on top of Sprint 14.0's
 * find/create/`toPublicUser`, which `AuthService` still depends on
 * unchanged.
 */
@Injectable()
export class UsersService {
  constructor(private readonly usersRepository: UsersRepository) {}

  findByEmail(email: string): Promise<User | null> {
    return this.usersRepository.findByEmail(email);
  }

  findById(id: string): Promise<User | null> {
    return this.usersRepository.findById(id);
  }

  create(data: CreateUserData): Promise<User> {
    return this.usersRepository.create(data);
  }

  toPublicUser(user: User): PublicUserDto {
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      avatarUrl: user.avatarUrl,
      role: user.role,
      createdAt: user.createdAt,
    };
  }

  async getProfile(userId: string): Promise<UserProfileResponseDto> {
    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found.');
    }
    return this.toProfileResponse(user);
  }

  /** `PATCH /users/me` — displayName/avatarUrl/bio/themePreference/language/timezone. Never `email`/`notificationPreferences` — `UpdateProfileDto` simply doesn't declare them. */
  async updateProfile(userId: string, dto: UpdateProfileDto): Promise<UserProfileResponseDto> {
    const user = await this.usersRepository.update(userId, {
      ...(dto.displayName !== undefined && { displayName: dto.displayName }),
      ...(dto.avatarUrl !== undefined && { avatarUrl: dto.avatarUrl }),
      ...(dto.bio !== undefined && { bio: dto.bio }),
      ...(dto.timezone !== undefined && { timezone: dto.timezone }),
      ...(dto.language !== undefined && { language: dto.language }),
      ...(dto.themePreference !== undefined && { themePreference: dto.themePreference }),
    });

    if (!user) {
      throw new NotFoundException('User not found.');
    }
    return this.toProfileResponse(user);
  }

  /**
   * `PATCH /users/preferences` — themePreference/language/timezone/
   * notificationPreferences only, never `displayName`/`avatarUrl`/`bio`.
   * `notificationPreferences` is merged into the existing stored JSON, not
   * replaced wholesale — PATCH semantics: a caller sending only
   * `{ aiInsights: false }` must not silently wipe out
   * `taskReminders`/`tripReminders`. The extra `findById` only happens when
   * `notificationPreferences` is actually part of the request.
   */
  async updatePreferences(
    userId: string,
    dto: UpdatePreferencesDto,
  ): Promise<UserProfileResponseDto> {
    let notificationPreferences: Prisma.InputJsonValue | undefined;
    if (dto.notificationPreferences !== undefined) {
      const existing = await this.usersRepository.findById(userId);
      if (!existing) {
        throw new NotFoundException('User not found.');
      }
      // `class-transformer` instantiates `NotificationPreferencesDto` with
      // an explicit `undefined` own-property for every field the client
      // didn't send, not just the ones it did. Spreading it as-is would
      // carry those `undefined`s onto the merge target — harmless in JS,
      // but Prisma's JSONB serialization drops `undefined`-valued keys
      // during JSON.stringify, so the *existing* stored value for that key
      // silently disappears too. Filtering to only the keys the caller
      // actually provided is what makes this a true partial merge.
      const providedPreferences = Object.fromEntries(
        Object.entries(dto.notificationPreferences).filter(([, value]) => value !== undefined),
      );
      notificationPreferences = {
        ...(existing.notificationPreferences as Prisma.JsonObject),
        ...providedPreferences,
      };
    }

    const user = await this.usersRepository.update(userId, {
      ...(dto.timezone !== undefined && { timezone: dto.timezone }),
      ...(dto.language !== undefined && { language: dto.language }),
      ...(dto.themePreference !== undefined && { themePreference: dto.themePreference }),
      ...(notificationPreferences !== undefined && { notificationPreferences }),
    });

    if (!user) {
      throw new NotFoundException('User not found.');
    }
    return this.toProfileResponse(user);
  }

  private toProfileResponse(user: User): UserProfileResponseDto {
    return {
      ...this.toPublicUser(user),
      bio: user.bio,
      timezone: user.timezone,
      language: user.language,
      themePreference: user.themePreference,
      // Safe: the only writer of this column is `updatePreferences`, always
      // through `NotificationPreferencesDto`'s validation.
      notificationPreferences:
        user.notificationPreferences as unknown as NotificationPreferencesDto,
      updatedAt: user.updatedAt,
    };
  }
}
