import {
  CanActivate,
  Controller,
  ExecutionContext,
  ForbiddenException,
  Get,
  INestApplication,
  Injectable,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { Test } from '@nestjs/testing';
import { UserRole } from '@prisma/client';
import { AddressInfo } from 'net';
import { UsersService } from '../../modules/users/users.service';
import { ROLES_KEY, Roles } from '../decorators/roles.decorator';
import { RolesGuard } from './roles.guard';

describe('RolesGuard', () => {
  const USER_ID = 'user-1';
  let findById: jest.Mock;
  let guard: RolesGuard;

  beforeEach(() => {
    findById = jest.fn();
    guard = new RolesGuard(new Reflector(), { findById } as unknown as UsersService);
  });

  /** A context whose handler/class carry real `@Roles(...)` metadata, read by the real `Reflector`. */
  function contextFor(options: {
    handlerRoles?: UserRole[];
    classRoles?: UserRole[];
    user?: { sub: string; role?: UserRole };
  }): ExecutionContext {
    class TestController {
      handler(): void {}
    }
    if (options.classRoles) Roles(...options.classRoles)(TestController);
    if (options.handlerRoles) {
      Roles(...options.handlerRoles)(
        TestController.prototype,
        'handler',
        Object.getOwnPropertyDescriptor(TestController.prototype, 'handler')!,
      );
    }
    return {
      getHandler: () => TestController.prototype.handler,
      getClass: () => TestController,
      switchToHttp: () => ({ getRequest: () => ({ user: options.user }) }),
    } as unknown as ExecutionContext;
  }

  const adminOnly = (user?: { sub: string; role?: UserRole }) =>
    contextFor({ classRoles: [UserRole.ADMIN], user });

  describe('deny by default', () => {
    it('rejects with 403 when neither the handler nor the controller declares @Roles', async () => {
      await expect(
        guard.canActivate(contextFor({ user: { sub: USER_ID } })),
      ).rejects.toBeInstanceOf(ForbiddenException);
      expect(findById).not.toHaveBeenCalled();
    });

    it('rejects even a database ADMIN when no role is declared', async () => {
      findById.mockResolvedValue({ id: USER_ID, role: UserRole.ADMIN });

      await expect(
        guard.canActivate(contextFor({ user: { sub: USER_ID, role: UserRole.ADMIN } })),
      ).rejects.toBeInstanceOf(ForbiddenException);
    });

    it('rejects when @Roles() is declared with an empty list', async () => {
      findById.mockResolvedValue({ id: USER_ID, role: UserRole.ADMIN });

      await expect(
        guard.canActivate(contextFor({ classRoles: [], user: { sub: USER_ID } })),
      ).rejects.toBeInstanceOf(ForbiddenException);
    });
  });

  it('allows an ADMIN on an admin-only route', async () => {
    findById.mockResolvedValue({ id: USER_ID, role: UserRole.ADMIN });

    await expect(guard.canActivate(adminOnly({ sub: USER_ID }))).resolves.toBe(true);
    expect(findById).toHaveBeenCalledWith(USER_ID);
  });

  it('rejects a USER on an admin-only route with 403', async () => {
    findById.mockResolvedValue({ id: USER_ID, role: UserRole.USER });

    await expect(guard.canActivate(adminOnly({ sub: USER_ID }))).rejects.toBeInstanceOf(
      ForbiddenException,
    );
  });

  it('decides from the database role, ignoring an ADMIN claim in the token', async () => {
    findById.mockResolvedValue({ id: USER_ID, role: UserRole.USER });

    await expect(
      guard.canActivate(adminOnly({ sub: USER_ID, role: UserRole.ADMIN })),
    ).rejects.toBeInstanceOf(ForbiddenException);
  });

  it('allows a database ADMIN whose token claims USER', async () => {
    findById.mockResolvedValue({ id: USER_ID, role: UserRole.ADMIN });

    await expect(guard.canActivate(adminOnly({ sub: USER_ID, role: UserRole.USER }))).resolves.toBe(
      true,
    );
  });

  it('rejects with 401 when the account no longer exists (deleted)', async () => {
    findById.mockResolvedValue(null);

    await expect(
      guard.canActivate(adminOnly({ sub: USER_ID, role: UserRole.ADMIN })),
    ).rejects.toBeInstanceOf(UnauthorizedException);
  });

  it('rejects with 401 when no authenticated user is on the request', async () => {
    await expect(guard.canActivate(adminOnly(undefined))).rejects.toBeInstanceOf(
      UnauthorizedException,
    );
    expect(findById).not.toHaveBeenCalled();
  });

  it('lets handler-level @Roles override the class-level one', async () => {
    findById.mockResolvedValue({ id: USER_ID, role: UserRole.USER });
    const context = contextFor({
      classRoles: [UserRole.ADMIN],
      handlerRoles: [UserRole.USER, UserRole.ADMIN],
      user: { sub: USER_ID },
    });

    await expect(guard.canActivate(context)).resolves.toBe(true);
  });

  it('stores the roles under the key the guard reads', () => {
    class Probe {}
    Roles(UserRole.ADMIN)(Probe);

    expect(Reflect.getMetadata(ROLES_KEY, Probe)).toEqual([UserRole.ADMIN]);
  });
});

/**
 * The same rule through a real Nest application: controllers that apply
 * `RolesGuard` with and without `@Roles`, called over HTTP as a database
 * ADMIN. Only authentication is faked (a guard that puts a user on the
 * request), so this exercises Nest's real metadata and guard pipeline.
 */
describe('RolesGuard deny-by-default (HTTP)', () => {
  @Injectable()
  class FakeAuthGuard implements CanActivate {
    canActivate(context: ExecutionContext): boolean {
      context.switchToHttp().getRequest().user = { sub: 'admin-1', role: UserRole.ADMIN };
      return true;
    }
  }

  @Controller('forgotten')
  @UseGuards(FakeAuthGuard, RolesGuard)
  class ForgottenRolesController {
    @Get()
    read(): string {
      return 'reached';
    }
  }

  @Controller('declared')
  @UseGuards(FakeAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  class DeclaredRolesController {
    @Get()
    read(): string {
      return 'reached';
    }
  }

  @Controller('mixed')
  @UseGuards(FakeAuthGuard, RolesGuard)
  class MixedController {
    @Get('declared')
    @Roles(UserRole.ADMIN)
    declared(): string {
      return 'reached';
    }

    @Get('forgotten')
    forgotten(): string {
      return 'reached';
    }
  }

  let app: INestApplication;
  let baseUrl: string;

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      controllers: [ForgottenRolesController, DeclaredRolesController, MixedController],
      providers: [
        RolesGuard,
        FakeAuthGuard,
        {
          provide: UsersService,
          useValue: { findById: async () => ({ id: 'admin-1', role: UserRole.ADMIN }) },
        },
      ],
    }).compile();
    app = moduleRef.createNestApplication({ logger: false });
    await app.listen(0, '127.0.0.1');
    baseUrl = `http://127.0.0.1:${(app.getHttpServer().address() as AddressInfo).port}`;
  });

  afterAll(async () => {
    await app.close();
  });

  const status = async (path: string) => (await fetch(baseUrl + path)).status;

  it('denies an ADMIN on a guarded controller that forgot @Roles', async () => {
    expect(await status('/forgotten')).toBe(403);
  });

  it('allows an ADMIN on a guarded controller that declares @Roles(ADMIN)', async () => {
    expect(await status('/declared')).toBe(200);
  });

  it('decides per handler when only some handlers declare @Roles', async () => {
    expect(await status('/mixed/declared')).toBe(200);
    expect(await status('/mixed/forgotten')).toBe(403);
  });
});
