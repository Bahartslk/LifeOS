# Folder Structure

## Document Information

| Field | Value |
| --- | --- |
| Document | Folder Structure |
| Status | Draft |
| Version | 1.10.0 |
| Last Updated | 2026-07-22 |
| Owner | Engineering |

## Table of Contents

- [Purpose](#purpose)
- [Structure Overview](#structure-overview)
- [Directory Layout](#directory-layout)
- [Naming Conventions](#naming-conventions)
- [Notes](#notes)

## Purpose

This document maps the architecture defined in [12-project-architecture.md](12-project-architecture.md) onto the actual folder layout of the LifeOS repository, and defines the responsibility of each top-level directory referenced in [CLAUDE.md](../CLAUDE.md#project-structure).

## Structure Overview

| Folder | Responsibility |
| --- | --- |
| `mobile/` | Kotlin / Compose Multiplatform client, organized feature-first per module. |
| `backend/` | NestJS API, organized feature-first by module with clear layer separation. |
| `docs/` | Project documentation; this folder. |
| `design/` | Design assets, including the Stitch design source (`design/stitch/`) referenced by [06-design-system.md](06-design-system.md) and [05-screen-inventory.md](05-screen-inventory.md). |
| `docker/` | Docker configuration for local development and deployment. |

## Directory Layout

### `mobile/`

A single Kotlin Multiplatform Gradle module (`composeApp`) targeting Android and iOS, organized feature-first per [CLAUDE.md](../CLAUDE.md#architecture); each feature owns its own presentation, domain, and data code, matching the six modules in [01-project-overview.md](01-project-overview.md#core-modules). Package root is `com.lifeos.app`.

```
mobile/
  gradle/
    libs.versions.toml         # Version catalog (Koin, Ktor, Coil, Navigation, ...)
  settings.gradle.kts
  build.gradle.kts
  composeApp/
    build.gradle.kts
    src/
      commonMain/kotlin/com/lifeos/app/
        App.kt                # Root composable: DI scope + theme + nav host
        core/
          di/                 # Koin modules (AppModule, NetworkModule, StorageModule, PlatformModule expect)
          network/            # Ktor HttpClient, ApiConfig, AuthTokenProvider contract
          storage/            # DataStore-backed PreferencesStorage
          designsystem/
            theme/             # Material 3 theme (06-design-system.md)
            components/        # Reusable UI primitives (06-design-system.md#components)
          navigation/          # Destination routes + LifeOSNavHost
        features/
          auth/{presentation,domain,data}/     # reserved, empty
          home/{presentation,domain,data}/     # reserved, empty
          aiassistant/{presentation,domain,data}/  # reserved, empty
          travel/{presentation,domain,data}/   # reserved, empty
          planner/{presentation,domain,data}/  # reserved, empty
          profile/{presentation,domain,data}/  # reserved, empty
      androidMain/kotlin/com/lifeos/app/       # LifeOSApplication, MainActivity, platform DI actuals
      iosMain/kotlin/com/lifeos/app/           # MainViewController, platform DI actuals
```

Each `presentation/domain/data` triad follows the layering defined in [12-project-architecture.md](12-project-architecture.md#clean-architecture-layers). The `features/*` folders exist but are empty until each feature is implemented — see `mobile/README.md`.

### `backend/`

Organized feature-first by module, following NestJS conventions and the layering defined in [12-project-architecture.md](12-project-architecture.md#layered-architecture).

```
backend/
  prisma/
    schema.prisma          # Datasource + generator + User (incl. Sprint 17.0's profile/preference fields) + RefreshToken + TaskList + Task + Trip + ItineraryItem (14-database-design.md); ai_* still absent — no separate user_preferences table, see User's doc comment
    migrations/             # init_user, add_refresh_tokens, add_planner, add_travel
  src/
    common/
      filters/             # AllExceptionsFilter (15-api-design.md#error-handling)
      interceptors/         # LoggingInterceptor, TransformResponseInterceptor (+ @SkipResponseTransform escape hatch)
      guards/               # JwtAuthGuard (authentication only; ownership guards are per-feature)
      strategies/           # JwtStrategy (RS256 verification; payload: sub, email)
      decorators/           # @CurrentUser(), @SkipResponseTransform()
      dto/                  # PaginationQueryDto, PaginationMetaDto (15-api-design.md#pagination), NaturalLanguageInputDto (shared {text} body for any single-free-text AI capability endpoint — Planner's task draft/deadline-extraction, Travel's trip draft; promoted here in Sprint 21.5 once a second module needed the identical shape)
      utils/                # parseDurationMs (refresh token expiry), toDbDate/toDbTime/fromDbDate/fromDbTime (date-only/time-only <-> Prisma Date conversions, shared by Planner + Travel)
    config/                 # Env validation (Joi) + typed configuration accessor (incl. auth.bcryptSaltRounds)
    prisma/                 # PrismaModule / PrismaService (connection lifecycle only)
    health/                 # GET /health — status, timestamp, version; unversioned, prefix-free
    modules/
      auth/                 # AuthController/AuthService/RefreshTokensRepository — register/login/refresh/logout (15-api-design.md#authentication)
      users/                 # UsersController (GET/PATCH /users/me, PATCH /users/preferences), UsersRepository/UsersService (find/create/toPublicUser/getProfile/updateProfile/updatePreferences) — 15-api-design.md#users--profile
      planner/               # PlannerController/PlannerService/TasksRepository/TaskListsRepository — task CRUD + dashboard (15-api-design.md#tasks-planner); exports PlannerService for cross-module use; planner/ai/ (PlannerAiController/PlannerAiService, Sprint 19) — 6 Planner-owned AI capability endpoints (15-api-design.md#planner-ai-capabilities) built on AiModule's AiCapabilityService, imported via forwardRef() (mutual circular dependency with AiModule)
      travel/                # TravelController/ItineraryController, TravelService/ItineraryService, TripsRepository/ItineraryItemsRepository — trip + itinerary CRUD, reorder, dashboard (15-api-design.md#trips-travel); imports PlannerModule for the Planner-task integration; exports TravelService for AiModule's ContextBuilder; travel/ai/ (TravelAiController/TravelAiService, Sprint 20) — 6 Travel-owned AI capability endpoints (15-api-design.md#travel-ai-capabilities) built on AiModule's AiCapabilityService, imported via forwardRef() (mutual circular dependency with AiModule)
      ai/                    # AiController (4 capability endpoints, 15-api-design.md#ai-capabilities) over AiService, a provider-agnostic AiProvider/ProviderRegistry/ProviderRouter/AiFallbackExecutor (GeminiProvider + OpenRouterProvider), ContextBuilder, PromptBuilder + templates, capabilities/, swagger/ (ApiAiProviderErrorResponses — shared 503/502 Swagger decorator reused by every AI capability controller, promoted in Sprint 21.5) — still no persistence (12-project-architecture.md#ai-integration-architecture)
      daily-brief/           # DailyBriefController/DailyBriefService (Sprint 21) — 6 cross-module AI capability endpoints (15-api-design.md#daily-brief-intelligence) combining Planner + Travel context via AiModule's AiCapabilityService/ContextBuilder/assistant template; imports only AiModule, no circular dependency (no capability operates on one specific existing entity)
      proactive-assistant/   # ProactiveAssistantController/ProactiveAssistantService (Sprint 22) — the platform's first proactive (not reactive) AI capability set (15-api-design.md#proactive-assistant-engine): suggestions/opportunities/reminders/insights reuse AiCapabilityService/ContextBuilder/assistant template exactly as daily-brief/ does; prioritization/ (suggestion-prioritizer.util.ts) is a deterministic rank/deduplicate/remove-contradictions algorithm, not an AI call; imports only AiModule, no circular dependency
      notifications/         # registered, still an empty shell (see modules/README.md for build order); profile/ removed in Sprint 17.5 once superseded by users/
    app.module.ts
    main.ts
```

`modules/` holds one subfolder per feature, registered in `app.module.ts` but implemented one at a time — see [modules/README.md](../backend/src/modules/README.md) for build order and naming notes.

### `docs/`

```
docs/
  01-project-overview.md
  02-product-vision.md
  03-user-personas.md
  04-user-flow.md
  05-screen-inventory.md
  06-design-system.md
  07-functional-requirements.md
  08-non-functional-requirements.md
  09-ai-features.md
  10-mvp.md
  11-roadmap.md
  12-project-architecture.md
  13-folder-structure.md
  14-database-design.md
  15-api-design.md
  16-development-guidelines.md
```

### `design/`

```
design/
  stitch/                 # Stitch-generated UI design source, referenced by 06-design-system.md
```

### `docker/`

```
docker/
  docker-compose.yml       # Local backend + PostgreSQL orchestration
  backend.Dockerfile       # Backend container image definition
```

Additional Docker services introduced as the deployment scales out — connection pooling, caching, and the AI job queue, per [12-project-architecture.md](12-project-architecture.md#scalability--production-readiness) — are added to `docker-compose.yml` (and a dedicated `worker.Dockerfile` for the AI worker pool) at the phase noted in that document, not before; this folder should not be pre-populated with that infrastructure ahead of need.

## Naming Conventions

- Mobile feature folders use lowercase, single-word names matching the module names in [01-project-overview.md](01-project-overview.md#core-modules) (`auth`, `home`, `aiassistant`, `travel`, `planner`, `profile`).
- Backend module folders use lowercase, plural resource names matching their primary entity (`users`, `trips`, `tasks`), except `auth` and `ai`, which are capability-named rather than resource-named.
- Documentation files use the `NN-kebab-case-name.md` convention already established in `docs/`, preserving numeric ordering.

## Notes

Any structural change to `mobile/` or `backend/` that deviates from this layout should update this document in the same change, per the [Documentation Rules](../CLAUDE.md#documentation-rules) in `CLAUDE.md`.
