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
7. Once deployed, open the service's **Settings → Networking → Generate Domain** to get a public URL, e.g. `https://lifeos-backend-production.up.railway.app`. Confirm it's alive:
   ```
   curl https://<your-domain>/health
   ```
   Expected: `{"status":"ok","timestamp":"...","version":"0.1.0"}`.

## 2. Environment variables (set these in Railway's Variables tab)

| Variable | Value |
|---|---|
| `NODE_ENV` | `production` |
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (variable reference to the Postgres plugin) |
| `JWT_ACCESS_TOKEN_PRIVATE_KEY` | Paste the PEM with real line breaks (multi-line value), **no surrounding quotes** and **not** `\n`-escaped — nothing in the code unescapes `\n`, so an escaped value hands `JwtModule` a string RSA can't parse. Railway's variable editor accepts multi-line text directly. |
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
./gradlew :composeApp:assembleRelease -PLIFEOS_API_BASE_URL=https://<your-railway-domain>/api/v1
```
Output: `mobile/composeApp/build/outputs/apk/release/composeApp-release.apk` — already signed (with the project's debug keystore; see section 6's note) and installable on any device via `adb install` or direct file transfer.

If you don't pass `-PLIFEOS_API_BASE_URL`, the build still succeeds but falls back to an obvious placeholder URL (`https://REPLACE_WITH_DEPLOYED_BACKEND_URL/api/v1`) that won't resolve — always pass the real flag for a release build.

## 6. How to change the API URL in future

Never edit source code for this. Three options, in order of convenience:

- **One-off build**: pass `-PLIFEOS_API_BASE_URL=https://new-url/api/v1` on the `gradlew` command line (overrides everything else).
- **Local persistent default**: add a line to `mobile/gradle.properties` (a commented example is already there):
  ```
  LIFEOS_API_BASE_URL=https://new-url/api/v1
  ```
- **CI/automated builds**: set `LIFEOS_API_BASE_URL` as an environment/secret variable in your CI pipeline instead of a properties file.

Debug builds are unaffected by all of this — they always use `http://10.0.2.2:3000/api/v1` (the emulator's local-backend alias), matching pre-existing local dev behavior exactly.

## 7. How to publish future versions

1. Bump `versionCode` (must strictly increase) and `versionName` in `mobile/composeApp/build.gradle.kts`'s `defaultConfig`.
2. Rebuild: `./gradlew :composeApp:assembleRelease -PLIFEOS_API_BASE_URL=https://<your-railway-domain>/api/v1`.
3. Redeploy the backend the same way as before (push to the connected branch — Railway auto-deploys on push once connected; `prisma migrate deploy` only ever applies migrations that haven't run yet, so redeploying is always safe to repeat).
4. Distribute the new APK the same way as the first one.

For an actual Play Store release later (not needed for mentor testing): generate a real upload keystore and replace `signingConfig = signingConfigs.getByName("debug")` in the release build type with a proper `signingConfigs.create("release") { ... }` referencing it — flagged directly in `build.gradle.kts` where that line lives.
