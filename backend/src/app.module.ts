import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { APP_FILTER, APP_GUARD, APP_INTERCEPTOR } from '@nestjs/core';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import configuration from './config/configuration';
import { envValidationSchema } from './config/env.validation';
import { THROTTLE_WINDOW_MS, throttleLimits } from './config/throttle.config';
import { PrismaModule } from './prisma/prisma.module';
import { HealthModule } from './health/health.module';
import { AuthModule } from './modules/auth/auth.module';
import { UsersModule } from './modules/users/users.module';
import { AdminModule } from './modules/admin/admin.module';
import { PlannerModule } from './modules/planner/planner.module';
import { TravelModule } from './modules/travel/travel.module';
import { AiModule } from './modules/ai/ai.module';
import { DailyBriefModule } from './modules/daily-brief/daily-brief.module';
import { ProactiveAssistantModule } from './modules/proactive-assistant/proactive-assistant.module';
import { NotificationsModule } from './modules/notifications/notifications.module';
import { AllExceptionsFilter } from './common/filters/all-exceptions.filter';
import { LoggingInterceptor } from './common/interceptors/logging.interceptor';
import { TransformResponseInterceptor } from './common/interceptors/transform-response.interceptor';

/**
 * Root module. Wires infrastructure (configuration, the database
 * connection, JWT verification, health checks, and the global
 * error/response/logging pipeline, per
 * docs/12-project-architecture.md#backend-architecture) plus every feature
 * module. `AuthModule`, `UsersModule`, `PlannerModule`, and `TravelModule`
 * are fully implemented (Sprints 14.0–17.0). `AiModule` (Sprint 18A–20) is
 * provider-agnostic AI infrastructure with its own four capability
 * endpoints, plus `AiCapabilityService` reused by `PlannerModule`'s (Sprint
 * 19) and `TravelModule`'s (Sprint 20) own AI capabilities — both of those
 * are mutually circular with `AiModule` (resolved via `forwardRef()`; see
 * each module's own doc comment). `DailyBriefModule` (Sprint 21) is the
 * platform's first genuinely cross-module AI feature — it imports only
 * `AiModule` (no circular dependency, since nothing in `AiModule` needs
 * anything from it). `ProactiveAssistantModule` (Sprint 22) is the
 * platform's first proactive (not reactive) AI feature — same reasoning,
 * same one-directional `AiModule`-only import, no circular dependency.
 * `NotificationsModule` remains an empty registration
 * with no controller or service. The `ProfileModule` shell registered
 * since Sprint 13.0 was removed in Sprint 17.5 (Architecture Audit) once
 * its entire reserved purpose was confirmed fulfilled by `UsersModule`
 * instead.
 */
@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [configuration],
      validationSchema: envValidationSchema,
    }),
    // Per-IP rate limiting on every route (see config/throttle.config.ts);
    // the credential routes in AuthController tighten it further.
    ThrottlerModule.forRoot([{ ttl: THROTTLE_WINDOW_MS, limit: throttleLimits.global }]),
    PrismaModule,
    HealthModule,
    AuthModule,
    UsersModule,
    AdminModule,
    PlannerModule,
    TravelModule,
    AiModule,
    DailyBriefModule,
    ProactiveAssistantModule,
    NotificationsModule,
  ],
  providers: [
    { provide: APP_GUARD, useClass: ThrottlerGuard },
    { provide: APP_FILTER, useClass: AllExceptionsFilter },
    { provide: APP_INTERCEPTOR, useClass: LoggingInterceptor },
    { provide: APP_INTERCEPTOR, useClass: TransformResponseInterceptor },
  ],
})
export class AppModule {}
