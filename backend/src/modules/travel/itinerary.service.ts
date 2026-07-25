import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { ItineraryItem, ItineraryItemType, TaskPriority, TaskSource } from '@prisma/client';
import { fromDbDate, toDbDate, toDbTime } from '../../common/utils/date-time.util';
import { PlannerService } from '../planner/planner.service';
import { TripsRepository } from './repositories/trips.repository';
import { ItineraryItemsRepository } from './repositories/itinerary-items.repository';
import { CreateItineraryItemDto } from './dto/create-itinerary-item.dto';
import { UpdateItineraryItemDto } from './dto/update-itinerary-item.dto';
import { ReorderItineraryDto } from './dto/reorder-itinerary.dto';
import { ItineraryItemResponseDto } from './dto/itinerary-item-response.dto';
import { toItineraryItemResponse } from './utils/itinerary-item-mapper.util';

/** No `TaskPriority` concept exists on `ItineraryItem` — used only when `createTask: true` creates a linked Planner task, since `Task.priority` is required with no default. A neutral, defensible default; the caller can change it afterward like any other Planner task, via `PlannerModule`'s own `PATCH /planner/tasks/:id`. */
const INTEGRATION_TASK_PRIORITY = TaskPriority.MEDIUM;

/**
 * Itinerary item CRUD + reorder + the Planner integration, per Sprint
 * 16.0's "When an itinerary item requires a task: create/update Planner
 * task, source = TRAVEL. Travel never duplicates Planner task data." Split
 * from `TravelService` to avoid one service covering both trips and
 * itinerary items (see that class's doc comment).
 *
 * Integration design: `ItineraryItem.taskId` (nullable, unique) records
 * which Planner `Task`, if any, this item spawned — set only when a
 * `createTask: true` request is honored. This is deliberately looser than
 * mobile's actual pattern for *displaying* Travel-sourced tasks (Travel
 * Detail's `TravelTasksSection` just filters Planner tasks by `source =
 * TRAVEL`, no per-item link needed for that) — the link exists here purely
 * so a later update to the SAME itinerary item can propagate to the SAME
 * task, which "create/update Planner task" requires and a filter alone
 * cannot provide. Every create/update/delete of a linked task goes through
 * `PlannerService` (`createIntegrationTask`, the existing `updateTask`,
 * the existing `deleteTask`) — this class never touches `PrismaService`'s
 * `task` model directly, satisfying "Travel never duplicates Planner task
 * logic".
 */
@Injectable()
export class ItineraryService {
  constructor(
    private readonly itineraryItemsRepository: ItineraryItemsRepository,
    private readonly tripsRepository: TripsRepository,
    private readonly plannerService: PlannerService,
  ) {}

  async getItinerary(userId: string, tripId: string): Promise<ItineraryItemResponseDto[]> {
    await this.assertTripOwnership(userId, tripId);
    const items = await this.itineraryItemsRepository.findByTripId(tripId, userId);
    return items.map((item) => toItineraryItemResponse(item));
  }

  async createItem(
    userId: string,
    tripId: string,
    dto: CreateItineraryItemDto,
  ): Promise<ItineraryItemResponseDto> {
    await this.assertTripOwnership(userId, tripId);
    this.assertSubtypeConsistency(dto.type, dto.transportationType, dto.accommodationType);

    const maxOrderIndex = await this.itineraryItemsRepository.findMaxOrderIndex(tripId);
    const nextOrderIndex = maxOrderIndex === null ? 0 : maxOrderIndex + 1;

    let taskId: string | null = null;
    if (dto.createTask) {
      const task = await this.plannerService.createIntegrationTask(userId, {
        title: dto.title,
        description: dto.description ?? null,
        dueDate: dto.date,
        dueTime: dto.startTime ?? null,
        priority: INTEGRATION_TASK_PRIORITY,
        source: TaskSource.TRAVEL,
      });
      taskId = task.id;
    }

    const item = await this.itineraryItemsRepository.create({
      tripId,
      title: dto.title,
      description: dto.description ?? null,
      date: toDbDate(dto.date),
      startTime: dto.startTime ? toDbTime(dto.startTime) : null,
      endTime: dto.endTime ? toDbTime(dto.endTime) : null,
      location: dto.location ?? null,
      orderIndex: nextOrderIndex,
      type: dto.type,
      transportationType: dto.transportationType ?? null,
      accommodationType: dto.accommodationType ?? null,
      taskId,
    });

    return toItineraryItemResponse(item);
  }

  /**
   * If the item already has a linked task, any changed title/date/startTime
   * is propagated to it via `PlannerService.updateTask` — the "update
   * Planner task" half of this sprint's integration requirement. If it has
   * no linked task yet and `dto.createTask: true` is sent, one is created
   * now (same as at creation time). `dto.createTask: false`/omitted on an
   * item that already has a link does NOT unlink or delete the task —
   * only `deleteItem` removes a task, never a plain update.
   */
  async updateItem(
    userId: string,
    id: string,
    dto: UpdateItineraryItemDto,
  ): Promise<ItineraryItemResponseDto> {
    const existing = await this.itineraryItemsRepository.findById(id, userId);
    if (!existing) {
      throw new NotFoundException('Itinerary item not found.');
    }

    const nextType = dto.type ?? existing.type;
    this.assertSubtypeConsistency(nextType, dto.transportationType, dto.accommodationType);

    let taskId = existing.taskId;
    if (existing.taskId) {
      await this.syncLinkedTask(userId, existing.taskId, dto);
    } else if (dto.createTask) {
      const task = await this.plannerService.createIntegrationTask(userId, {
        title: dto.title ?? existing.title,
        description: dto.description ?? existing.description,
        dueDate: dto.date ?? fromDbDate(existing.date),
        dueTime: dto.startTime,
        priority: INTEGRATION_TASK_PRIORITY,
        source: TaskSource.TRAVEL,
      });
      taskId = task.id;
    }

    const item = await this.itineraryItemsRepository.update(id, userId, {
      ...(dto.title !== undefined && { title: dto.title }),
      ...(dto.description !== undefined && { description: dto.description }),
      ...(dto.date !== undefined && { date: toDbDate(dto.date) }),
      ...(dto.startTime !== undefined && { startTime: toDbTime(dto.startTime) }),
      ...(dto.endTime !== undefined && { endTime: toDbTime(dto.endTime) }),
      ...(dto.location !== undefined && { location: dto.location }),
      ...(dto.type !== undefined && { type: dto.type }),
      ...(dto.transportationType !== undefined && { transportationType: dto.transportationType }),
      ...(dto.accommodationType !== undefined && { accommodationType: dto.accommodationType }),
      ...(taskId !== existing.taskId && { taskId }),
    });

    if (!item) {
      throw new NotFoundException('Itinerary item not found.');
    }
    return toItineraryItemResponse(item);
  }

  /** Deletes the item; if it has a linked task, that task is soft-deleted too via `PlannerService.deleteTask` — Travel cleans up after itself rather than leaving an orphaned task once its originating item is gone. */
  async deleteItem(userId: string, id: string): Promise<void> {
    const deleted = await this.itineraryItemsRepository.delete(id, userId);
    if (!deleted) {
      throw new NotFoundException('Itinerary item not found.');
    }
    if (deleted.taskId) {
      await this.plannerService.deleteTask(userId, deleted.taskId);
    }
  }

  /** Requires `dto.itemIds` to be exactly the trip's current item ids (see `ReorderItineraryDto`'s doc comment) — anything missing or foreign is a 400, not a partial/best-effort reorder. */
  async reorder(
    userId: string,
    tripId: string,
    dto: ReorderItineraryDto,
  ): Promise<ItineraryItemResponseDto[]> {
    await this.assertTripOwnership(userId, tripId);

    const existingItems = await this.itineraryItemsRepository.findByTripId(tripId, userId);
    const existingIds = new Set(existingItems.map((item) => item.id));
    const providedIds = new Set(dto.itemIds);

    const missing = existingItems.filter((item) => !providedIds.has(item.id));
    const foreign = dto.itemIds.filter((id) => !existingIds.has(id));
    if (missing.length > 0 || foreign.length > 0) {
      throw new BadRequestException(
        "itemIds must contain exactly the trip's current itinerary item ids — no missing or foreign ids.",
      );
    }

    await this.itineraryItemsRepository.reorder(
      dto.itemIds.map((id, index) => ({ id, orderIndex: index })),
    );
    const reordered = await this.itineraryItemsRepository.findByTripId(tripId, userId);
    return reordered.map((item) => toItineraryItemResponse(item));
  }

  private async syncLinkedTask(
    userId: string,
    taskId: string,
    dto: UpdateItineraryItemDto,
  ): Promise<void> {
    if (
      dto.title === undefined &&
      dto.description === undefined &&
      dto.date === undefined &&
      dto.startTime === undefined
    ) {
      return;
    }
    await this.plannerService.updateTask(userId, taskId, {
      ...(dto.title !== undefined && { title: dto.title }),
      ...(dto.description !== undefined && { description: dto.description }),
      ...(dto.date !== undefined && { dueDate: dto.date }),
      ...(dto.startTime !== undefined && { dueTime: dto.startTime }),
    });
  }

  private async assertTripOwnership(userId: string, tripId: string): Promise<void> {
    const trip = await this.tripsRepository.findById(tripId, userId);
    if (!trip) {
      throw new NotFoundException('Trip not found.');
    }
  }

  private assertSubtypeConsistency(
    type: ItineraryItemType,
    transportationType: ItineraryItem['transportationType'] | undefined,
    accommodationType: ItineraryItem['accommodationType'] | undefined,
  ): void {
    if (transportationType && type !== ItineraryItemType.TRANSPORTATION) {
      throw new BadRequestException(
        'transportationType is only valid when type is TRANSPORTATION.',
      );
    }
    if (accommodationType && type !== ItineraryItemType.ACCOMMODATION) {
      throw new BadRequestException('accommodationType is only valid when type is ACCOMMODATION.');
    }
  }
}
