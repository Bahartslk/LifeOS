import { ZodType } from 'zod';
import { InvalidProviderResponseException } from '../exceptions/ai.exceptions';
import { ProviderId } from '../types/ai.types';

/**
 * The full "malformed AI responses must never reach API consumers" flow:
 * raw text -> parsed JSON -> schema-validated, typed result. Both failure
 * modes (not JSON at all; valid JSON that doesn't match the capability's
 * `outputSchema`) throw the same `InvalidProviderResponseException` a
 * caller already knows how to handle, with a specific, non-leaky reason —
 * never the raw provider text (see this module's Security notes).
 */
export function parseAndValidateStructuredResponse<T>(
  text: string,
  schema: ZodType<T>,
  providerId: ProviderId,
): T {
  const json = parseJson(text, providerId);
  const validated = schema.safeParse(json);

  if (!validated.success) {
    const issues = validated.error.issues
      .map((issue) => `${issue.path.join('.') || '(root)'}: ${issue.message}`)
      .join('; ');
    throw new InvalidProviderResponseException(
      providerId,
      `Response did not match the expected shape (${issues}).`,
    );
  }

  return validated.data;
}

function parseJson(text: string, providerId: ProviderId): unknown {
  try {
    return JSON.parse(stripCodeFence(text.trim()));
  } catch {
    throw new InvalidProviderResponseException(providerId, 'Response was not valid JSON.');
  }
}

/**
 * Models sometimes wrap JSON in a ` ```json ... ``` ` fence despite being
 * told not to (see `AiCapabilityService.buildInstruction`) — stripped
 * defensively here rather than by tightening the prompt further, since a
 * prompt can never *guarantee* a model's raw output format.
 */
function stripCodeFence(text: string): string {
  const fenceMatch = /^```(?:json)?\s*([\s\S]*?)\s*```$/i.exec(text);
  return fenceMatch ? fenceMatch[1] : text;
}
