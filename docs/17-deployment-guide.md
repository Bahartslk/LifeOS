# Deployment Guide

How to deploy the backend to Railway and build a release APK that works on any Android device, on any network. Written for a mentor/external-testing release, not a Play Store submission.

## 1. Railway deployment steps

1. Go to [railway.app](https://railway.app) and sign in (GitHub login is simplest, since this project is a git repo).
2. **New Project → Deploy from GitHub repo** → select this repository.
3. Railway will create a service from the repo root. Open that service's **Settings → Root Directory** and set it to:
   ```
   backend
   ```
   This repo is a monorepo (`mobile/` + `backend/`); Railway needs to know only `backend/` is the deployable service. `backend/railway.json` (already committed) takes over from there — it tells Railway to build with Nixpacks, run `npm run build`, start with `npm run start:prod`, and healthcheck `/health`.
4. Add a **PostgreSQL** plugin to the project (**+ New → Database → PostgreSQL**). Railway provisions it and exposes a `DATABASE_URL` variable automatically.
5. In the backend service's **Variables** tab, set every variable listed in section 2 below. For `DATABASE_URL`, reference the Postgres plugin's value: Railway lets you pick `${{Postgres.DATABASE_URL}}` from its variable-reference dropdown instead of retyping it.
6. Deploy. Railway will run (automatically, per `backend/package.json`):
   - `npm install` → triggers `postinstall` → `prisma generate`
   - `npm run build` → `nest build`
   - `npm run start:prod` → `prisma migrate deploy && node dist/main`
7. Once deployed, open the service's **Settings → Networking → Generate Domain** to get a public URL. LifeOS production uses `https://lifeos-production-532b.up.railway.app`. Confirm it's alive:
   ```
   curl https://lifeos-production-532b.up.railway.app/health
   ```
   Expected: `{"status":"ok","timestamp":"...","version":"0.1.0"}`.

## 2. Environment variables (set these in Railway's Variables tab)

| Variable | Value |
|---|---|
| `NODE_ENV` | `production` |
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (variable reference to the Postgres plugin) |
| `JWT_ACCESS_TOKEN_PRIVATE_KEY` | Paste the PEM with real line breaks (multi-line value), **no surrounding quotes** and **not** `\n`-escaped — nothing in the backend unescapes `\n`, so an escaped value fails to parse as a key. Railway's variable editor accepts multi-line text directly. |
| `JWT_ACCESS_TOKEN_PUBLIC_KEY` | Same — paste the matching public key PEM, real line breaks, no quotes. |
| `JWT_ACCESS_TOKEN_TTL` | `15m` (or your preferred value) |
| `JWT_REFRESH_TOKEN_TTL` | `30d` |
| `BCRYPT_SALT_ROUNDS` | `12` |
| `GEMINI_API_KEY` / `OPENROUTER_API_KEY` | Optional — leave unset if you don't need AI features working for this test; the app boots fine without them. |

Do **not** generate new JWT keys unless you want every existing local account invalidated — reuse the key pair already in your local `backend/.env` (never commit that file; it's gitignored).

`PORT` does **not** need to be set — Railway injects its own and the app already reads `process.env.PORT` (`backend/src/config/configuration.ts`).

## 3. Database setup

The Postgres plugin from step 1.4 is all you need — no manual schema setup. `prisma migrate deploy` (wired into `start:prod`) applies every migration in `backend/prisma/migrations/` against it on first boot, in order, including the initial schema and the `add_task_category` migration — creating all tables, the case-insensitive unique email index, the soft-delete partial indexes, and every foreign key exactly as they exist in local dev.

## 4. Prisma migration (already automatic — for reference)

Locally, when you change `schema.prisma`:
```
cd backend
npx prisma migrate dev --name <describe_the_change>
```
This creates a new file under `prisma/migrations/`. Commit it. The next Railway deploy picks it up automatically via `prisma migrate deploy` in `start:prod` — no manual migration step on Railway, ever.

## 5. How to build the Release APK

```
cd mobile
./gradlew :composeApp:assembleRelease
```
The release build connects to the Railway production API by default: `mobile/gradle.properties` sets `LIFEOS_API_BASE_URL=https://lifeos-production-532b.up.railway.app/api/v1`, which `composeApp/build.gradle.kts` writes into `BuildConfig.API_BASE_URL`.

Output: `mobile/composeApp/build/outputs/apk/release/composeApp-release.apk` — already signed (with the project's debug keystore; see section 7's note) and installable on any device via `adb install` or direct file transfer.

Only if `LIFEOS_API_BASE_URL` is missing entirely does the build fall back to an obvious placeholder URL (`https://REPLACE_WITH_DEPLOYED_BACKEND_URL/api/v1`) that won't resolve.

## 6. How to change the API URL in future

Never edit source code for this. The URL is the `LIFEOS_API_BASE_URL` Gradle property:

- **One-off build**: pass `-PLIFEOS_API_BASE_URL=https://new-url/api/v1` on the `gradlew` command line. It overrides the value in `mobile/gradle.properties` for that build only.
- **Project default**: change the `LIFEOS_API_BASE_URL` line in `mobile/gradle.properties` (currently the Railway production API). This is the value every release build uses unless overridden.
- **CI/automated builds**: set the `ORG_GRADLE_PROJECT_LIFEOS_API_BASE_URL` environment variable; Gradle exposes it as the same property.

`mobile/local.properties` is **not** read for this property.

Debug builds are unaffected by all of this — they always use `http://10.0.2.2:3000/api/v1` (the emulator's local-backend alias), matching pre-existing local dev behavior exactly.

## 7. How to publish future versions

1. Bump `versionCode` (must strictly increase) and `versionName` in `mobile/composeApp/build.gradle.kts`'s `defaultConfig`.
2. Rebuild: `./gradlew :composeApp:assembleRelease` (targets the Railway production API by default; see section 6 to point at another backend).
3. Redeploy the backend the same way as before (push to the connected branch — Railway auto-deploys on push once connected; `prisma migrate deploy` only ever applies migrations that haven't run yet, so redeploying is always safe to repeat).
4. Distribute the new APK the same way as the first one.

For an actual Play Store release later (not needed for mentor testing): generate a real upload keystore and replace `signingConfig = signingConfigs.getByName("debug")` in the release build type with a proper `signingConfigs.create("release") { ... }` referencing it — flagged directly in `build.gradle.kts` where that line lives.

## 8. Admin web panel (not deployed yet)

The admin panel in `admin/` (see [`admin/README.md`](../admin/README.md)) is **not deployed**. It has been run and tested only locally, against a local backend and database. Nothing in sections 1–7 changes because of it: Railway keeps building only `backend/`.

When it is deployed, these steps are needed — each one deliberate and separate from merging code:

1. **Host the static build.** `cd admin && npm ci && npm run build` produces `admin/dist/`. Any static host works; it must serve `index.html` for unknown paths (single-page-application fallback), because routes such as `/users/<id>` exist only in the browser.
2. **Point it at the backend.** Set `VITE_API_BASE_URL` at build time to the backend origin, without `/api/v1` (for production: `https://lifeos-production-532b.up.railway.app`). `VITE_*` values end up in the public bundle; they are configuration, never secrets.
3. **Allow the panel's origin on the backend.** Add the panel's public URL to the backend's `CORS_ALLOWED_ORIGINS` variable (comma-separated). With `NODE_ENV=production` and this variable empty, the backend allows no cross-origin browser requests, so the panel cannot work until it is set. This is a Railway variable change and restarts the backend.
4. **Grant an admin account.** No account is an admin by default and no API request can change a role. Run the backend's tool against the production database, dry run first:
   ```
   npm run admin:promote -- <email>
   npm run admin:promote -- <email> --confirm
   ```

Until steps 3 and 4 are done, production is unaffected by the panel's existence: the `/admin/*` routes answer `401`/`403` to everyone.
