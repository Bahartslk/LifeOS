# LifeOS

A premium, AI-powered life management platform that unifies travel planning, task management, and intelligent scheduling into one ecosystem — a Kotlin Multiplatform / Compose Multiplatform client backed by a NestJS API.

> For people who juggle travel, tasks, and schedules across multiple disconnected apps, LifeOS keeps everything in one place, on one data model, so an AI assistant always has the full picture instead of a fragment of it. See [`docs/02-product-vision.md`](docs/02-product-vision.md) for the full product vision.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Folder Structure](#folder-structure)
- [Installation](#installation)
- [Backend Requirements](#backend-requirements)
- [Screenshots](#screenshots)
- [Future Roadmap](#future-roadmap)

## Project Overview

LifeOS combines three normally-separate tools — a task planner, a travel planner, and an AI assistant — under one account and one data model, so the assistant and dashboard can reason across trips and tasks together instead of per-app. The client is a single Kotlin Multiplatform / Compose Multiplatform codebase (Android today; iOS source sets are already in place); the backend is a NestJS + PostgreSQL API secured with JWT authentication.

The project has gone through a staged backend-integration migration, replacing an initial fake-data mobile prototype with real, end-to-end backend-backed features one module at a time (see [Future Roadmap](#future-roadmap) for what's already done vs. still pending).

## Features

| Module | Status | Notes |
| --- | --- | --- |
| **Authentication** | ✅ Fully integrated | Register, login, session persistence, automatic bearer-token injection and refresh-on-401 |
| **Planner** | ✅ Fully integrated | Dashboard, task list, task detail, create/delete/toggle-complete tasks, all against the real backend |
| **Travel** | ✅ Fully integrated | Trip list, trip detail, create/update/delete trips, real itinerary timeline and statistics |
| **Home Dashboard** | ✅ Fully integrated | Real greeting, Planner-derived task stats and priorities, Travel-derived upcoming-trip card and highlights — no fabricated data |
| **Profile** | 🔶 UI complete, local-only | Theme preference persists on-device; backend `users` API already exists but isn't wired up yet |
| **AI Assistant** | 🔶 UI complete, local-only | Reads real Planner data for its progress summary; backend AI capabilities (Gemini/OpenRouter) exist but the client doesn't call them yet |

Across every integrated module, the project deliberately never synthesizes fake business data to fill a gap — where the backend has no data for something (e.g. trip weather, flight details, week-over-week productivity trend), the UI shows a real empty state instead of a placeholder value.

## Technologies

| Layer | Technology |
| --- | --- |
| Mobile / Client | Kotlin 2.1, Compose Multiplatform, Material 3 |
| Mobile / Client | Koin 4 (Dependency Injection) |
| Mobile / Client | Ktor Client 3 (networking, JWT bearer + auto-refresh) |
| Mobile / Client | Kotlinx Serialization, kotlinx-datetime |
| Mobile / Client | Coil (image loading), Compose Multiplatform Navigation |
| Backend | NestJS 10, Prisma 5 (ORM) |
| Database | PostgreSQL |
| Infrastructure | Docker / Docker Compose |
| Security | JWT (RS256 access tokens, rotated + hashed refresh tokens) |
| API Documentation | Swagger (`/api/docs`) |
| AI | Gemini API + OpenRouter, provider-agnostic backend infrastructure |

## Architecture

Both the client and the backend follow the same core principles, per [`CLAUDE.md`](CLAUDE.md) and [`docs/12-project-architecture.md`](docs/12-project-architecture.md):

- **Clean Architecture** — strict inward dependency direction: `presentation → domain → data`. Domain layers never depend on a framework or a serialization library.
- **Feature-first organization** — each mobile feature (`auth`, `home`, `planner`, `travel`, `profile`, `ai`) owns its own `presentation/domain/data` slice; each backend module (`auth`, `planner`, `travel`, `users`, `ai`, `daily-brief`) owns its own controller/service/repository slice.
- **MVVM on mobile** — ViewModels expose immutable UI state and one-shot actions over `StateFlow`/`Channel`; Composables are stateless and driven entirely by that state.
- **Repository Pattern** — every feature depends on a repository *interface*; concrete implementations (real, backend-backed) are swapped in via Koin, and no ViewModel or Composable ever calls a data source directly.
- **No repository-to-repository coupling** — cross-feature composition (e.g. Home reading Planner and Travel data, Travel Detail reading Planner tasks) happens at the ViewModel layer through each feature's own use cases, never by one repository importing another.
- **Shared networking, once** — a single Ktor `HttpClient` (with bearer-token injection and automatic refresh-on-401) and a single response-envelope/error-handling convention are reused by every feature's remote data source; nothing duplicates that plumbing.
- **DTO → domain mapping at the edge** — every remote data source decodes backend DTOs and maps them into the same domain models used by fake data during earlier development, so ViewModels and UI never changed shape when a feature moved from fake to real data.

## Folder Structure

```
LifeOS/
├── mobile/                  Kotlin Multiplatform / Compose Multiplatform client
│   └── composeApp/src/commonMain/kotlin/com/lifeos/app/
│       ├── core/            DI, networking, storage, design system, navigation, date utils
│       └── features/
│           ├── auth/        Login, register, session, token refresh
│           ├── home/        Dashboard composed from Planner + Travel
│           ├── planner/     Tasks, calendar, dashboard
│           ├── travel/      Trips, itinerary, AI trip generation (UI)
│           ├── profile/     Account/preferences UI
│           └── ai/          AI Assistant UI
├── backend/                 NestJS API
│   └── src/
│       ├── common/          Guards, filters, interceptors, shared DTOs/utils
│       ├── config/          Environment/config validation
│       └── modules/
│           ├── auth/        JWT auth, refresh-token rotation
│           ├── planner/     Tasks, task lists, dashboard, Planner AI capabilities
│           ├── travel/      Trips, itinerary, Travel AI capabilities
│           ├── users/       Profile + preferences
│           ├── ai/          Provider-agnostic AI infrastructure (Gemini, OpenRouter)
│           └── daily-brief/ Cross-module (Planner + Travel) AI feature
├── docs/                    Product vision, architecture, API design, and process docs
├── design/                  Design assets and design-system references
└── docker/                  Local Postgres / full-stack Docker Compose setup
```

See [`docs/13-folder-structure.md`](docs/13-folder-structure.md) for the fully detailed layout.

## Installation

### Prerequisites

- Android Studio (recent stable) with the Kotlin Multiplatform plugin
- JDK 17+
- Node.js 20+ and npm
- Docker Desktop (for local PostgreSQL)
- An Android emulator or device (API 28+)

### Backend

```bash
cd backend
cp .env.example .env      # fill in DATABASE_URL and an RS256 key pair — see .env.example
npm install
docker compose -f ../docker/docker-compose.yml up postgres -d
npm run prisma:generate
npm run prisma:migrate:dev
npm run start:dev         # http://localhost:3000, Swagger at /api/docs
```

### Mobile

```bash
cd mobile
gradle wrapper --gradle-version 8.10   # only if gradlew is not already present
```

Open `mobile/` in Android Studio, let it sync, and run the `composeApp` Android configuration against an emulator or device.

**Android emulator networking:** the client's `ApiConfig.BASE_URL` points at `10.0.2.2` (the emulator's host-loopback alias), matching a backend running locally on the host machine per the steps above. A physical device needs the host machine's real LAN IP instead.

## Backend Requirements

- **PostgreSQL** (via Docker Compose or a local instance) — connection string in `backend/.env`
- **JWT key pair** — an RS256 private/public key pair for signing access tokens (generation instructions in `backend/.env.example`)
- **AI provider keys (optional)** — `GEMINI_API_KEY` and/or `OPENROUTER_API_KEY` only if exercising the `/api/v1/ai/...` or Planner/Travel AI-capability endpoints; the API boots and every other endpoint works without them
- **Prisma migrations** must be applied (`npm run prisma:migrate:dev`) before first run

## Screenshots

_Screenshots pending — to be added once final UI polish is complete._

<!-- ![Home Dashboard](docs/screenshots/home.png) -->
<!-- ![Planner](docs/screenshots/planner.png) -->
<!-- ![Travel](docs/screenshots/travel.png) -->

## Future Roadmap

Completed so far (see each iteration's own QA report in project history):

1. ✅ Shared networking infrastructure + Authentication
2. ✅ Planner backend integration
3. ✅ Travel backend integration
4. ✅ Home Dashboard integration (composed from real Planner + Travel data)

Planned next:

5. ⏳ Profile backend integration — wire the existing `GET/PATCH /api/v1/users/me` and `PATCH /api/v1/users/preferences` endpoints into the Profile UI
6. ⏳ AI Assistant backend integration — connect the client to the existing Gemini/OpenRouter-backed `/api/v1/ai/...` and Daily Brief endpoints
7. ⏳ Real week-over-week productivity history (currently an honest empty state — no backend endpoint tracks it yet)
8. ⏳ Subtask persistence against the real backend (no backend model yet; toggling a subtask fails explicitly). Planner Calendar already reads the user's real tasks via `GET /api/v1/planner/tasks?dueAfter=&dueBefore=`
9. ⏳ iOS build verification (source sets exist; not yet built/tested on this project)

See [`docs/11-roadmap.md`](docs/11-roadmap.md) for the full, longer-term product roadmap.
