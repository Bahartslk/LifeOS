# Feature Modules

Sprint 13.0 (Backend Foundation) registered every module shell below —
`auth`, `users`, `planner`, `travel`, `profile`, `ai`, `notifications` — so
`app.module.ts`'s import graph was already correct, but implemented none of
them.

Sprint 14.0 (Authentication Backend) implemented `auth`
(`AuthController`/`AuthService`/`RefreshTokensRepository` —
register/login/refresh/logout) and just enough of `users`
(`UsersRepository`/`UsersService`: find/create/`toPublicUser`, no controller
yet) for `auth` to depend on.

Sprint 15.0 (Planner Backend) implemented `planner` in full:
`PlannerController`/`PlannerService`/`TasksRepository`/`TaskListsRepository`
— task CRUD, complete/incomplete, and the dashboard aggregate, per
[15-api-design.md](../../../docs/15-api-design.md#tasks-planner). No
`TaskListsController` yet (see that sprint's explicit route list) — tasks
can reference a `taskListId`, but lists themselves can only be created
directly in the database until a future sprint adds one. `PlannerService`
is exported for cross-module use (see Sprint 16.0 below).

Planner Step 3 (overdue tasks + local date): the dashboard's "today" is
the caller's local date — an optional `?date=YYYY-MM-DD` from the client,
otherwise today in `User.timezone` (via `common/utils/date-time.util.ts`'s
`todayInTimeZone`; `PlannerModule` now imports `UsersModule`) — instead of
the server's UTC date. The response gains `overdueTasks` (unfinished tasks
due before today), which `highPriorityTasks` and the AI Planner context
now include; the `planner`/`assistant` prompt templates were bumped to
`v2` for that. Travel's dashboard still uses the UTC date (separate
follow-up).

Sprint 16.0 (Travel Backend) implemented `travel` in full: trip CRUD,
itinerary CRUD + reorder, and the dashboard, per
[15-api-design.md](../../../docs/15-api-design.md#trips-travel). Split into
two controllers/services — `TravelController`/`TravelService` (trips,
dashboard) and `ItineraryController`/`ItineraryService` (itinerary items,
reorder, the Planner integration) — to avoid one service covering both.
`travel` imports `PlannerModule` and `ItineraryService` injects
`PlannerService` directly to create/update/delete linked Planner tasks
(`source = TRAVEL`) — see
[12-project-architecture.md](../../../docs/12-project-architecture.md#integration-points).
No `TaskListsController`-style gap here — Travel's API surface is complete
per that sprint's explicit route list.

Sprint 17.0 (Users / Profile Backend) implemented `users` in full:
`UsersController` (`GET /users/me`, `PATCH /users/me`,
`PATCH /users/preferences`) on top of Sprint 14.0's
`UsersRepository`/`UsersService`, per
[15-api-design.md](../../../docs/15-api-design.md#users--profile). Per that
sprint's explicit title and scope ("UsersModule becomes responsible for
profile information and user preferences"), profile/preference fields were
added directly to `users` and this module — **not** a separate
`ProfileModule`.

Sprint 17.5 (Backend Architecture Audit & Cleanup) removed the `profile/`
shell entirely: its entire reserved purpose (per its own doc comment, "will
own `user_preferences`... or owns its table directly") had been fulfilled by
`users` since Sprint 17.0, and Sprint 17.0 itself had already flagged it as
a retirement candidate rather than a module with any remaining future. The
same sprint also promoted the `DATE_PATTERN`/`TIME_PATTERN` regex constants
that `planner` and `travel` had each independently redeclared into
`common/utils/date-time.util.ts`, and reconciled several stale doc comments
across `common/` and `app.module.ts` — no API or database behavior changed.

Sprint 18A (AI Foundation) implemented `ai`'s provider-agnostic
infrastructure — no chat endpoint, no controller, no persistence, per that
sprint's explicit "infrastructure only" scope. `AiModule` exports a single
`AiService` facade; a future feature module injects it and calls
`.generate()` without ever knowing whether Gemini, OpenAI, or any other
provider handled the request. Internally:

- `interfaces/ai-provider.interface.ts` — the `AiProvider` seam every
  provider integration implements; `providers/gemini/gemini.provider.ts` is
  the only file in the module that imports the `@google/genai` SDK.
- `registry/provider-registry.service.ts` — discovers/registers/resolves/
  validates every `AiProvider` Nest constructs, via a DI multi-provider
  token (`AI_PROVIDERS` in `constants/ai.constants.ts`) rather than any
  direct `new SomeProvider()` call.
- `router/provider-router.service.ts` — resolves an ordered provider chain
  from `AiConfigService`'s `defaultProvider`/`enabledProviders`/
  `providerPriority`, with a real feature-specific override
  (`AI_FEATURE_PROVIDER_OVERRIDES`, Sprint 18B) that reorders — never
  shrinks — that chain.
- `context/context-builder.service.ts` + `mapper/context.mapper.ts` —
  assembles a provider-independent `AiContext` from `UsersService`.
  `getProfile`, `PlannerService.getDashboard`, and `TravelService
  .getDashboard` only (never a repository directly), truncated to a small
  per-section cap for prompt injection.
- `templates/` + `prompt/prompt-builder.service.ts` — five centrally
  maintained, versioned system-prompt templates (`base-system`, `general`,
  `planner`, `travel`, `assistant`); nothing outside this folder
  concatenates a prompt string, per CLAUDE.md's AI Integration Rules.
- `service/ai-retry.executor.ts` — bounded exponential-backoff retry,
  applied only to the two transient exception types
  (`ProviderTimeoutException`, `ProviderUnavailableException`); rate-limit
  and invalid-response failures are never retried, per
  docs/12-project-architecture.md#error-handling.
- `exceptions/ai.exceptions.ts` — six typed exceptions (a sixth,
  `ProviderFallbackExhaustedException`, added Sprint 18B), all extending
  `HttpException` (the same base every other module already uses).

`TravelModule` now exports `TravelService` (added Sprint 18A, alongside the
rest of the AI work) purely so `ContextBuilder` can inject it — the same
cross-module pattern `PlannerModule`/`UsersModule` already established; no
existing Travel consumer or route changed.

Sprint 18B (First AI Capabilities) activated the Sprint 18A infrastructure:

- `providers/openrouter/openrouter.provider.ts` — a second `AiProvider`,
  calling OpenRouter's OpenAI-compatible `chat/completions` REST endpoint
  via the runtime's native `fetch` rather than a dedicated SDK (OpenRouter
  has no comparable official Node SDK the way Gemini does). Registered
  through the exact same `AI_PROVIDERS` DI factory as `GeminiProvider` — no
  change to `ProviderRegistry`/`ProviderRouter`/`AiService` was needed to
  add it, the concrete proof of Sprint 18A's "pluggable without modifying
  business logic" claim.
- `service/ai-fallback.executor.ts` — walks `ProviderRouter.resolveChain`'s
  ordered candidates, giving each its own full `AiRetryExecutor` attempt
  before advancing to the next; gated by `AI_FALLBACK_ENABLED` (a config
  field Sprint 18A had already reserved for exactly this). Throws
  `ProviderFallbackExhaustedException` only once every candidate has
  failed.
- `capabilities/` — four declarative `AiCapabilityDefinition`s
  (`planner-analyze`, `planner-suggest`, `travel-suggest`, `daily-brief`)
  plus the one generic `AiCapabilityService.run` executor every definition
  shares (reuses `AiService.generate`, then validates the provider's text
  response against the definition's own schema) — not four near-duplicate
  service classes.
- `ai.controller.ts` — this module's first controller:
  `POST /api/v1/ai/{planner/analyze,planner/suggest,travel/suggest,daily-brief}`,
  per [15-api-design.md](../../../docs/15-api-design.md#ai-capabilities).
  Deliberately not a generic chat endpoint.

No Prisma changes, no new tables — every capability endpoint reads through
`ContextBuilder` and returns a suggestion; nothing is persisted or written
back to `Task`/`Trip`/`ItineraryItem`.

Sprint 18B.5 (AI Platform Hardening & Final Refinement) hardened the above
without changing its shape — no new endpoint, no new provider, no
persistence:

- `capabilities/capability.types.ts` — `AiCapabilityDefinition` is now
  generic (`AiCapabilityDefinition<TResult>`) and fully describes its own
  behavior: `outputSchema` (a Zod schema, not just a prompt-facing
  description), `preferredProvider`, `fallbackAllowed`, `temperature`,
  `maxTokens`. `AiCapabilityService.run` reads every one of these — no
  capability-specific branch exists in any service, per this sprint's
  "behavior should be driven by capability metadata" rule. The
  `contextOptions` field was renamed `contextScope` to match this sprint's
  own metadata naming.
- `capabilities/response-validation.util.ts` (renamed from
  `structured-response.util.ts`) — the full "malformed responses must
  never reach API consumers" flow: raw text -> parsed JSON -> Zod-validated,
  typed result. A structurally-valid-but-empty response (e.g. zero
  suggestions from `planner-suggest`) is now rejected too, since every
  suggestions array schema carries `.min(1).max(3)` matching what the
  prompt already asks for.
- `router/provider-router.service.ts` — `resolveChain` now has three
  precedence tiers, not two: a config-driven `AI_FEATURE_PROVIDER_OVERRIDES`
  entry, then a caller-supplied `preferredProviderId` (from
  `AiCapabilityDefinition.preferredProvider` — Planner capabilities prefer
  Gemini, Travel prefers OpenRouter), then plain `providerPriority`. Config
  always wins over a code default, so ops can override routing without a
  redeploy.
- `service/ai-fallback.executor.ts` — `execute` now accepts a per-call
  `fallbackAllowed` option (from `AiCapabilityDefinition.fallbackAllowed`);
  combined with `AI_FALLBACK_ENABLED` as **both must allow it** — global
  config can only further restrict fallback for a capability, never force
  it on for one that opts out.
- `providers/with-timeout.util.ts`, `exceptions/classify-provider-http-error.util.ts`,
  `mapper/generate-result.util.ts` — three small shared utilities extracted
  to remove real duplication between `GeminiProvider`/`OpenRouterProvider`
  (timeout/AbortController boilerplate, HTTP-status classification) and
  between `gemini-response.mapper.ts`/`openrouter-response.mapper.ts`
  (empty-text checking, `AiGenerateResult` assembly). While extracting the
  shared HTTP-status classifier, a real bug was found and fixed: an
  unrecognized/network-level provider error (neither a classified HTTP
  status nor a timeout — e.g. a raw DNS/connection failure) was previously
  classified as `InvalidProviderResponseException` (never retried, no
  fallback); it's now `ProviderUnavailableException` (transient, retried,
  fallback-eligible) — a network failure is far more likely to be a
  transient blip than a malformed response.
- `service/ai-retry.executor.ts` — the backoff base delay (250ms) and
  jitter ratio (20%) are now named constants (`RETRY_BASE_DELAY_MS`,
  `RETRY_JITTER_RATIO`) instead of inline literals; deliberately not new
  env vars — these are algorithm constants, not per-deployment settings
  (unlike `AI_RETRY_COUNT`, which already is one).
- Jest was previously configured but entirely unused (`npm test` had no
  transform for `.ts` files and would have failed immediately) — a
  standard `ts-jest` config was added to `package.json`, and three focused
  unit test suites now cover `ProviderRouter` (precedence rules),
  `AiFallbackExecutor` (fallback gating, all-fail case), and
  `response-validation.util.ts` (parse + schema validation, code-fence
  stripping) — the pure-logic pieces best suited to fast, deterministic
  unit tests, as opposed to every prior sprint's live-E2E-only
  verification style (still used here too, for everything that needs a
  real database/HTTP server).

Sprint 19 (Intelligent Planner) made `planner` AI-powered: six new
capability endpoints under `/planner/ai/...` (`PlannerAiController`/
`PlannerAiService`, in `modules/planner/ai/`), per that sprint's explicit
"Planner owns these capabilities" instruction — distinct from `AiModule`'s
own `/ai/planner/analyze`/`/ai/planner/suggest` (Sprint 18B), which remain
untouched. Every one of the six reuses `AiModule`'s existing execution
engine end-to-end (`AiCapabilityService`, `ProviderRouter`,
`AiFallbackExecutor`, `ContextBuilder`, `PromptBuilder`) — nothing here
duplicates that infrastructure:

- Natural-language task creation (`POST /planner/ai/tasks/draft`) and
  deadline extraction (`POST /planner/ai/deadline-extraction`) parse free
  text into structured, `null`-tolerant fields — never persisted by these
  endpoints.
- Task breakdown (`POST /planner/ai/tasks/:id/breakdown`) is the one route
  that reads persisted data: `PlannerService.getTask`'s existing ownership
  check (404, never 403) runs before the task's fields are ever placed in
  a prompt.
- Schedule analysis (`POST /planner/ai/schedule/analysis`), priority
  suggestion (`POST /planner/ai/priority-suggestion`), and smart tags
  (`POST /planner/ai/tags`) mirror Sprint 18B's `planner-analyze`
  capability shape but with their own, more specific output schemas.

This required three small, additive touches to `AiModule` itself (all
"absolutely necessary" to fulfill this sprint's explicit "reuse AI
Capability Service" instruction, not a redesign):

- `ai.module.ts` now exports `AiCapabilityService` in addition to
  `AiService`, and imports `PlannerModule` via `forwardRef()` — Sprint 19
  makes `AiModule` and `PlannerModule` a genuine, mutual circular module
  dependency (`AiModule` already needed `PlannerModule` for
  `ContextBuilder`; `PlannerModule` now needs `AiModule` for
  `AiCapabilityService`). `TravelModule`'s existing (one-directional, not
  circular on its own) import of `PlannerModule` also needed `forwardRef()`
  once this closed a 3-module cycle
  (`TravelModule -> PlannerModule -> AiModule -> TravelModule`) — confirmed
  by actually booting the app, not just by reasoning about the module
  graph; Node's circular `require` resolution left `PlannerModule`
  `undefined` at that import site without it.
- `capabilities/ai-capability.service.ts`'s `run` method gained an
  optional 4th parameter, `primaryInput?: string` — every Sprint 18B call
  site still passes exactly 3 arguments, unaffected. Sprint 18B's four
  capabilities only ever needed the caller's stored context plus an
  optional `notes` steer; several of Sprint 19's capabilities need to
  operate on specific per-call text/fields that aren't part of any user's
  stored context at all, and `notes` was never meant to carry a
  capability's primary payload.
- `templates/base-system.template.ts` bumped to `v2`: it now states
  today's date (computed from `context.metadata.generatedAt` in the
  caller's own timezone) so relative expressions like "tomorrow" or "next
  Friday" can be resolved correctly — needed by Sprint 19's task-draft and
  deadline-extraction capabilities, but harmless and mildly beneficial to
  every other capability that composes this template too.
- `capabilities/capability.types.ts`'s `AiCapabilityId` widened from a
  4-value union scoped to `AiModule`'s own Sprint 18B capabilities to a
  plain `string` — it was never switched on or used for exhaustiveness
  checking (only logging/attribution), and a closed union can't
  accommodate capability ids owned by other modules.

Sprint 20 (Intelligent Travel) made `travel` AI-powered, the exact same
pattern Sprint 19 established for `planner`: six new capability endpoints
under `/travel/ai/...` (`TravelAiController`/`TravelAiService`, in
`modules/travel/ai/`), per that sprint's explicit "Travel owns its AI
capabilities" instruction — distinct from `AiModule`'s own
`/ai/travel/suggest` (Sprint 18B), which remains untouched. Every one of
the six reuses `AiModule`'s existing execution engine end-to-end — nothing
here duplicates that infrastructure, and nothing in `AiCapabilityService`,
`ProviderRouter`, `AiFallbackExecutor`, `ContextBuilder`, or `PromptBuilder`
changed to support it:

- Natural-language trip drafting (`POST /travel/ai/trips/draft`) and
  travel tags (`POST /travel/ai/tags`) are standalone — free text or plain
  fields, no existing `Trip` involved, never persisted.
- Itinerary generation, budget analysis, packing suggestions, and risk
  analysis (`POST /travel/ai/{itinerary,budget-analysis,packing-list,risk-analysis}`)
  all read an existing trip via `TravelService.getTrip`'s existing
  ownership check (404, never 403) before its fields are ever placed in a
  prompt; risk analysis additionally reads the trip's itinerary via
  `ItineraryService.getItinerary` (also already ownership-checked) so it
  can flag scheduling conflicts and missing transportation gaps, not just
  the trip's own fields.
- Every array field the model can return unprompted-empty (e.g. risk
  analysis's `overlyBusyDays`/`budgetRisks`) uses Zod's `.default([])`
  rather than requiring the model to always emit every key — a genuinely
  empty finding is valid and common (most trips have no risks to report),
  unlike Sprint 19's suggestion arrays, which are never legitimately empty
  by design (`.min(1)`).

This closed a second `AiModule` circular dependency, resolved the same way
Sprint 19's `PlannerModule` pair was: `ai.module.ts` now imports
`TravelModule` via `forwardRef()` too (previously direct — one-directional,
since Travel never imported `AiModule` before), and `travel.module.ts`
imports `AiModule` via `forwardRef()` in return. Re-verified empirically by
booting the app after the change, the same discipline Sprint 19
established — a clean `npm run build` does not, on its own, prove Nest's
runtime DI graph resolves a circular module reference correctly.

Sprint 21 (Daily Brief Intelligence) added `daily-brief`, a brand-new
top-level module and the platform's first genuinely cross-module AI
feature — per that sprint's "Daily Brief becomes the first feature that
understands the user's overall situation rather than a single module"
goal. Six capability endpoints under `/daily-brief/...`
(`DailyBriefController`/`DailyBriefService`), distinct from `AiModule`'s
own single `/ai/daily-brief` capability (Sprint 18B), which remains
untouched:

- Every one of the six uses `templateId: 'assistant'` and the default
  `contextScope` (`{}`) — the first capabilities in the platform
  explicitly combining Planner *and* Travel context together, reusing the
  `assistant` template and `ContextBuilder` exactly as Sprint 18A built
  them, unmodified.
- Unlike Sprint 19/20's per-entity capabilities (`breakdownTask`,
  `generateItinerary`, etc.), none of the six operate on one specific
  existing `Task`/`Trip` — every one reasons only over the caller's own
  aggregate context, so there is no ownership check to perform and no
  `primaryInput` to compose; `notes` is the only ever caller-supplied
  content, reused directly from `AiModule`'s own `AiCapabilityRequestDto`
  rather than a new module-specific request DTO.
- Because of this, `daily-brief.module.ts` imports only `AiModule` (for
  `AiCapabilityService`) — no `PlannerModule`/`TravelModule` import at
  all, and therefore **no new circular dependency**, unlike Sprint 19's
  `PlannerModule` and Sprint 20's `TravelModule`. `AiModule` never needs
  anything from `DailyBriefModule` in return, so the import is plainly
  one-directional.
- "Avoid duplicate context loading" (this sprint's own instruction) is
  satisfied by the existing architecture's own shape, not new caching
  machinery: each capability is one HTTP request triggering exactly one
  `AiService.generate()` call, which builds context exactly once — there
  was never a scenario where the same request would load Planner or
  Travel context twice.
- `priorities`'s `suggestedRank` is a plain 1-based ordinal, deliberately
  not named `priority`, to avoid any confusion with the real, stored
  `TaskPriority` enum field it is never allowed to modify.

Sprint 21.5 (Architecture Review & Platform Stabilization) audited every
module built across Sprints 18A-21 for duplication, dead code, and
consistency, per its explicit "assume all functionality is working, do not
redesign, do not add features" scope. No architecture changed; every
existing endpoint, request/response shape, and Swagger contract is
unchanged. Findings and fixes:

- **Duplicated Swagger error documentation**: the same two `@ApiResponse`
  objects (503 "no provider available", 502 "invalid response") had been
  redeclared three times — a local `PROVIDER_ERROR_RESPONSES` array in
  `travel-ai.controller.ts` and `daily-brief.controller.ts`, and fully
  inline in `planner-ai.controller.ts` (which predates the array pattern
  Sprint 20 introduced and was never retrofitted). Promoted to one shared
  `ApiAiProviderErrorResponses()` decorator in
  `ai/swagger/ai-provider-error-responses.decorator.ts`, applied by all
  three controllers. Swagger output is byte-identical — a documentation-
  authoring change only.
- **Duplicated `NaturalLanguageInputDto`**: Planner's and Travel's `ai/dto/`
  folders each declared an identical `{text: string}` shape (Travel's copy
  was written with an explicit doc-comment flagging it as a future
  promotion candidate once a second feature needed it — Sprint 21.5 is that
  cleanup). Promoted to `common/dto/natural-language-input.dto.ts`; both
  modules now import the shared class instead of a local copy.
- **Duplicated date/time regex literals in Zod schemas**: `planner-ai.capabilities.ts`,
  `travel-ai.capabilities.ts`, and `daily-brief.capabilities.ts` each
  re-declared `/^\d{4}-\d{2}-\d{2}$/` and/or `/^([01]\d|2[0-3]):[0-5]\d$/`
  inline inside `z.string().regex(...)` calls — the exact same patterns
  Sprint 17.5 already promoted to `common/utils/date-time.util.ts`'s
  `DATE_PATTERN`/`TIME_PATTERN` for class-validator `@Matches()` use. A
  plain `RegExp` works identically for Zod's `.regex()`, so all AI capability
  schemas now import and reuse the same two constants instead of
  redeclaring them.
- **Provider id magic strings**: every capability definition across `ai/`,
  `planner/ai/`, `travel/ai/`, and `daily-brief/` wrote `preferredProvider:
  'gemini'`/`'openrouter'` as raw string literals instead of importing the
  `GEMINI_PROVIDER_ID`/`OPENROUTER_PROVIDER_ID` constants `ai.constants.ts`
  already exports (and `GeminiProvider`/`OpenRouterProvider` themselves
  already use). All ~22 call sites now reference the constants.
- **Dead code**: `ProviderRouter.resolve()` (a single-provider convenience
  wrapper around `resolveChain()[0]`) had zero production callers —
  `AiService.generate()` has called `resolveChain()` directly since Sprint
  18B.5's fallback-executor rework, leaving `resolve()` reachable only from
  its own spec test. Removed the method and its two spec assertions;
  `resolveChain()` (still the only thing any real caller uses) is
  unaffected.
- **Module boundaries, dependency graph, and every `forwardRef()` usage**
  (`AiModule<->PlannerModule`, `AiModule<->TravelModule`,
  `TravelModule->PlannerModule`) were re-reviewed against the current,
  final state of all four AI-capability modules: every one is still
  genuinely required (each resolves a real mutual dependency introduced by
  Sprint 19/20's capability modules needing `AiCapabilityService` while
  `AiModule`'s own `ContextBuilder` needs their data services back) — none
  could be removed without removing a shipped feature, so none were
  touched.
- **Logging**: `AiService.logCall`, `GeminiProvider.normalizeError`, and
  `OpenRouterProvider.normalizeError` were re-checked line by line — none
  logs a prompt, a provider response body, an API key, or user context;
  only `error.message` (never a raw prompt/response) reaches a log line.
  No change needed.
- **New tests**: `AiCapabilityService` (the single generic executor every
  capability across every module shares) and `classifyProviderHttpError`
  (the shared HTTP-status classification both providers use) had zero
  direct unit coverage despite being central, reused infrastructure — added
  `ai-capability.service.spec.ts` (9 tests: metadata pass-through,
  instruction composition/ordering, envelope shape, schema-validation
  failure) and `classify-provider-http-error.util.spec.ts` (4 tests: 429,
  5xx, other 4xx, provider id in the message). Did not add per-capability
  schema tests (Planner/Travel/Daily Brief's ~20 Zod schemas) — each would
  mostly re-test Zod's own behavior on a new shape, not genuinely new logic,
  per this sprint's own "do not create meaningless coverage" instruction.

Sprint 22 (Proactive Assistant Engine) added `proactive-assistant`, a
brand-new top-level module and the platform's first genuinely *proactive*
AI feature — every capability before this sprint only answered a caller's
specific request; these analyze the caller's situation unprompted and
surface something worth acting on. Five endpoints under
`/proactive-assistant/...` (`ProactiveAssistantController`/
`ProactiveAssistantService`):

- `suggestions`, `opportunities`, `reminders`, and `insights` are AI
  capabilities built the same way Sprint 21's Daily Brief capabilities are:
  `templateId: 'assistant'`, default `contextScope` (`{}`, both Planner and
  Travel loaded), reusing `AiCapabilityService`/`ProviderRouter`/
  `AiFallbackExecutor`/`ContextBuilder`/`PromptBuilder` unmodified. Every
  result array uses `.max(N).default([])` — an empty list is the correct,
  expected answer when nothing genuinely warrants a suggestion/opportunity/
  reminder/insight right now, never something to fabricate to avoid
  returning empty.
- `reminders` recommends reminders only — this module has no reminder
  entity and no notification channel to write to, so it is architecturally
  incapable of creating or scheduling one, not merely instructed not to.
- Each result item's enum fields (`SuggestionPriority`, `SuggestionConfidence`,
  `OpportunityType`, `ReminderRelatedTo`, `InsightCategory`) are declared
  once, in each DTO file (`dto/*.ts`), and every capability's Zod schema
  validates against the same enum via `z.nativeEnum(...)` rather than
  redeclaring the value list a second time — the AI JSON contract and the
  Swagger-documented response shape can never drift apart.
- `prioritize` is deliberately **not** an AI capability — a caller sends
  back a suggestion list (typically `suggestions`'s own output) and gets a
  ranked, deduplicated, contradiction-filtered list from a pure, deterministic
  algorithm (`prioritization/suggestion-prioritizer.util.ts`): rank by
  priority (weighted highest), then confidence, then soonest `expiresAt`,
  then title, for full determinism; deduplicate by normalized
  (trimmed/lowercased/whitespace-collapsed) title, keeping the
  highest-ranked survivor; then drop the lower-ranked half of any pair
  matching a small explicit table of opposing keyword phrases (e.g. "leave
  earlier" vs. "leave later"). Running this through the AI platform instead
  would cost latency and tokens for well-defined data-transformation work,
  per this sprint's "no duplicated AI calls" performance rule — its
  response therefore has no `provider`/`model`/`usage`/`latencyMs` envelope,
  since no provider is ever called.
- The contradiction-keyword table is intentionally small and literal
  rather than a semantic/NLP approach — a real, working v1 algorithm for
  the pairs this assistant is actually likely to generate; genuine
  natural-language contradiction detection is future work (see Sprint 22's
  own Future Enhancements).
- Like `daily-brief`, none of these five capabilities operate on one
  specific existing `Task`/`Trip`, so `proactive-assistant.module.ts`
  imports only `AiModule` — no circular dependency, no `forwardRef()`.
- New tests: `suggestion-prioritizer.util.spec.ts` (7 tests covering
  ranking order, confidence tie-breaking, `expiresAt` tie-breaking,
  deduplication, contradiction removal, and the pass-through case) and
  `proactive-assistant.service.spec.ts` (6 tests covering correct
  capability-to-method routing, error propagation, and that `prioritize`
  never calls `AiCapabilityService`).

`notifications` remains an empty `@Module({})` shell with no controller,
service, or repository — filled in following the layered architecture in
[12-project-architecture.md](../../../docs/12-project-architecture.md#backend-architecture)
and the module boundaries convention in
[13-folder-structure.md](../../../docs/13-folder-structure.md#backend).

Module names here match the mobile client's `features/*` naming
(`planner`, `travel`) rather than docs/14-database-design.md's
entity-group names (`tasks`, `trips`), per CLAUDE.md's "consistency with
existing patterns" rule applied across the whole stack.

Suggested build order, since later modules depend on earlier ones:

1. ~~`auth`~~ — done (Sprint 14.0). Register/login/refresh/logout; no
   `forgot-password`/`reset-password` yet (docs/15-api-design.md lists both
   under Authentication but this sprint's brief scoped only the four
   above).
2. ~~`users`~~ — done (Sprint 17.0). Profile + preferences on top of
   Sprint 14.0's find/create. `PATCH /users/me/password` (FR-PROFILE-03)
   and session management (FR-PROFILE-05) remain open — that sprint's
   explicit "Do NOT implement: Password reset" scope, and no session routes
   in its 3-route API list.
3. ~~`planner`~~ — done (Sprint 15.0). Full task CRUD + dashboard; no
   `TaskListsController` (see above). Made AI-powered in Sprint 19 (six
   capability endpoints under `/planner/ai/...`).
4. ~~`travel`~~ — done (Sprint 16.0). Full trip + itinerary CRUD, reorder,
   dashboard, and the Planner-task integration (itinerary items optionally
   spawn a linked `Task` with `source = TRAVEL`; Packing Checklist items
   mapping into `Task`s the same way remains a future Travel-side feature,
   not yet built). Made AI-powered in Sprint 20 (six capability endpoints
   under `/travel/ai/...`, including AI-generated packing lists — a
   suggestion only, still not wired into the Packing Checklist gap above).
5. ~~`ai`~~ — infrastructure done (Sprint 18A); first capabilities done
   (Sprint 18B); hardened (Sprint 18B.5). Provider-agnostic
   `AiService`/`ProviderRegistry`/`ProviderRouter`/`AiFallbackExecutor`/
   `GeminiProvider`/`OpenRouterProvider`/`ContextBuilder`/`PromptBuilder`,
   four capability endpoints (`AiController`) each fully described by its
   own metadata (preferred provider, fallback policy, generation
   parameters, a Zod-validated output schema), per
   [12-project-architecture.md](../../../docs/12-project-architecture.md#ai-integration-architecture)
   and [15-api-design.md](../../../docs/15-api-design.md#ai-capabilities).
   Still open, deliberately out of every AI sprint's scope so far: a
   chat/conversation endpoint, persistence (`ai_conversations`/
   `ai_messages` remain absent from `schema.prisma`), per-user rate
   limiting, a circuit breaker, and a third provider — see Sprint 21's
   Future Enhancements for Sprint 22.
6. ~~`daily-brief`~~ — done (Sprint 21). Six cross-module capability
   endpoints (`DailyBriefController`/`DailyBriefService`) combining
   Planner + Travel context via the unmodified `AiCapabilityService`/
   `ContextBuilder`/`assistant` template — no new AI infrastructure, no
   circular dependency (imports only `AiModule`).
7. ~~`proactive-assistant`~~ — done (Sprint 22). The platform's first
   proactive (not reactive) capability set: `suggestions`/`opportunities`/
   `reminders`/`insights` (`ProactiveAssistantController`/
   `ProactiveAssistantService`) reuse `AiCapabilityService`/
   `ContextBuilder`/`assistant` template exactly as `daily-brief` does — no
   new AI infrastructure, no circular dependency (imports only `AiModule`).
   `prioritize` is a deterministic algorithm, not an AI capability — see its
   own section below.
8. `notifications` — no entity or requirement exists yet in
   [07-functional-requirements.md](../../../docs/07-functional-requirements.md)
   or [14-database-design.md](../../../docs/14-database-design.md); needs
   product requirements before implementation. `users.notification_preferences`
   already exists as the future toggle store once this module is built.
