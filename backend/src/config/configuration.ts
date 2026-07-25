/**
 * Typed accessor for environment configuration, consumed via
 * `ConfigService.get<AppConfig>('...')` throughout the app. Centralizing
 * this here means no other file reads `process.env` directly, per
 * docs/12-project-architecture.md#secrets-management.
 */
export interface AppConfig {
  nodeEnv: string;
  port: number;
  database: {
    url: string;
  };
  jwt: {
    accessTokenPrivateKey: string;
    accessTokenPublicKey: string;
    accessTokenTtl: string;
    refreshTokenTtl: string;
  };
  auth: {
    bcryptSaltRounds: number;
  };
  ai: {
    geminiApiKey: string | undefined;
    /** OpenRouter's own API key (Sprint 18B's second provider) — same "optional, provider just reports unavailable without it" pattern as `geminiApiKey`. */
    openRouterApiKey: string | undefined;
    /** Provider id used when no feature-specific/config-based override applies — see `AiModule`'s `ProviderRouter`. */
    defaultProvider: string;
    /** Provider ids the router is allowed to select from; a registered provider not listed here is never routed to even if available. */
    enabledProviders: string[];
    /** Ordered provider ids `ProviderRouter.resolveChain` tries, most-preferred first. */
    providerPriority: string[];
    /** Consulted by `AiFallbackExecutor` (Sprint 18B) — when true, a failed provider (after its own retries are exhausted) falls through to the next entry in `providerPriority`; when false, only the first resolved provider is ever tried. */
    fallbackEnabled: boolean;
    /** Gemini's own model id — read only by `GeminiProvider`. */
    model: string;
    /** OpenRouter's own model id (an OpenRouter model slug, e.g. `"openai/gpt-4o-mini"`) — read only by `OpenRouterProvider`. Deliberately a separate field from `model`, not a rename of it — different providers' model id namespaces mean nothing to each other. */
    openRouterModel: string;
    temperature: number;
    topP: number;
    topK: number;
    maxTokens: number;
    timeoutMs: number;
    retryCount: number;
    /**
     * Raw `"feature=providerId"` pairs (e.g. `{ planner: "gemini", travel:
     * "openrouter" }`) — kept as a plain string map here since this config
     * layer never depends on a feature module's types (the same reason
     * `AiFeature`/`ProviderId` aren't imported here); `AiConfigService`
     * narrows this to `Partial<Record<AiFeature, ProviderId>>` at the AI
     * module boundary. Consulted by `ProviderRouter.resolveChain`.
     */
    featureProviderOverrides: Record<string, string>;
  };
}

/** Splits a comma-separated env var into trimmed, non-empty entries. */
function parseList(value: string | undefined, fallback: string[]): string[] {
  if (!value) return fallback;
  const items = value
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
  return items.length > 0 ? items : fallback;
}

/** Parses `"key1=value1,key2=value2"` into a plain object; malformed/empty pairs are skipped rather than throwing, since this drives optional routing, not a required boot-time setting. */
function parseKeyValueMap(value: string | undefined): Record<string, string> {
  if (!value) return {};
  const entries = value
    .split(',')
    .map((pair) => pair.trim())
    .filter((pair) => pair.length > 0)
    .map((pair): [string, string] => {
      const [key, val] = pair.split('=').map((part) => part.trim());
      return [key ?? '', val ?? ''];
    })
    .filter(([key, val]) => key.length > 0 && val.length > 0);
  return Object.fromEntries(entries);
}

export default (): AppConfig => ({
  nodeEnv: process.env.NODE_ENV ?? 'development',
  port: parseInt(process.env.PORT ?? '3000', 10),
  database: {
    url: process.env.DATABASE_URL as string,
  },
  jwt: {
    accessTokenPrivateKey: process.env.JWT_ACCESS_TOKEN_PRIVATE_KEY as string,
    accessTokenPublicKey: process.env.JWT_ACCESS_TOKEN_PUBLIC_KEY as string,
    accessTokenTtl: process.env.JWT_ACCESS_TOKEN_TTL ?? '15m',
    refreshTokenTtl: process.env.JWT_REFRESH_TOKEN_TTL ?? '30d',
  },
  auth: {
    bcryptSaltRounds: parseInt(process.env.BCRYPT_SALT_ROUNDS ?? '12', 10),
  },
  ai: {
    geminiApiKey: process.env.GEMINI_API_KEY,
    openRouterApiKey: process.env.OPENROUTER_API_KEY,
    defaultProvider: process.env.AI_DEFAULT_PROVIDER ?? 'gemini',
    enabledProviders: parseList(process.env.AI_ENABLED_PROVIDERS, ['gemini', 'openrouter']),
    providerPriority: parseList(process.env.AI_PROVIDER_PRIORITY, ['gemini', 'openrouter']),
    fallbackEnabled: (process.env.AI_FALLBACK_ENABLED ?? 'false') === 'true',
    model: process.env.AI_MODEL ?? 'gemini-2.0-flash',
    openRouterModel: process.env.AI_OPENROUTER_MODEL ?? 'openai/gpt-4o-mini',
    temperature: parseFloat(process.env.AI_TEMPERATURE ?? '0.7'),
    topP: parseFloat(process.env.AI_TOP_P ?? '0.95'),
    topK: parseInt(process.env.AI_TOP_K ?? '40', 10),
    maxTokens: parseInt(process.env.AI_MAX_TOKENS ?? '1024', 10),
    timeoutMs: parseInt(process.env.AI_TIMEOUT_MS ?? '15000', 10),
    retryCount: parseInt(process.env.AI_RETRY_COUNT ?? '2', 10),
    featureProviderOverrides: parseKeyValueMap(process.env.AI_FEATURE_PROVIDER_OVERRIDES),
  },
});
