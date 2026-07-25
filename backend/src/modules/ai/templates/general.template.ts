import { PromptTemplate } from '../interfaces/prompt-template.interface';
import { BASE_SYSTEM_TEMPLATE } from './base-system.template';

/** Base system prompt with no feature-specific context injected — for a request that isn't Planner/Travel/Assistant-scoped. */
export const GENERAL_TEMPLATE: PromptTemplate = {
  id: 'general',
  version: 'v1',
  renderSystem(context, variables) {
    return [
      BASE_SYSTEM_TEMPLATE.renderSystem(context, variables),
      'No specific feature context was requested for this call — answer generally.',
    ].join('\n\n');
  },
};
