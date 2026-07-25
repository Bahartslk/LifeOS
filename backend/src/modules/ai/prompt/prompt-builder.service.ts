import { Injectable } from '@nestjs/common';
import { AiConfigurationException } from '../exceptions/ai.exceptions';
import { PROMPT_TEMPLATES } from '../templates';
import { AiContext } from '../types/ai-context.types';
import { AiTemplateId } from '../types/ai.types';

export interface PromptBuildRequest {
  templateId: AiTemplateId;
  /** The caller's actual ask, e.g. "Summarize today's tasks in one sentence." Passed through as the user turn — this class never rewrites or augments it. */
  instruction: string;
  context: AiContext;
  variables?: Record<string, string>;
}

export interface BuiltPrompt {
  systemPrompt: string;
  userPrompt: string;
  templateId: AiTemplateId;
  templateVersion: string;
}

/**
 * The only place a system/user prompt pair is assembled — per CLAUDE.md's
 * "prompt templates must be reusable and centrally maintained, not
 * duplicated inline across call sites," nothing outside this class and
 * `templates/` ever concatenates prompt strings. Context injection happens
 * at the system-prompt layer (each template's `renderSystem` consumes
 * `AiContext` directly) rather than by mutating the user's instruction, so
 * the same instruction always produces the same user turn regardless of
 * which template/context it's paired with.
 */
@Injectable()
export class PromptBuilder {
  build(request: PromptBuildRequest): BuiltPrompt {
    const template = PROMPT_TEMPLATES[request.templateId];
    if (!template) {
      throw new AiConfigurationException(`Unknown prompt template id "${request.templateId}".`);
    }

    return {
      systemPrompt: template.renderSystem(request.context, request.variables),
      userPrompt: request.instruction,
      templateId: template.id,
      templateVersion: template.version,
    };
  }
}
