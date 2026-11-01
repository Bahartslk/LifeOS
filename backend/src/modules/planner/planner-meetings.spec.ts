import { BadRequestException, NotFoundException } from '@nestjs/common';
import { Task, TaskCategory, TaskPriority, TaskSource, TaskStatus, TaskType } from '@prisma/client';
import { UsersService } from '../users/users.service';
import { DEFAULT_MEETING_REMINDER_MINUTES, PlannerService } from './planner.service';
import { TaskListsRepository } from './repositories/task-lists.repository';
import { CreateTaskData, TasksRepository } from './repositories/tasks.repository';

const USER_ID = 'user-1';

function storedTask(overrides: Partial<Task> = {}): Task {
  return {
    id: 'task-1',
    userId: USER_ID,
    taskListId: null,
    title: 'Görev',
    description: null,
    dueDate: new Date('2026-11-02T00:00:00.000Z'),
    dueTime: null,
    priority: TaskPriority.MEDIUM,
    category: TaskCategory.WORK,
    status: TaskStatus.TODO,
    source: TaskSource.PLANNER,
    type: TaskType.TASK,
    durationMinutes: null,
    location: null,
    reminderMinutesBefore: null,
    createdAt: new Date('2026-11-01T10:00:00.000Z'),
    updatedAt: new Date('2026-11-01T10:00:00.000Z'),
    deletedAt: null,
    ...overrides,
  };
}

/** Meeting fields on create/update: defaults, the "needs a time" rules and clearing with null. */
describe('PlannerService meetings', () => {
  let repository: jest.Mocked<Pick<TasksRepository, 'create' | 'update' | 'findById'>>;
  let service: PlannerService;

  beforeEach(() => {
    repository = {
      create: jest.fn(async (data: CreateTaskData) =>
        storedTask({
          ...data,
          dueDate: data.dueDate,
          dueTime: data.dueTime,
        }),
      ),
      update: jest.fn<Promise<Task | null>, Parameters<TasksRepository['update']>>(async () =>
        storedTask(),
      ),
      findById: jest.fn<Promise<Task | null>, Parameters<TasksRepository['findById']>>(async () =>
        storedTask(),
      ),
    };
    service = new PlannerService(
      repository as unknown as TasksRepository,
      {} as TaskListsRepository,
      {} as UsersService,
    );
  });

  const base = {
    title: 'Sprint planlama',
    dueDate: '2026-11-02',
    priority: TaskPriority.HIGH,
    category: TaskCategory.WORK,
  };

  describe('createTask', () => {
    it('saves a meeting with its time, length and place, and a 10-minute reminder by default', async () => {
      const result = await service.createTask(USER_ID, {
        ...base,
        dueTime: '10:00',
        type: TaskType.MEETING,
        durationMinutes: 30,
        location: 'Ofis',
      });

      expect(repository.create).toHaveBeenCalledWith(
        expect.objectContaining({
          type: TaskType.MEETING,
          durationMinutes: 30,
          location: 'Ofis',
          reminderMinutesBefore: DEFAULT_MEETING_REMINDER_MINUTES,
        }),
      );
      expect(DEFAULT_MEETING_REMINDER_MINUTES).toBe(10);
      expect(result).toEqual(
        expect.objectContaining({
          type: 'MEETING',
          dueTime: '10:00',
          durationMinutes: 30,
          location: 'Ofis',
          reminderMinutesBefore: 10,
        }),
      );
    });

    it('lets a meeting opt out of the reminder with null', async () => {
      await service.createTask(USER_ID, {
        ...base,
        dueTime: '10:00',
        type: TaskType.MEETING,
        reminderMinutesBefore: null,
      });

      expect(repository.create).toHaveBeenCalledWith(
        expect.objectContaining({ reminderMinutesBefore: null }),
      );
    });

    it('keeps a plain task as before: type TASK, no reminder unless asked', async () => {
      await service.createTask(USER_ID, base);

      expect(repository.create).toHaveBeenCalledWith(
        expect.objectContaining({
          type: TaskType.TASK,
          durationMinutes: null,
          location: null,
          reminderMinutesBefore: null,
        }),
      );
    });

    it('accepts a reminder on a timed task', async () => {
      await service.createTask(USER_ID, { ...base, dueTime: '18:00', reminderMinutesBefore: 0 });

      expect(repository.create).toHaveBeenCalledWith(
        expect.objectContaining({ reminderMinutesBefore: 0 }),
      );
    });

    it('rejects a meeting without a time', async () => {
      await expect(
        service.createTask(USER_ID, { ...base, type: TaskType.MEETING }),
      ).rejects.toThrow(new BadRequestException('A meeting needs a dueTime.'));
      expect(repository.create).not.toHaveBeenCalled();
    });

    it('rejects a reminder on a task without a time', async () => {
      await expect(
        service.createTask(USER_ID, { ...base, reminderMinutesBefore: 15 }),
      ).rejects.toThrow(new BadRequestException('A reminder needs a dueTime.'));
    });
  });

  describe('updateTask', () => {
    it('turns a timed task into a meeting, checking against the stored time', async () => {
      repository.findById.mockResolvedValue(
        storedTask({ dueTime: new Date('1970-01-01T09:30:00.000Z') }),
      );

      await service.updateTask(USER_ID, 'task-1', { type: TaskType.MEETING });

      expect(repository.update).toHaveBeenCalledWith('task-1', USER_ID, { type: TaskType.MEETING });
    });

    it('rejects turning an all-day task into a meeting', async () => {
      await expect(
        service.updateTask(USER_ID, 'task-1', { type: TaskType.MEETING }),
      ).rejects.toThrow(BadRequestException);
      expect(repository.update).not.toHaveBeenCalled();
    });

    it('accepts the time and the meeting type in the same request', async () => {
      await service.updateTask(USER_ID, 'task-1', { type: TaskType.MEETING, dueTime: '14:00' });

      expect(repository.update).toHaveBeenCalled();
    });

    it('clears the meeting fields with explicit nulls', async () => {
      await service.updateTask(USER_ID, 'task-1', {
        durationMinutes: null,
        location: null,
        reminderMinutesBefore: null,
      });

      expect(repository.update).toHaveBeenCalledWith('task-1', USER_ID, {
        durationMinutes: null,
        location: null,
        reminderMinutesBefore: null,
      });
    });

    it('does not look the task up when no rule-relevant field changes', async () => {
      await service.updateTask(USER_ID, 'task-1', { location: 'Zoom' });

      expect(repository.findById).not.toHaveBeenCalled();
    });

    it('answers 404 for a task that is not the caller', async () => {
      repository.findById.mockResolvedValue(null);

      await expect(
        service.updateTask(USER_ID, 'task-1', { reminderMinutesBefore: 5 }),
      ).rejects.toThrow(NotFoundException);
    });
  });
});
