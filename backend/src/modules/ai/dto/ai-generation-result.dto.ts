import { AiUsage } from '../interfaces/ai-provider.interface';

/** `AiService.generate()`'s output contract — a caller sees a provider id/model as plain metadata, never a provider-specific response object. */
export interface AiGenerationResultDto {
  text: string;
  providerId: string;
  model: string;
  usage: AiUsage;
  latencyMs: number;
  retryCount: number;
  /** How many earlier providers in the resolved chain were tried and failed before `providerId` succeeded — `0` means the first candidate succeeded, per `AiFallbackExecutor` (Sprint 18B). */
  fallbackCount: number;
  templateId: string;
  templateVersion: string;
}
