import { PromptTemplate } from '../interfaces/prompt-template.interface';
import { BASE_SYSTEM_TEMPLATE } from './base-system.template';
import { formatPlannerContext } from './format-context.util';

/** Base system prompt plus the caller's Planner (task) context, per this sprint's "Planner owns planner data; AI consumes services" principle — this template never fabricates task data beyond what `ContextBuilder` supplied. `v2` (Planner Step 3): the Planner block now also lists overdue tasks. */
export const PLANNER_TEMPLATE: PromptTemplate = {
  id: 'planner',
  version: 'v2',
  renderSystem(context, variables) {
    const plannerBlock = context.planner
      ? formatPlannerContext(context.planner)
      : '(no Planner data available)';
    return [
      BASE_SYSTEM_TEMPLATE.renderSystem(context, variables),
      'You are helping with Planner (task management). Base your answer only on this task data:',
      plannerBlock,
    ].join('\n\n');
  },
};
