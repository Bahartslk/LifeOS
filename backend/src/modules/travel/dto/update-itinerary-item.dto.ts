import { PartialType } from '@nestjs/swagger';
import { CreateItineraryItemDto } from './create-itinerary-item.dto';

/**
 * Every `CreateItineraryItemDto` field, optional (PATCH semantics) — via
 * `PartialType`. `createTask` is inherited too: if the item has no linked
 * task yet and `createTask: true` is sent, `ItineraryService.updateItem`
 * creates one; if a linked task already exists, any changed
 * title/date/startTime fields are propagated to it automatically
 * regardless of `createTask` (see that method's doc comment) — Sprint
 * 16.0's "create/update Planner task" requirement.
 */
export class UpdateItineraryItemDto extends PartialType(CreateItineraryItemDto) {}
