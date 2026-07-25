import { Injectable } from '@nestjs/common';
import { AiService } from '../service/ai.service';
import { parseAndValidateStructuredResponse } from './response-validation.util';
import { AiCapabilityDefinition } from './capability.types';

export interface AiCapabilityUsage {
  inputTokens: number;
  outputTokens: number;
  totalTokens: number;
}

/** The exact shape Sprint 18B's Structured Responses objective specifies: `{provider, model, result, usage, latencyMs}` — never a raw provider response. */
export interface AiCapabilityResult<TResult> {
  provider: string;
  model: string;
  result: TResult;
  usage: AiCapabilityUsage;
  latencyMs: number;
}

/**
 * The single generic executor every capability endpoint shares — reuses
 * `AiService.generate` (and, through it, `ContextBuilder`/`PromptBuilder`/
 * the provider chain) exactly as Sprint 18A built it. A capability is data
 * (`AiCapabilityDefinition`), not a subclass — adding a 5th capability
 * means adding one more definition object and one more controller route,
 * not another service.
 *
 * Every behavioral knob (which provider to prefer, whether fallback is
 * allowed, temperature, token budget, which context sections to load) is
 * read from the definition, never hardcoded here — Sprint 18B.5's
 * "behavior should be driven by capability metadata" rule. `TResult` is
 * inferred from `definition` itself (`AiCapabilityDefinition<TResult>`),
 * so a caller can no longer pass a generic type param that doesn't match
 * the definition's actual `outputSchema` — a real compile-time safety
 * improvement over Sprint 18B's version, where the two were unrelated.
 *
 * `primaryInput` (Sprint 19) is an optional 4th argument, purely additive
 * — every Sprint 18B call site still passes exactly 3 arguments and is
 * unaffected. Sprint 18B's four capabilities only ever needed the caller's
 * existing Planner/Travel/Users context plus an optional free-text `notes`
 * steer; Sprint 19's Planner capabilities (task drafting from natural
 * language, extracting a task's own subtasks, ...) need to operate on
 * specific per-call content that isn't part of any user's stored context
 * at all — `notes` was never meant to carry a capability's primary
 * payload, so a distinct, clearly-labeled parameter was added instead of
 * overloading `notes`'s existing meaning.
 */
@Injectable()
export class AiCapabilityService {
  constructor(private readonly aiService: AiService) {}

  async run<TResult>(
    definition: AiCapabilityDefinition<TResult>,
    userId: string,
    notes?: string,
    primaryInput?: string,
  ): Promise<AiCapabilityResult<TResult>> {
    const instruction = this.buildInstruction(definition, notes, primaryInput);

    const generated = await this.aiService.generate({
      userId,
      templateId: definition.templateId,
      feature: definition.feature,
      instruction,
      contextOptions: definition.contextScope,
      capabilityId: definition.id,
      preferredProviderId: definition.preferredProvider,
      fallbackAllowed: definition.fallbackAllowed,
      generateOptions: {
        temperature: definition.temperature,
        maxTokens: definition.maxTokens,
      },
    });

    const result = parseAndValidateStructuredResponse(
      generated.text,
      definition.outputSchema,
      generated.providerId,
    );

    return {
      provider: generated.providerId,
      model: generated.model,
      result,
      usage: {
        inputTokens: generated.usage.estimatedInputTokens,
        outputTokens: generated.usage.estimatedOutputTokens,
        totalTokens: generated.usage.estimatedInputTokens + generated.usage.estimatedOutputTokens,
      },
      latencyMs: generated.latencyMs,
    };
  }

  private buildInstruction<TResult>(
    definition: AiCapabilityDefinition<TResult>,
    notes: string | undefined,
    primaryInput: string | undefined,
  ): string {
    const parts = [
      definition.basePrompt,
      primaryInput ? `Input:\n${primaryInput}` : undefined,
      notes ? `Additional focus from the caller: ${notes}` : undefined,
      `Respond with ONLY a JSON object matching exactly this shape, no markdown, no code fences, no extra text: ${definition.jsonShapeInstruction}`,
    ];
    return parts.filter((part): part is string => Boolean(part)).join('\n\n');
  }
}
