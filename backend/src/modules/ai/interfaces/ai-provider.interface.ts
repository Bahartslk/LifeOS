import { ProviderId } from '../types/ai.types';

export interface AiGenerateOptions {
  temperature?: number;
  topP?: number;
  topK?: number;
  maxTokens?: number;
  timeoutMs?: number;
}

export interface AiGenerateRequest {
  systemPrompt: string;
  userPrompt: string;
  options?: AiGenerateOptions;
}

export interface AiUsage {
  estimatedInputTokens: number;
  estimatedOutputTokens: number;
}

export interface AiGenerateResult {
  text: string;
  providerId: ProviderId;
  model: string;
  usage: AiUsage;
  latencyMs: number;
}

/**
 * The single seam every AI provider integration implements — per
 * docs/12-project-architecture.md#ai-service-boundaries, "the GeminiProvider
 * adapter is the only component that imports the Gemini SDK/client." No
 * method here ever exposes a provider-specific request/response shape;
 * `AiService`, `ProviderRegistry`, and `ProviderRouter` only ever see this
 * interface, never a concrete provider class.
 */
export interface AiProvider {
  readonly id: ProviderId;
  readonly displayName: string;

  /**
   * Cheap, synchronous, config-based readiness check (e.g. "is an API key
   * present") — never calls the network. `ProviderRegistry.listAvailable`
   * and `ProviderRouter` both use this to skip a registered-but-unusable
   * provider instead of letting a `generate` call fail.
   */
  isAvailable(): boolean;

  generate(request: AiGenerateRequest): Promise<AiGenerateResult>;
}
