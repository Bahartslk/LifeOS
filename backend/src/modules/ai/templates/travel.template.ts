import { PromptTemplate } from '../interfaces/prompt-template.interface';
import { BASE_SYSTEM_TEMPLATE } from './base-system.template';
import { formatTravelContext } from './format-context.util';

/** Base system prompt plus the caller's Travel (trip/itinerary) context, per this sprint's "Travel owns travel data; AI consumes services" principle. */
export const TRAVEL_TEMPLATE: PromptTemplate = {
  id: 'travel',
  version: 'v1',
  renderSystem(context, variables) {
    const travelBlock = context.travel
      ? formatTravelContext(context.travel)
      : '(no Travel data available)';
    return [
      BASE_SYSTEM_TEMPLATE.renderSystem(context, variables),
      'You are helping with Travel (trips and itineraries). Base your answer only on this trip data:',
      travelBlock,
    ].join('\n\n');
  },
};
