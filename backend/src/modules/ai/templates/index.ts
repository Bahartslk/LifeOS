import { PromptTemplate } from '../interfaces/prompt-template.interface';
import { AiTemplateId } from '../types/ai.types';
import { ASSISTANT_TEMPLATE } from './assistant.template';
import { BASE_SYSTEM_TEMPLATE } from './base-system.template';
import { GENERAL_TEMPLATE } from './general.template';
import { PLANNER_TEMPLATE } from './planner.template';
import { TRAVEL_TEMPLATE } from './travel.template';

/** Every centrally-maintained, versioned prompt template — `PromptBuilder` is the only consumer, per CLAUDE.md's "prompt templates must be reusable and centrally maintained, not duplicated inline across call sites." */
export const PROMPT_TEMPLATES: Record<AiTemplateId, PromptTemplate> = {
  'base-system': BASE_SYSTEM_TEMPLATE,
  general: GENERAL_TEMPLATE,
  planner: PLANNER_TEMPLATE,
  travel: TRAVEL_TEMPLATE,
  assistant: ASSISTANT_TEMPLATE,
};
