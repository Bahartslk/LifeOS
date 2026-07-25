import { Injectable } from '@nestjs/common';
import { TaskList } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

/**
 * Only what `PlannerService` needs today: confirming a client-supplied
 * `taskListId` actually belongs to the requesting user before a `Task` is
 * linked to it — no `TaskList` controller/service/CRUD exists yet (see
 * `TaskList`'s doc comment in `prisma/schema.prisma`), so this is the
 * entire surface for now.
 */
@Injectable()
export class TaskListsRepository {
  constructor(private readonly prisma: PrismaService) {}

  findById(id: string, userId: string): Promise<TaskList | null> {
    return this.prisma.taskList.findFirst({ where: { id, userId, deletedAt: null } });
  }
}
