import * as Joi from 'joi';

/**
 * Validates process.env at bootstrap time so the application fails fast
 * with a clear error if a required variable is missing or malformed,
 * instead of failing later with a confusing runtime error deep inside a
 * request handler.
 *
 * JWT keys are asymmetric (RS256) rather than a shared secret, per
 * docs/12-project-architecture.md#jwt-strategy — this lets any API instance
 * verify a token with only the public key, without holding the private
 * signing key, once the API scales to multiple instances
 * (docs/12-project-architecture.md#scalability--production-readiness).
 * Refresh tokens are opaque, hashed server-side (not JWTs), so they need a
 * TTL only, no signing key.
 *
 * GEMINI_API_KEY remains optional here even though Sprint 18A's `AiModule`
 * now reads it (via `AiConfigService`) — making it required would break
 * every existing dev/CI environment that boots the app without an AI key,
 * a real behavioral regression for Auth/Users/Planner/Travel this sprint
 * must not cause. Instead, `GeminiProvider.isAvailable()` returns `false`
 * without a key, and `ProviderRegistry`/`ProviderRouter` simply route
 * around an unavailable provider — the app boots fine either way; only an
 * actual `AiService.generate()` call without a configured provider fails,
 * with a clear `ProviderUnavailableException`. Every other `AI_*` variable
 * below has a safe default for the same reason — Sprint 18A adds no new
 * required configuration to the existing boot sequence.
 */
export const envValidationSchema = Joi.object({
  NODE_ENV: Joi.string().valid('development', 'test', 'production').default('development'),

  PORT: Joi.number().port().default(3000),

  // Database — consumed by PrismaService via DATABASE_URL, per
  // docs/14-database-design.md
  DATABASE_URL: Joi.string().uri().required(),

  // JWT access tokens — RS256, per docs/12-project-architecture.md#jwt-strategy
  JWT_ACCESS_TOKEN_PRIVATE_KEY: Joi.string().required(),
  JWT_ACCESS_TOKEN_PUBLIC_KEY: Joi.string().required(),
  JWT_ACCESS_TOKEN_TTL: Joi.string().default('15m'),

  // Refresh tokens — opaque, hashed server-side; TTL only
  JWT_REFRESH_TOKEN_TTL: Joi.string().default('30d'),

  // Password hashing cost factor (bcrypt), per
  // docs/12-project-architecture.md#password-hashing. Configurable rather
  // than hardcoded so it can be tuned per deployment's hardware without a
  // code change, matching the JWT TTLs above.
  BCRYPT_SALT_ROUNDS: Joi.number().integer().min(10).max(15).default(12),

  // AI — Sprint 18A (AI Foundation) and Sprint 18B (First AI Capabilities).
  // GEMINI_API_KEY/OPENROUTER_API_KEY stay optional (see this file's top
  // doc comment); everything else has a safe default so an environment
  // that predates either sprint still boots unchanged.
  GEMINI_API_KEY: Joi.string().optional().allow(''),
  OPENROUTER_API_KEY: Joi.string().optional().allow(''),
  AI_DEFAULT_PROVIDER: Joi.string().default('gemini'),
  AI_ENABLED_PROVIDERS: Joi.string().optional().allow(''),
  AI_PROVIDER_PRIORITY: Joi.string().optional().allow(''),
  AI_FALLBACK_ENABLED: Joi.boolean().default(false),
  AI_MODEL: Joi.string().default('gemini-2.0-flash'),
  AI_OPENROUTER_MODEL: Joi.string().default('openai/gpt-4o-mini'),
  AI_TEMPERATURE: Joi.number().min(0).max(2).default(0.7),
  AI_TOP_P: Joi.number().min(0).max(1).default(0.95),
  AI_TOP_K: Joi.number().integer().min(1).default(40),
  AI_MAX_TOKENS: Joi.number().integer().min(1).default(1024),
  AI_TIMEOUT_MS: Joi.number().integer().min(1000).default(15000),
  AI_RETRY_COUNT: Joi.number().integer().min(0).max(5).default(2),
  // "feature=providerId,feature2=providerId2" — see ProviderRouter.resolveChain.
  AI_FEATURE_PROVIDER_OVERRIDES: Joi.string().optional().allow(''),
});
