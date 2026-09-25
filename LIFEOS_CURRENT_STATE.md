# LifeOS — Current State Report

| Alan | Değer |
|---|---|
| Rapor tarihi | 2026-09-25 |
| İncelenen commit | `5810cce` (master = origin/master) + 7 commit edilmemiş dosya |
| Yöntem | Kod incelemesi + backend'in yerelde çalıştırılması + Android emülatöründe uçtan uca arayüz testi |

Durum etiketleri: ✅ Gerçekten çalışıyor (bugün doğrulandı) · 🟡 Kısmen · 🔴 Çalışmıyor / bug · 🟣 Mock/Fake · ⚪ Uygulanmamış · ❓ Doğrulanamadı

---

## 1. Project Overview

LifeOS; görev yönetimi (Planner), seyahat planlama (Travel), ana sayfa panosu ve bir AI asistanını tek hesap altında birleştiren bir mobil uygulamadır.

- `mobile/` — Kotlin Multiplatform / Compose Multiplatform istemci (Android çalışıyor, iOS kaynakları var ama derlenmedi)
- `backend/` — NestJS + Prisma + PostgreSQL REST API
- `docs/` — 17 ürün/mimari belgesi (`docs/01-project-overview.md` … `docs/17-deployment-guide.md`)
- `design/stitch/` — 12 ekran tasarımı (PNG)
- `docker/` — `docker-compose.yml` (postgres + backend), `backend.Dockerfile`

Ölçek: mobilde 314 `.kt` dosyası, backend'de test dışı 160 `.ts` dosyası, 54 REST uç noktası.

## 2. Tech Stack

| Katman | Teknoloji (sürüm kaynağı) |
|---|---|
| Mobil | Kotlin 2.1.0, Compose Multiplatform 1.7.1, AGP 8.7.2 (`mobile/gradle/libs.versions.toml`) |
| Mobil DI / ağ | Koin 4.0.0, Ktor Client 3.0.1 (Auth, ContentNegotiation, Logging) |
| Mobil diğer | kotlinx-serialization 1.7.3, kotlinx-coroutines 1.9.0, kotlinx-datetime 0.6.1, DataStore, Coil |
| Backend | NestJS 10.4, Prisma 5.20, TypeScript 5.6 (`backend/package.json`) |
| Veritabanı | PostgreSQL 16 (`docker/docker-compose.yml`, `postgres:16-alpine`) |
| Güvenlik | JWT RS256 access token + hash'lenmiş, rotate edilen refresh token, bcrypt |
| API belgeleri | Swagger, `/api/docs` |
| AI | `@google/genai` (Gemini) + OpenRouter, sağlayıcıdan bağımsız altyapı |

## 3. Mobile Architecture

Kök: `mobile/composeApp/src/commonMain/kotlin/com/lifeos/app/`

- **Clean Architecture + feature-first:** Her özellik `features/<ad>/{data,domain,presentation,di}` yapısında: `auth`, `home`, `planner`, `travel`, `profile`, `ai`.
- **MVVM + tek yönlü akış (MVI benzeri):** Her ekranda `*Contract.kt` bulunur (`UiState` + `Event` + `Action`). ViewModel durumu `StateFlow`, tek seferlik eylemleri `Channel` ile yayar. Örnek: `features/profile/presentation/ProfileContract.kt`, `ProfileViewModel.kt`. Eylemler `core/presentation/CollectActions.kt` ile toplanır.
- **Katmanlar:** UseCase (`features/*/domain/usecase/`) → Repository arayüzü (`features/*/domain/repository/`) → RepositoryImpl (`features/*/data/repository/`) → DataSource (`features/*/data/remote/*RemoteDataSource.kt` veya `data/datasource/Fake*`).
- **DI:** `core/di/AppModule.kt`, `NetworkModule.kt`, `StorageModule.kt`, `PlatformModule.kt` ve her özelliğin `di/*Module.kt` dosyası.
- **Navigation:** `core/navigation/Destination.kt` (15 hedef), `LifeOSNavHost.kt`, `MainNavGraph.kt`, `MainScreen.kt` (alt menü: Ana Sayfa, Seyahat, Ajanda, AI Asistan, Profil), `features/auth/presentation/navigation/AuthNavGraph.kt`.
- **Ağ:** `core/network/HttpClientFactory.kt` (tek Ktor istemcisi, bearer token + 401'de otomatik refresh), `ApiEnvelope.kt` (`{ data: ... }` zarfı), `HttpResponseExtensions.kt` (`ApiException`), `ApiConfig.kt` (+ `ApiConfig.android.kt` → `BuildConfig.API_BASE_URL`).
- **Local storage:** `core/storage/DataStoreFactory.kt`, `PreferencesStorage.kt`, `ThemePreferenceStorage.kt`; oturum `features/auth/data/local/AuthTokenLocalDataSource.kt` içinde.
- **Design System:** `core/designsystem/theme/` (Color, Type, Shape, Dimens, Elevation, Gradient, Animation, Sizing, Theme) ve `core/designsystem/components/` (27 bileşen: AppButton, AppTextField, PasswordTextField, EmptyState, ErrorView, LoadingIndicator, SkeletonLoader, ConfirmationDialog …).
- **Loading/Empty/Error:** Emülatörde doğrulandı. Yeni kullanıcıda Seyahat ("Henüz seyahatiniz yok"), Ajanda ("Henüz göreviniz yok") ve AI Asistan boş durumları doğru görünüyor.
- **Tarih:** `core/date/AppDateParser.kt`, `AppDateFormatter.kt`, `AppToday.kt`, `RelativeDateFormatter.kt`.

## 4. Backend Architecture

Kök: `backend/src/`

- **Modüller (`modules/`):** `auth`, `users`, `planner` (+ `planner/ai`), `travel` (+ `travel/ai`, itinerary), `ai`, `daily-brief`, `proactive-assistant`, `notifications`.
- **Katmanlar:** controller → service → repository (`modules/*/repositories/*.repository.ts`) → PrismaService.
- **Ortak (`common/`):** guards, `filters/all-exceptions.filter.ts` (yapılandırılmış hata yanıtı: `statusCode`, `error`, `message`, `path`, `timestamp`), interceptors (`{ data }` zarfı), decorators, strategies, dto, utils.
- **Config:** `config/configuration.ts`, `config/env.validation.ts` (Joi ile ortam değişkeni doğrulaması).
- **Validation:** Global ValidationPipe (whitelist + forbidNonWhitelisted). Bilinmeyen alan içeren istek 400 alıyor (doğrulandı).
- **Health:** `health/health.controller.ts` → `GET /health`.
- **Commit edilmemiş değişiklik:** `main.ts` içinde HTTP istek günlükleme ara katmanı ve `app.listen(port, '0.0.0.0')`.
- **Testler:** 7 test takımı / 43 test (yalnızca AI router/fallback/validation ve proactive prioritizer). Controller/e2e testi yok; `test/jest-e2e.json` script'i tanımlı ama `test/` klasörü yok.

## 5. Database

`backend/prisma/schema.prisma`

- **Modeller:** `User`, `RefreshToken`, `TaskList`, `Task`, `Trip`, `ItineraryItem`.
- **Enum'lar:** TaskCategory, ThemeMode, TaskPriority, TaskStatus, TaskSource, TaskListType, TripStatus, TripCategory, ItineraryItemType, TransportationType, AccommodationType.
- **Migration'lar (8):** `20260720141630_init_user`, `20260720152452_add_refresh_tokens`, `20260720155556_add_planner`, `20260720181110_add_travel`, `20260721054839_add_user_profile_fields`, `20260727160554_add_trip_category`, `20260727190120_add_trip_weather`, `20260729114055_add_task_category`.
- **Yerel durum ✅:** `prisma migrate status` → "Database schema is up to date!". Tablolar mevcut; test öncesi 36 users, 26 tasks, 20 trips, 107 refresh_tokens vardı.
- **Seed:** Seed script'i yok ⚪.
- Tasarım kuralları (UUID PK, soft delete, FK, index) `docs/14-database-design.md` içinde belgelenmiş.

## 6. Authentication

| Adım | Durum | Kanıt |
|---|---|---|
| Register (API) | ✅ | `POST /api/v1/auth/register` → 201, access + refresh token |
| Register (UI) | ✅ | Emülatörde form → ana sayfa |
| Duplicate e-posta | ✅ | 409 → mobilde `EmailAlreadyRegisteredException` |
| Login (API + UI) | ✅ | 200; yanlış şifrede 401 → "E-posta veya şifre hatalı." |
| Korumalı uç noktalar | ✅ | Token yoksa `GET /users/me` 401, varsa 200 |
| Refresh + rotation | ✅ | Yeni token alınıyor, eski refresh token tekrar kullanılınca 401 |
| Logout (API) | ✅ | `POST /auth/logout` 200 |
| Logout (UI) | 🔴 | `features/profile/presentation/ProfileViewModel.kt:110` `confirmLogout()` yalnızca "yakında" mesajı gösteriyor; `LogoutUseCase` Koin'de tanımlı ama hiç kullanılmıyor |
| Oturum kalıcılığı | ✅ | Uygulama kapatılıp açılınca doğrudan ana sayfa açılıyor |
| Forgot password | 🟣 | Arayüz "E-posta Gönderildi" diyor, ancak `AuthRepositoryImpl.requestPasswordReset` her zaman başarılı dönen bir stub; backend `/auth/forgot-password` → 404 |
| Google ile giriş | ⚪ | Yalnızca "yakında" |

## 7. API

54 uç nokta tanımlı; Swagger `GET /api/docs` 200, `/api/docs-json` 200.

| Grup | Uç noktalar | Mobil kullanıyor mu? |
|---|---|---|
| health | `GET /health` | – |
| auth | register, login, refresh, logout | ✅ |
| users | `GET/PATCH /users/me`, `PATCH /users/preferences` | ⚪ hayır |
| planner | tasks CRUD, complete/incomplete, dashboard | ✅ |
| travel | trips CRUD, itinerary, reorder, dashboard | ✅ (reorder ve itinerary düzenleme hariç) |
| travel/itinerary | `PATCH/DELETE :id` | ⚪ hayır |
| ai | planner/analyze, planner/suggest, travel/suggest, daily-brief | ⚪ hayır |
| planner/ai | 6 yetenek | ⚪ hayır |
| travel/ai | 6 yetenek | ⚪ hayır |
| daily-brief | 6 uç nokta | ⚪ hayır |
| proactive-assistant | 5 uç nokta | ⚪ hayır |

Bugün doğrulananlar: planner/dashboard, planner/tasks (GET, POST), travel/dashboard, travel/trips (GET, POST) → 200/201. Türkçe karakterler (`Muğla`, `Türkiye`) doğru saklanıyor.

## 8. AI Integration

- **Altyapı (backend):** `modules/ai/interfaces/ai-provider.interface.ts` (`AiProvider.generate()`), sağlayıcılar `modules/ai/providers/gemini`, `providers/openrouter`; `registry/`, `router/provider-router.service.ts`, `service/ai-fallback.executor.ts`, `templates/` (sürümlü prompt şablonları), `capabilities/`.
- **Modeller (`backend/.env`):** `AI_MODEL=gemini-2.0-flash`, `AI_OPENROUTER_MODEL=openai/gpt-4o-mini`, `AI_FALLBACK_ENABLED=false`.
- **Gerçek çağrı durumu ❓/🔴:** Yerel `.env` dosyasında `GEMINI_API_KEY` ve `OPENROUTER_API_KEY` boş. `POST /ai/planner/suggest`, `/daily-brief/summary` ve `/proactive-assistant/suggestions` → **503 "AI provider "gemini" is not available."** Hata düzgün yönetiliyor, ancak gerçek bir AI yanıtı bugün doğrulanamadı.
- **Mobil tarafta AI:**
  - 🟣 "AI Seyahat Planlayıcı": `features/travel/data/repository/FakeTripGenerationRepository.kt` 2,5 sn bekleyip `FakeTripGenerationDataSource.kt` şablonlarından plan üretiyor. Backend'deki AI uç noktaları çağrılmıyor. Kaydet adımı ise gerçek `POST /travel/trips`.
  - 🟡 "AI Asistan" ekranı: `features/ai/domain/usecase/BuildAiTaskContextUseCase.kt` görevleri yerelde sınıflandırıyor (bugün, yaklaşan, yüksek öncelikli, gecikmiş). Dil modeli çağrısı yok.
  - 🟣 "Bugünkü planın yapay zeka tarafından optimize edildi" gibi metinler statik; arkasında AI çağrısı yok.

## 9. Implemented Features

Auth (splash, onboarding, login, register, forgot password arayüzü), Home, Planner (liste, detay, oluşturma, takvim), Travel (liste, detay, AI planlayıcı arayüzü), AI Asistan (bağlam özeti), Profil (tema ve ayarlar), backend'de 54 uç nokta.

## 10. Working Features (bugün doğrulandı ✅)

- Splash → 3 adımlı Onboarding → Login (emülatör)
- Register, Login, yanlış şifre uyarısı, oturum kalıcılığı (emülatör + API)
- Home: gerçek `planner/dashboard` + `travel/dashboard` verisiyle açılıyor
- Planner: arayüzden görev oluşturma (`POST /planner/tasks` 201), ajandada görünme
- Travel ve Planner boş durumları; AI Asistan ekranı açılıyor
- Profil: kullanıcı adı ve e-postası gerçek oturumdan geliyor; tema seçimi yerelde kalıcı
- Backend: health, Swagger, JWT akışı, refresh rotation, doğrulama hataları, PostgreSQL bağlantısı
- Backend unit testleri 43/43, `tsc --noEmit` 0 hata, `nest build` başarılı
- Android debug + release derlemesi başarılı

## 11. Partial Features 🟡

- **Planner:** Takvim ayı ve alt görev işaretleme hâlâ `FakePlannerDataSource` üzerinden (`features/planner/data/repository/PlannerRepositoryImpl.kt:48, 92, 113, 116`).
- **Profil:** Yalnızca tema kalıcı. Profili düzenle, şifre değiştir, dil, bildirimler vb. "yakında" mesajı gösteriyor; backend `users` uç noktaları mobile bağlı değil.
- **AI Asistan:** Yalnızca yerel görev özeti, sohbet veya LLM yok.
- **Görev eki ekleme ve görev için AI önerileri:** "yakında" mesajı.
- **iOS:** `iosMain` kaynakları var, hiç derlenmedi ❓.

## 12. Mock/Fake Features 🟣

| Özellik | Dosya |
|---|---|
| AI seyahat planı üretimi | `features/travel/data/repository/FakeTripGenerationRepository.kt`, `data/datasource/FakeTripGenerationDataSource.kt` |
| Planner takvim ve alt görevler | `features/planner/data/datasource/FakePlannerDataSource.kt` |
| Şifre sıfırlama | `features/auth/data/repository/AuthRepositoryImpl.kt` → `requestPasswordReset` (stub) |
| `FakeTravelDataSource` | Yalnızca taslak seyahat kimliği üretmek için kullanılıyor |

## 13. Known Bugs 🔴

1. **Logout arayüzde çalışmıyor:** `ProfileViewModel.confirmLogout()` yalnızca mesaj gösteriyor.
2. **Tarih ayrıştırıcı ASCII "yarin" girişini reddediyor:** Emülatörde "Yarin 14:00" → "Tarih anlaşılamadı". Ayrıştırıcı yalnızca `"yarın"`/`"bugün"` anahtar kelimelerini tanıyor (`core/date/AppDateParser.kt:103-104`); Türkçe klavyesi olmayan kullanıcılar etkileniyor.
3. **Uygulama sürümü tutarsız:** Profilde "1.0.0" (`features/profile/presentation/ProfileStrings.kt:52`), Gradle'da `versionName = "0.1.0"`.
4. **Kayıt formu klavye açılınca kaymıyor:** Emülatörde "Şifre Tekrar" alanı klavyenin altında kaldı. Kullanıcı elle kaydırabiliyor; ekran ayarına göre değişebilir.
5. **"Şifremi unuttum" yanıltıcı başarı mesajı gösteriyor** (bkz. Mock).

## 14. Technical Risks

- **Railway dağıtımı yayında değil:** `https://lifeos-production-532b.up.railway.app/health` → Railway 404 "Application not found" (25.09.2026). Release APK bu adrese bağlı, yani **mevcut release APK şu an başka bir telefonda çalışmaz.**
- **Release APK debug keystore ile imzalı:** `composeApp/build.gradle.kts:131` `signingConfig = signingConfigs.getByName("debug")`. Demo için yeterli, mağaza için değil.
- **Commit edilmemiş 7 dosya:** Telefonda test edilen son sürümün önemli kısmı yalnızca yerelde duruyor. Disk kaybında geri gelmez.
- **Test kapsamı düşük:** Mobilde test yok; backend'de controller/e2e testi yok.
- **AI anahtarları yerelde boş:** AI özellikleri canlıda nasıl davranacak, doğrulanmadı.
- **Git geçmişi çok kaba:** İlk commit 561 dosya içeriyor, bu da ince taneli geri dönüşü zorlaştırıyor.
- **Önemsiz artıklar:** `backend/package.json;C` adında boş bir klasör var (izlenmiyor). `.claude/settings.json` izleniyor ve eski yerel yollar içeriyor (gizli bilgi yok).

## 15. Git Status

- **Remote:** `origin https://github.com/Bahartslk/LifeOS.git`
- **Branch:** `master`, `origin/master` ile aynı commit (`5810cce`); push veya pull bekleyen commit yok (`git ls-remote` ile doğrulandı).
- **Tag:** yok.
- **Commit'ler:** `e0619a7` Initial commit · `6f5c135` README · `9f48d93` travel category/weather/Türkçe tarih düzeltmesi · `5810cce` Railway hazırlığı.
- **Commit edilmemiş (değiştirilmedi):**
  - `backend/.env.example`
  - `backend/railway.json` (healthcheckTimeout 300)
  - `backend/src/main.ts` (HTTP log + 0.0.0.0)
  - `docs/17-deployment-guide.md`
  - `mobile/.../auth/data/repository/AuthRepositoryImpl.kt` (hata loglama)
  - `mobile/.../auth/presentation/register/RegisterViewModel.kt` (hata loglama)
  - `mobile/gradle.properties` (`LIFEOS_API_BASE_URL` = Railway adresi)
- **Untracked:** Bu rapor dosyası dışında yok.
- **Gizli bilgi kontrolü ✅:** `.gitignore` `.env`, `.env.*` (`.env.example` hariç), `*.pem` ve `local.properties` dosyalarını kapsıyor. `backend/.env`, `backend/.devkeys/jwt-*.pem` ve `mobile/local.properties` ignore ediliyor. İzlenen dosyalarda ve tüm git geçmişinde API anahtarı, private key veya token deseni bulunamadı. `.env.example` yalnızca yerel geliştirme değeri içeriyor (`lifeos:lifeos@localhost`).

## 16. Build Status

| Komut | Sonuç |
|---|---|
| `backend: npx tsc --noEmit` | ✅ 0 hata |
| `backend: npx jest` | ✅ 7 takım / 43 test geçti |
| `backend: nest build` | ✅ |
| `backend: node dist/main` (yerel) | ✅ başladı; `/health` 200 |
| `mobile: ./gradlew :composeApp:assembleDebug :composeApp:assembleRelease` | ✅ BUILD SUCCESSFUL |

## 17. APK Status

- `mobile/composeApp/build/outputs/apk/debug/composeApp-debug.apk`: 25.09.2026'da yeniden derlendi, `API_BASE_URL = http://10.0.2.2:3000/api/v1`. Emülatörde test edildi.
- `mobile/composeApp/build/outputs/apk/release/composeApp-release.apk`: 29.07.2026 23:52. Gradle bugün bu APK'yı **UP-TO-DATE** saydı; yani mevcut çalışma ağacı (commit edilmemiş değişiklikler dahil) bu APK ile aynı girdilere sahip. `API_BASE_URL = https://lifeos-production-532b.up.railway.app/api/v1`, bu adres şu an kapalı.
- `LifeOS-Staj-Demo-v0.1.0.apk` henüz oluşturulmadı: backend adresi kapalıyken çalışmayan bir "demo" APK üretmemek için bekletildi.

## 18. Environment Variables

`backend/.env` (değerler rapora yazılmadı):

- **Dolu:** `NODE_ENV`, `PORT`, `POSTGRES_*`, `DATABASE_URL`, `JWT_ACCESS_TOKEN_PRIVATE_KEY`, `JWT_ACCESS_TOKEN_PUBLIC_KEY` (çok satırlı PEM), `JWT_ACCESS_TOKEN_TTL=15m`, `JWT_REFRESH_TOKEN_TTL=30d`, `BCRYPT_SALT_ROUNDS=12`, tüm `AI_*` ayarları.
- **Boş:** `GEMINI_API_KEY`, `OPENROUTER_API_KEY`, `AI_FEATURE_PROVIDER_OVERRIDES`.

Mobil: `LIFEOS_API_BASE_URL` (Gradle özelliği; `mobile/gradle.properties` içinde, commit edilmemiş). Railway için gerekenler `docs/17-deployment-guide.md` içinde listeli.

## 19. Current Limitations

- Canlı backend yok, bu yüzden release APK yalnızca yeniden dağıtımdan sonra çalışır.
- AI özellikleri kullanıcıya gerçek bir LLM çıktısı göstermiyor.
- Logout, şifre sıfırlama ve profil düzenleme yok.
- Yalnızca Android doğrulandı.

## 20. Recommended Next Steps

1. **Baseline'ı koru (onay gerekli):** Commit edilmemiş 7 dosyayı tek bir commit'le kaydet, `staj-demo-baseline` branch'ini ve `v0.1.0-staj-demo` tag'ini oluştur, ardından push et.
2. **Backend'i yeniden yayına al** (Railway'de servis veya plan durumunu kontrol et) ya da demo için başka bir adres belirle. Sonra release APK'yı `-PLIFEOS_API_BASE_URL` ile yeniden derleyip `LifeOS-Staj-Demo-v0.1.0.apk` olarak kaydet.
3. **Küçük, düşük riskli düzeltmeler:** Logout'u `LogoutUseCase`'e bağla, ASCII "yarin"/"bugun" desteği ekle, sürüm etiketini `versionName` ile eşitle, kayıt formunda IME padding ekle.
4. AI anahtarlarıyla gerçek AI yanıtlarını doğrula, ardından mobildeki `FakeTripGenerationRepository`'yi backend `travel/ai` uç noktalarına bağlamayı planla.
5. Kritik akışlar için en az backend e2e testleri ekle.
