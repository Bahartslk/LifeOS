import { AiContext } from '../types/ai-context.types';
import { AiTemplateId } from '../types/ai.types';

/**
 * A single versioned, named prompt artifact — per CLAUDE.md's "prompt
 * templates must be reusable and centrally maintained, not duplicated
 * inline across call sites." `version` allows a template to be improved
 * later without affecting anything that already recorded which version it
 * used (no such caller exists yet, since there's no conversation
 * persistence this sprint, but `PromptBuilder.build`'s result already
 * carries `templateVersion` for when one does).
 */
export interface PromptTemplate {
  readonly id: AiTemplateId;
  readonly version: string;
  renderSystem(context: AiContext, variables?: Record<string, string>): string;
}
