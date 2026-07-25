import { Injectable, Logger } from '@nestjs/common';
import { ContextBuilder } from '../context/context-builder.service';
import { AiGenerationRequestDto } from '../dto/ai-generation-request.dto';
import { AiGenerationResultDto } from '../dto/ai-generation-result.dto';
import { PromptBuilder } from '../prompt/prompt-builder.service';
import { ProviderRouter } from '../router/provider-router.service';
import { AiContext } from '../types/ai-context.types';
import { ProviderId } from '../types/ai.types';
import { AiFallbackExecutor } from './ai-fallback.executor';

function buildEmptyContext(): AiContext {
  return {
    user: { displayName: '' },
    preferences: { language: 'tr', timezone: 'Europe/Istanbul', themePreference: 'SYSTEM' },
    planner: null,
    travel: null,
    metadata: { userId: '', generatedAt: new Date().toISOString() },
    conversation: [],
  };
}

/**
 * The AI module's sole export and the only thing a caller (today, only
 * `AiCapabilityService`) ever injects. It never instantiates a provider
 * directly — `ProviderRouter` resolves a candidate chain from
 * `ProviderRegistry`, and `AiFallbackExecutor` walks it — so this class has
 * no idea whether Gemini, OpenRouter, or anything else ultimately handled
 * the request, per this platform's central Goal.
 *
 * Orchestration order: resolve a provider chain -> build context (if
 * `userId` given) -> build the prompt -> call the chain through the
 * fallback executor (which itself retries each candidate via
 * `AiRetryExecutor` before advancing) -> log -> return. Business logic
 * about what to DO with the returned text belongs to the calling feature's
 * own use case, never here (Design Principle: "AI never owns business
 * entities"). `request.capabilityId` is passed through purely for
 * attribution in the log line below — this class stays capability-agnostic
 * otherwise; it never branches on which capability is calling.
 */
@Injectable()
export class AiService {
  private readonly logger = new Logger(AiService.name);

  constructor(
    private readonly router: ProviderRouter,
    private readonly promptBuilder: PromptBuilder,
    private readonly contextBuilder: ContextBuilder,
    private readonly fallbackExecutor: AiFallbackExecutor,
  ) {}

  async generate(request: AiGenerationRequestDto): Promise<AiGenerationResultDto> {
    const chain = this.router.resolveChain({
      feature: request.feature,
      preferredProviderId: request.preferredProviderId,
    });

    const context = request.userId
      ? await this.contextBuilder.build(request.userId, request.contextOptions)
      : buildEmptyContext();

    const prompt = this.promptBuilder.build({
      templateId: request.templateId,
      instruction: request.instruction,
      context,
      variables: request.variables,
    });

    const { result, retryCount, fallbackCount, attemptedProviderIds } =
      await this.fallbackExecutor.execute(
        chain,
        (provider) =>
          provider.generate({
            systemPrompt: prompt.systemPrompt,
            userPrompt: prompt.userPrompt,
            options: request.generateOptions,
          }),
        { fallbackAllowed: request.fallbackAllowed },
      );

    this.logCall(
      request.capabilityId,
      result.model,
      result.latencyMs,
      retryCount,
      fallbackCount,
      attemptedProviderIds,
      result.usage,
    );

    return {
      text: result.text,
      providerId: result.providerId,
      model: result.model,
      usage: result.usage,
      latencyMs: result.latencyMs,
      retryCount,
      fallbackCount,
      templateId: prompt.templateId,
      templateVersion: prompt.templateVersion,
    };
  }

  /**
   * Structured, parseable log line per this module's Logging rule —
   * capability (when the caller supplied one), selected provider (the
   * last entry of `attemptedProviderIds`), any earlier providers that
   * failed first, model, latency, retry count, fallback count, and token
   * estimates only. Never logs `systemPrompt`/`userPrompt`/API keys/user
   * context (see this module's Security notes) — nothing here ever holds
   * a reference to either prompt string.
   */
  private logCall(
    capabilityId: string | undefined,
    model: string,
    latencyMs: number,
    retryCount: number,
    fallbackCount: number,
    attemptedProviderIds: ProviderId[],
    usage: { estimatedInputTokens: number; estimatedOutputTokens: number },
  ): void {
    const providerId = attemptedProviderIds[attemptedProviderIds.length - 1];
    const failedProviderIds = attemptedProviderIds.slice(0, -1);
    this.logger.log(
      JSON.stringify({
        capabilityId,
        providerId,
        failedProviderIds,
        model,
        latencyMs,
        retryCount,
        fallbackCount,
        ...usage,
      }),
    );
  }
}
