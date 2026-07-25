import { PromptTemplate } from '../interfaces/prompt-template.interface';
import { BASE_SYSTEM_TEMPLATE } from './base-system.template';
import { formatPlannerContext, formatTravelContext } from './format-context.util';

/** Base system prompt plus both Planner and Travel context — for a general assistant that may need to reason across both, e.g. "what does my week look like". */
export const ASSISTANT_TEMPLATE: PromptTemplate = {
  id: 'assistant',
  version: 'v1',
  renderSystem(context, variables) {
    const plannerBlock = context.planner
      ? formatPlannerContext(context.planner)
      : '(no Planner data available)';
    const travelBlock = context.travel
      ? formatTravelContext(context.travel)
      : '(no Travel data available)';
    return [
      BASE_SYSTEM_TEMPLATE.renderSystem(context, variables),
      'You are the general LifeOS assistant, with access to both Planner and Travel context:',
      `Planner:\n${plannerBlock}`,
      `Travel:\n${travelBlock}`,
    ].join('\n\n');
  },
};
