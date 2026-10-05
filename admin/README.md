# LifeOS Admin Panel

A web panel for LifeOS administrators: a system-wide dashboard, a searchable user list and a per-user detail view. It is a separate web client of the same backend the mobile app uses, and it is **read-only** — nothing in it creates, changes or deletes data.

The interface is in Turkish, like the mobile app. Code, identifiers and documentation are in English.

## Tech stack

| Area | Technology |
| --- | --- |
| UI | React 19, TypeScript, React Router 7 |
| Build | Vite |
| Styling | Plain CSS with custom properties (no UI framework) |
| HTTP | `fetch`, through one small API client (no axios, no query/state library) |
| Tests | Vitest, Testing Library, jsdom |
| Lint | ESLint (typescript-eslint, react-hooks) |

Runtime dependencies are `react`, `react-dom` and `react-router-dom` only.

## Local development

Requirements: Node.js 20+ and the backend running locally (see the [root README](../README.md#installation)).

```bash
cd admin
npm install
cp .env.example .env.local   # optional; the default already points at http://localhost:3000
npm run dev                  # http://localhost:5173
```

The backend allows cross-origin requests from `http://localhost:5173` (dev) and `http://localhost:4173` (preview) outside production, so no proxy is needed.

Signing in requires an account with the `ADMIN` role. Roles are never changed through the API; grant one to a local account with the backend's command-line tool:

```bash
cd backend
npm run build
npm run admin:promote -- you@example.com            # dry run
npm run admin:promote -- you@example.com --confirm  # make ADMIN
```

### Configuration

| Variable | Meaning | Default |
| --- | --- | --- |
| `VITE_API_BASE_URL` | Backend origin, without the `/api/v1` suffix | `http://localhost:3000` |

`VITE_*` values are compiled into the public JavaScript bundle. They are configuration, not secrets — never put a secret in one. Local `.env*` files are git-ignored; only `.env.example` is committed.

### Commands

| Command | What it does |
| --- | --- |
| `npm run dev` | Development server with hot reload |
| `npm test` | Runs the test suite once (`npm run test:watch` to watch) |
| `npm run typecheck` | TypeScript, no output files |
| `npm run lint` | ESLint |
| `npm run build` | Type-checks, then builds to `dist/` |
| `npm run preview` | Serves the built bundle on port 4173 |

Formatting follows the backend's Prettier configuration (`backend/.prettierrc`); the panel does not carry its own Prettier dependency:

```bash
../backend/node_modules/.bin/prettier --config ../backend/.prettierrc --check "src/**/*.{ts,tsx,css}"
```

## Architecture

```
React + TypeScript + Vite (this folder)
        │  pages → feature api modules → apiClient (fetch)
        ▼
NestJS backend  (/api/v1/auth/*, /api/v1/admin/*)
        │  JwtAuthGuard → RolesGuard → service → repository
        ▼
PostgreSQL
```

The source is organized feature-first, with the same inward dependency direction as the rest of the project:

```
src/
  app/        Route table, signed-in layout (sidebar + top bar)
  core/
    api/      apiClient, error types, in-memory token store
    auth/     AuthProvider (session state), RequireAdmin (route guard)
    config/   Environment access
  features/
    auth/       Login and "access denied" pages
    dashboard/  Dashboard page, its charts and API module
    users/      User list, filters, user detail and API module
  shared/     Reusable UI components, hooks, formatting, stat sections
  styles/     Design tokens and stylesheets
  test/       Test-only helpers (in-memory fake backend, render helpers)
```

Pages never call `fetch`. They call their feature's `api.ts`, which uses the single `apiClient`; that is the only place that knows about URLs, headers, the response envelope and session renewal.

Design tokens in `src/styles/tokens.css` mirror the mobile app's Material 3 theme (`mobile/.../core/designsystem/theme`), so both clients share one visual identity; see [docs/06-design-system.md](../docs/06-design-system.md).

### Authentication and session

- Sign-in uses the same `POST /auth/login` as the mobile app. After it succeeds, the panel calls `GET /admin/session`; the answer to that call — not the `role` in the login response — decides whether the panel opens.
- **Both tokens are kept in memory only.** Nothing is written to `localStorage`, `sessionStorage`, cookies or IndexedDB, and an ESLint rule forbids browser storage in this codebase. Reloading the page therefore signs the admin out.
- On a `401`, the client renews the session once with `POST /auth/refresh` and retries the request once. Renewal is **single-flight**: however many requests fail at the same moment, exactly one refresh call is made. This matters because refresh tokens rotate and the backend treats a reused one as theft, revoking the whole session.
- A `403` is never retried or refreshed. It means the account is not an admin; the panel shows "access denied" and ends the session.
- If renewal fails, the tokens are dropped and the panel returns to the login page.
- Signing out calls `POST /auth/logout`, which revokes the refresh token on the backend, then forgets both tokens.

### Security boundary

**The backend is the security boundary.** Every `/admin/*` route is protected by `JwtAuthGuard` and `RolesGuard`, and the guard reads the caller's current role from the database on every request (see [docs/15-api-design.md](../docs/15-api-design.md#roles)). The role known to the frontend is used only to decide which screen to show; bypassing the route guard in the browser yields no data.

The panel shows only what the admin API returns: account fields and aggregate counts. Password hashes, tokens, bio, avatar, notification preferences and the content of tasks and trips are not part of the API contract and are never displayed.

## Routes

| Path | Screen | Access |
| --- | --- | --- |
| `/login` | Sign-in form | Public |
| `/access-denied` | Shown when the backend answers `403` | Public |
| `/dashboard` | System-wide counts of users, tasks and trips | Admin |
| `/users` | User list with search, role/status filters, sort and paging | Admin |
| `/users/:id` | One user's account fields and own task/trip counts | Admin |

Any other path, including `/`, redirects to `/dashboard` (and from there to `/login` when there is no session). The user list keeps its filters in the URL, e.g. `/users?q=bahar&role=ADMIN&status=all&sort=email`.

## Admin API

The panel consumes these backend endpoints, all under `/api/v1` and documented in [docs/15-api-design.md](../docs/15-api-design.md#admin):

| Method | Path | Used for |
| --- | --- | --- |
| `POST` | `/auth/login` | Sign in |
| `POST` | `/auth/refresh` | Renew the session after a `401` |
| `POST` | `/auth/logout` | Sign out and revoke the refresh token |
| `GET` | `/admin/session` | Confirm admin access |
| `GET` | `/admin/dashboard` | Dashboard figures |
| `GET` | `/admin/users` | User list |
| `GET` | `/admin/users/:id` | User detail |

Contract details the UI follows:

- Single resources arrive as `{ data }`, lists as `{ data, meta }`.
- The user list is cursor-paginated (`limit`, `cursor`) and has **no total count**, so the panel offers "Önceki / Sonraki" and never "page X of Y". Going back uses the cursors remembered in the page.
- List parameters: `q` (2–100 characters), `role` (`USER`, `ADMIN`), `status` (`active`, `deleted`, `all`; default `active`) and `sort` (`createdAt`, `-createdAt`, `email`, `-email`). There is no sort by display name.
- `lastActiveAt` exists only on the user detail, not in the list.
- `users.total` is the number of accounts that are not soft-deleted. `users.activeLast7Days` counts accounts that signed in or renewed a session in the last 7 days; it is derived from refresh-token creation and is **not** a measure of in-app usage. The two are shown as separate figures with that explanation.
- Dashboard "overdue" uses the UTC date; a user's detail uses that user's own timezone.

## Testing

```bash
npm test
```

117 automated tests in 7 files cover:

- **API client** — envelopes, the `401` → refresh → retry flow, single-flight renewal, refresh failure, `403`, `429`, network errors, malformed responses, and that nothing is written to browser storage.
- **Sign-in and routing** — admin and non-admin sign-in, invalid credentials, rate limiting, sign-out, redirects for every protected route, and mid-session `401`/`403`.
- **Dashboard, user list and user detail** — loading, success, empty and error states, search and debounce, filters and sort in the URL, back/forward, cursor paging, soft-deleted accounts, and that sensitive fields are not rendered even if a response were to contain them.

Page tests run the whole app (router, auth, real API client) against a small in-memory fake of the backend in `src/test/`. It exists only for tests and is not part of the build.

Verification at the time of the first release of the panel:

| Check | Result |
| --- | --- |
| Automated tests | 117 / 117 |
| TypeScript | Pass |
| ESLint | Pass |
| Prettier | Pass |
| Production build | Pass |
| Manual browser QA (Microsoft Edge, local backend and database) | 17 / 17 scenarios |

## Deployment

**The panel is not deployed to production yet.** It has only been run locally against a local backend and database.

Deploying it later involves, at minimum:

- Hosting the static build (`npm run build` → `dist/`) with a single-page-application fallback to `index.html`.
- Building with `VITE_API_BASE_URL` set to the production backend origin.
- Adding the panel's public origin to the backend's `CORS_ALLOWED_ORIGINS` variable. In production the backend allows no cross-origin browser requests until this is set (see [docs/15-api-design.md](../docs/15-api-design.md#cors)).
- Granting the `ADMIN` role to a production account with `admin:promote`.

Each of these is a deliberate, separate step; see [docs/17-deployment-guide.md](../docs/17-deployment-guide.md#8-admin-web-panel-not-deployed-yet).

## Known limitations

- Reloading the page signs the admin out (by design, see above). The refresh token of the abandoned session is not revoked by the reload; it stays unused on the backend until it expires.
- Read-only: there are no user management actions.
