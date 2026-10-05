# LifeOS Backend

NestJS API. See [docs/12-project-architecture.md](../docs/12-project-architecture.md#backend-architecture)
for the architecture this module implements and
[docs/15-api-design.md](../docs/15-api-design.md) for the API contract it
will expose.

## Status

Infrastructure foundation (Sprint 13.0), Authentication (Sprint 14.0), a
complete Planner backend (Sprint 15.0), a complete Travel backend (Sprint
16.0), a complete Users / Profile backend (Sprint 17.0), an architecture
audit and cleanup pass (Sprint 17.5), provider-agnostic AI infrastructure
(Sprint 18A), the first real AI endpoints on top of it (Sprint 18B), a
hardening/refinement pass over the AI platform (Sprint 18B.5), Planner's own
AI capabilities on top of all of it (Sprint 19), Travel's own AI
capabilities on the same engine (Sprint 20), the platform's first
cross-module AI feature (Sprint 21), and a second architecture audit
covering the whole AI platform (Sprint 21.5) — no Auth/Users API or database
behavior changed by any of the last eight; Planner's and Travel's existing
CRUD/dashboard routes are unchanged, only additive new routes were added;
and the `/api/v1/ai/...` API contract itself is unchanged since Sprint 18B
(verified byte-identical via Swagger, including after Sprint 21.5):

- `POST /api/v1/auth/{register,login,refresh,logout}` — bcrypt password
  hashing, RS256-signed short-lived access tokens, rotated + SHA-256-hashed
  refresh tokens with family-wide revocation on reuse detection. See
  [docs/12-project-architecture.md](../docs/12-project-architecture.md#security-architecture)
  for the reasoning and `src/modules/auth/` for the implementation.
- `POST/GET/PATCH/DELETE /api/v1/planner/tasks[/:id[/complete|incomplete]]`
  and `GET /api/v1/planner/dashboard` — full task CRUD, soft delete, cursor
  pagination + filtering/sorting, and a dashboard aggregate matching the
  mobile client's Planner + AI Assistant data needs. Ownership-scoped
  end-to-end (404, never 403, for another user's task — see
  [docs/15-api-design.md](../docs/15-api-design.md#authorization)). No
  `TaskList` controller yet — see `src/modules/README.md`.
- `POST /api/v1/planner/ai/{tasks/draft,tasks/:id/breakdown,schedule/analysis,priority-suggestion,tags,deadline-extraction}`
  (Sprint 19) — Planner's own AI capabilities, owned and registered by
  `PlannerModule` itself (not `AiModule`), built entirely on the AI
  platform's existing execution engine (`AiCapabilityService`,
  `ProviderRouter`, `AiFallbackExecutor`, `ContextBuilder`, `PromptBuilder`).
  Natural-language task drafting, task breakdown into subtasks, schedule
  analysis (overloaded/free days, conflicts, workload balance), priority
  suggestion, smart tags, and deadline extraction from relative phrases
  ("next Friday", "tomorrow morning"). **None of these persist anything** —
  every response is a draft or suggestion the mobile client reviews before
  calling the routes above. See
  [docs/15-api-design.md](../docs/15-api-design.md#planner-ai-capabilities).
- `POST/GET/PATCH/DELETE /api/v1/travel/trips[/:id]`,
  `GET/POST /api/v1/travel/trips/:id/itinerary`,
  `PATCH/DELETE /api/v1/travel/itinerary/:id`,
  `PATCH /api/v1/travel/trips/:id/reorder`, and
  `GET /api/v1/travel/dashboard` — full trip + itinerary CRUD, reorder, and
  a dashboard aggregate. Itinerary items can optionally spawn a linked
  Planner task (`source = TRAVEL`) via `PlannerModule`'s exported
  `PlannerService` — Travel never duplicates Planner's task logic. Same
  ownership model as Planner (404, never 403) — see `src/modules/travel/`.
- `POST /api/v1/travel/ai/{trips/draft,itinerary,budget-analysis,packing-list,risk-analysis,tags}`
  (Sprint 20) — Travel's own AI capabilities, owned and registered by
  `TravelModule` itself (not `AiModule`), built on the exact same
  `AiCapabilityService`/`ProviderRouter`/`AiFallbackExecutor`/
  `ContextBuilder`/`PromptBuilder` engine Sprint 19 used for Planner.
  Natural-language trip drafting, day-by-day itinerary generation, budget
  estimation (with explicit uncertainty, never fabricated precision),
  categorized packing lists, itinerary risk analysis (overly busy days,
  unrealistic schedules, missing transportation/preparation), and travel
  tags. **None of these persist anything** — every response is a draft or
  suggestion the mobile client reviews before calling the routes above. See
  [docs/15-api-design.md](../docs/15-api-design.md#travel-ai-capabilities).
- `GET /api/v1/users/me`, `PATCH /api/v1/users/me`, and
  `PATCH /api/v1/users/preferences` — profile (displayName, avatarUrl, bio)
  and preferences (themePreference, language, timezone,
  notificationPreferences — a schema-flexible `Json` column, extensible
  without a migration) directly on `User`, no separate `user_preferences`
  table. No `:id` routes — every route operates on the caller's own JWT
  subject, so there's no foreign-resource case at all. See
  `src/modules/users/`.
- `AiModule` — provider-agnostic AI infrastructure (Sprint 18A) backing
  four real, hardened endpoints (Sprint 18B, refined in Sprint 18B.5):
  `POST /api/v1/ai/{planner/analyze,planner/suggest,travel/suggest,daily-brief}`
  — capability-scoped, not a generic chat endpoint, each returning a
  normalized `{provider, model, result, usage, latencyMs}` envelope
  regardless of which provider ran it. `result` is Zod-schema-validated
  against the capability's own declared shape before it ever leaves the
  server — a malformed or incomplete AI response never reaches a caller.
  Two providers today — `GeminiProvider` (Google's official `@google/genai`
  SDK) and `OpenRouterProvider` (OpenRouter's OpenAI-compatible REST API
  via native `fetch`) — both self-registered through Nest DI via
  `ProviderRegistry`, never `new`'d directly. Each of the four capabilities
  fully describes its own routing/generation behavior as metadata
  (`preferredProvider`, `fallbackAllowed`, `temperature`, `maxTokens`) — no
  capability-specific branch exists in any service. `ProviderRouter`
  resolves default/priority/feature-config/capability-preference selection
  (config always wins over a code default); `AiFallbackExecutor`
  automatically falls through the resolved provider chain, but only after
  each candidate's own bounded-retry attempt (`AiRetryExecutor`) is
  exhausted, gated by both `AI_FALLBACK_ENABLED` and the capability's own
  `fallbackAllowed`. `AiService` remains the module's only export to other
  modules — no controller/endpoint dependency ever leaks a provider type.
  **Still no persistence** (`ai_conversations`/`ai_messages` remain
  absent). See `src/modules/README.md`, `src/modules/ai/`, and
  [docs/15-api-design.md](../docs/15-api-design.md#ai-capabilities) for the
  full architecture and API contract. A third provider (Anthropic, Groq,
  Azure OpenAI, Ollama, ...) requires implementing `AiProvider` and
  registering it in `ai.module.ts`, never touching business logic.
- `POST /api/v1/daily-brief/{summary,conflicts,preparation,risks,priorities,motivation}`
  (Sprint 21) — the platform's first genuinely cross-module AI feature, in
  a brand-new `DailyBriefModule` (not owned by Planner or Travel, since
  neither owns "the user's whole day"). Every one of the six combines
  Planner and Travel context together via the unmodified `ContextBuilder`
  and the existing `assistant` prompt template — daily agenda summary,
  cross-module schedule conflict detection (overlaps, impossible
  schedules, insufficient travel time, overloaded days), preparation
  recommendations, a 7-day-forward risk analysis (severity/recommendation/
  reason per risk), a today's-priorities ranking *recommendation* (never
  modifies a task's stored priority), and one specific, non-generic daily
  motivational message. **None of these persist anything or write to
  Planner/Travel data.** Distinct from `AiModule`'s own single
  `POST /api/v1/ai/daily-brief` capability (Sprint 18B), which remains
  unchanged. Unlike Sprint 19/20's per-entity capabilities, none of these
  six fetch one specific `Task`/`Trip` — every one reasons purely over the
  caller's own aggregate context, so `DailyBriefModule` introduces no new
  circular dependency (it imports only `AiModule`). See
  [docs/15-api-design.md](../docs/15-api-design.md#daily-brief-intelligence).
- `NotificationsModule` remains registered but empty. The `ProfileModule`
  shell registered since Sprint 13.0 was removed in Sprint 17.5 once its
  entire reserved purpose was confirmed fulfilled by `UsersModule` instead.
- Configuration + env validation, Prisma (`User` — now including Sprint
  17.0's profile/preference fields, `RefreshToken`, `TaskList`, `Task`,
  `Trip`, `ItineraryItem`), global validation, exception filtering,
  response enveloping, request logging, compression, Helmet, CORS, health
  check, and Swagger are all wired and ready.

## Getting Started

1. `cp .env.example .env` and fill in `DATABASE_URL` and a generated RS256
   key pair (instructions in `.env.example`). Every `AI_*` variable has a
   safe default and can be left as-is; only set `GEMINI_API_KEY` and/or
   `OPENROUTER_API_KEY` once you actually need one of the `/api/v1/ai/...`
   endpoints to succeed (without at least one key, the app still boots — a
   call just fails with a clear `ProviderUnavailableException`).
2. `npm install`
3. Start PostgreSQL: `docker compose -f ../docker/docker-compose.yml up postgres -d`
   (or point `DATABASE_URL` at your own local Postgres instance).
4. `npm run prisma:generate`
5. `npm run prisma:migrate:dev`
6. `npm run start:dev`
7. `GET http://localhost:3000/health` for a liveness check; Swagger UI is
   served at `http://localhost:3000/api/docs`.
8. `npm test` runs the unit test suite (currently: `AiModule`'s
   `ProviderRouter`, `AiFallbackExecutor`, response-validation logic,
   `AiCapabilityService`, and `classifyProviderHttpError` — the latter two
   added in Sprint 21.5, see `src/modules/README.md`'s Sprint 18B.5 and
   21.5 entries). No database or network access required; every other
   sprint's own verification has been live-E2E instead (curl against a
   running server) — see each sprint's own deliverables for that history.

## Running via Docker Compose

`docker/docker-compose.yml` runs Postgres and the backend together. Its
`DATABASE_URL` must use `postgres` (the compose service name) as the host,
not `localhost` — see the comment in `.env.example`. Since both the local
and Docker workflows read the same `.env` (`env_file: ../backend/.env`),
switch the host in `.env` depending on which one you're running, or keep two
files and point `env_file` at whichever applies.

```
docker compose -f ../docker/docker-compose.yml up -d --build
```

## Admin Access

Accounts have a role, `USER` (default) or `ADMIN`. Routes under
`/api/v1/admin/...` require an account whose current role in the database
is `ADMIN`; see `docs/15-api-design.md#roles`.

No API request can grant a role. An operator with database access promotes
an account that already exists (registered the normal way):

```
npm run build
npm run admin:promote -- person@example.com              # dry run: shows what would change
npm run admin:promote -- person@example.com --confirm    # USER -> ADMIN
npm run admin:promote -- person@example.com --revoke --confirm   # ADMIN -> USER
```

The tool reads `DATABASE_URL` from the environment, prints which database
it is connected to (host and name only), writes nothing without
`--confirm`, never creates an account and never touches a password. A role
change takes effect immediately, including for already-issued access
tokens. During development `npm run admin:promote:dev -- ...` runs the
TypeScript source directly.

## Adding a Feature Module

1. Add the entities it needs to `prisma/schema.prisma` (per
   [docs/14-database-design.md](../docs/14-database-design.md)) and run
   `npm run prisma:migrate:dev`.
2. Create `src/modules/<feature>/` with its controller, service, DTOs, and a
   repository that injects `PrismaService`.
3. Protect authenticated routes with `JwtAuthGuard` and read the caller's id
   via the `@CurrentUser()` decorator (`src/common/decorators`).
4. Import the new module in `AppModule`.

See [docs/16-development-guidelines.md](../docs/16-development-guidelines.md)
for coding, branching, and review conventions.
