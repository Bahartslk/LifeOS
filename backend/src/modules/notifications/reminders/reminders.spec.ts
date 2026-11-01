import { Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { NotificationKind, Prisma, TaskType } from '@prisma/client';
import { AppConfig } from '../../../config/configuration';
import { PrismaService } from '../../../prisma/prisma.service';
import { PlannerService, ScheduledTaskInfo } from '../../planner/planner.service';
import { NotificationTarget, UsersService } from '../../users/users.service';
import { NotificationsService } from '../notifications.service';
import { DeviceTokensRepository } from '../repositories/device-tokens.repository';
import { NotificationDeliveriesRepository } from '../repositories/notification-deliveries.repository';
import { buildDailySummary, buildTaskReminder } from './reminder-messages';
import { ReminderScheduler } from './reminder.scheduler';
import { ReminderService } from './reminder.service';

function task(overrides: Partial<ScheduledTaskInfo> = {}): ScheduledTaskInfo {
  return {
    id: 'task-1',
    userId: 'user-1',
    title: 'Sprint planlama',
    dueDate: '2026-11-02',
    dueTime: '10:00',
    type: TaskType.MEETING,
    durationMinutes: 60,
    location: 'Ofis',
    reminderMinutesBefore: 10,
    ...overrides,
  };
}

describe('reminder messages', () => {
  it('announces a meeting with its lead, time range and place', () => {
    expect(buildTaskReminder(task())).toEqual({
      title: 'Toplantı 10 dakika sonra: Sprint planlama',
      body: '10:00–11:00 · Ofis',
      channelId: 'reminders',
      data: { type: 'TASK_REMINDER', taskId: 'task-1' },
    });
  });

  it('says a meeting is starting when the reminder is at the time', () => {
    expect(buildTaskReminder(task({ reminderMinutesBefore: 0, location: null })).title).toBe(
      'Toplantı başlıyor: Sprint planlama',
    );
  });

  it('words a plain task reminder by its lead or as due now', () => {
    const plain = { type: TaskType.TASK, durationMinutes: null, location: null };
    expect(buildTaskReminder(task({ ...plain, reminderMinutesBefore: 60 }))).toEqual(
      expect.objectContaining({
        title: 'Hatırlatma: Sprint planlama',
        body: '1 saat sonra · 10:00',
      }),
    );
    expect(buildTaskReminder(task({ ...plain, reminderMinutesBefore: 0 }))).toEqual(
      expect.objectContaining({ title: 'Zamanı geldi: Sprint planlama', body: '10:00' }),
    );
  });

  it("builds the morning summary from today's tasks, without AI", () => {
    const message = buildDailySummary(
      [task(), task({ id: 'task-2', title: 'Rapor', type: TaskType.TASK, dueTime: null })],
      '2026-11-02',
    );

    expect(message).toEqual({
      title: 'Günaydın! Bugün 2 görevin var',
      body: '1 toplantı dahil. İlki 10:00: Sprint planlama. Günün özeti seni bekliyor.',
      channelId: 'daily_summary',
      data: { type: 'DAILY_SUMMARY', date: '2026-11-02' },
    });
  });
});

describe('ReminderService', () => {
  const target: NotificationTarget = {
    userId: 'user-1',
    timezone: 'Europe/Istanbul',
    taskReminders: true,
    dailySummary: true,
  };
  let deviceTokens: { findUserIdsWithDevices: jest.Mock };
  let claimed: Set<string>;
  let deliveries: { claim: jest.Mock };
  let users: { findNotificationTargets: jest.Mock };
  let planner: { findReminderCandidates: jest.Mock; findUnfinishedDueOn: jest.Mock };
  let notifications: { sendToUser: jest.Mock };
  let service: ReminderService;

  beforeEach(() => {
    deviceTokens = { findUserIdsWithDevices: jest.fn().mockResolvedValue(['user-1']) };
    claimed = new Set();
    deliveries = {
      claim: jest.fn(async ({ dedupeKey }: { dedupeKey: string }) => {
        if (claimed.has(dedupeKey)) return false;
        claimed.add(dedupeKey);
        return true;
      }),
    };
    users = { findNotificationTargets: jest.fn().mockResolvedValue([target]) };
    planner = {
      findReminderCandidates: jest.fn().mockResolvedValue([task()]),
      findUnfinishedDueOn: jest.fn().mockResolvedValue([]),
    };
    notifications = { sendToUser: jest.fn().mockResolvedValue({ sent: 1 }) };
    service = new ReminderService(
      deviceTokens as unknown as DeviceTokensRepository,
      deliveries as unknown as NotificationDeliveriesRepository,
      users as unknown as UsersService,
      planner as unknown as PlannerService,
      notifications as unknown as NotificationsService,
    );
    jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined);
  });

  afterEach(() => jest.restoreAllMocks());

  // The meeting is 10:00 in Istanbul (07:00Z); its 10-minute reminder fires at 06:50Z.
  const at = (iso: string) => new Date(iso);

  it("sends a meeting reminder at its time in the user's time zone, exactly once", async () => {
    await service.runOnce(at('2026-11-02T06:50:05Z'));
    await service.runOnce(at('2026-11-02T06:51:05Z'));

    expect(notifications.sendToUser).toHaveBeenCalledTimes(1);
    expect(notifications.sendToUser).toHaveBeenCalledWith('user-1', buildTaskReminder(task()));
    expect(deliveries.claim).toHaveBeenCalledWith({
      userId: 'user-1',
      kind: NotificationKind.TASK_REMINDER,
      dedupeKey: 'TASK_REMINDER:task-1:2026-11-02T06:50:00.000Z',
      scheduledFor: at('2026-11-02T06:50:00Z'),
    });
  });

  it('does not send before the time, and skips one that is more than 10 minutes late', async () => {
    await service.runOnce(at('2026-11-02T06:49:59Z'));
    await service.runOnce(at('2026-11-02T07:00:01Z'));

    expect(notifications.sendToUser).not.toHaveBeenCalled();
  });

  it('still sends a few minutes late, after a restart', async () => {
    await service.runOnce(at('2026-11-02T06:57:00Z'));

    expect(notifications.sendToUser).toHaveBeenCalledTimes(1);
  });

  it('sends again when the task moves to a new time', async () => {
    await service.runOnce(at('2026-11-02T06:50:05Z'));
    planner.findReminderCandidates.mockResolvedValue([task({ dueTime: '11:00' })]);
    await service.runOnce(at('2026-11-02T07:50:05Z'));

    expect(notifications.sendToUser).toHaveBeenCalledTimes(2);
  });

  it('asks Planner only for users with reminders on, over a window wide enough for every time zone', async () => {
    users.findNotificationTargets.mockResolvedValue([
      target,
      { ...target, userId: 'user-2', taskReminders: false },
    ]);
    deviceTokens.findUserIdsWithDevices.mockResolvedValue(['user-1', 'user-2']);

    await service.runOnce(at('2026-11-02T06:50:05Z'));

    expect(planner.findReminderCandidates).toHaveBeenCalledWith(
      ['user-1'],
      '2026-10-31',
      '2026-11-05',
    );
  });

  it('does nothing at all when nobody has a device', async () => {
    deviceTokens.findUserIdsWithDevices.mockResolvedValue([]);

    expect(await service.runOnce(at('2026-11-02T06:50:05Z'))).toEqual({
      reminders: 0,
      summaries: 0,
    });
    expect(users.findNotificationTargets).not.toHaveBeenCalled();
  });

  describe('morning summary', () => {
    beforeEach(() => planner.findReminderCandidates.mockResolvedValue([]));

    it("goes out at 08:00 in the user's time zone with today's tasks, once", async () => {
      planner.findUnfinishedDueOn.mockResolvedValue([task()]);

      await service.runOnce(at('2026-11-02T05:00:05Z'));
      await service.runOnce(at('2026-11-02T05:01:05Z'));

      expect(planner.findUnfinishedDueOn).toHaveBeenCalledWith('user-1', '2026-11-02');
      expect(notifications.sendToUser).toHaveBeenCalledTimes(1);
      expect(notifications.sendToUser).toHaveBeenCalledWith(
        'user-1',
        buildDailySummary([task()], '2026-11-02'),
      );
    });

    it('is not sent on a day without tasks, nor outside 08:00', async () => {
      await service.runOnce(at('2026-11-02T05:00:05Z'));
      planner.findUnfinishedDueOn.mockResolvedValue([task()]);
      await service.runOnce(at('2026-11-02T06:00:05Z'));

      expect(notifications.sendToUser).not.toHaveBeenCalled();
    });

    it('respects the dailySummary preference', async () => {
      users.findNotificationTargets.mockResolvedValue([{ ...target, dailySummary: false }]);
      planner.findUnfinishedDueOn.mockResolvedValue([task()]);

      await service.runOnce(at('2026-11-02T05:00:05Z'));

      expect(notifications.sendToUser).not.toHaveBeenCalled();
    });
  });
});

describe('ReminderScheduler', () => {
  const config = (schedulerEnabled: boolean) =>
    ({
      get: () => ({ schedulerEnabled }),
    }) as unknown as ConfigService<AppConfig, true>;

  beforeEach(() => {
    jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined);
    jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => {
    jest.restoreAllMocks();
    jest.useRealTimers();
  });

  it('ticks a few seconds into every minute once started', async () => {
    jest.useFakeTimers({ now: new Date('2026-11-02T06:49:30Z') });
    const runOnce = jest.fn().mockResolvedValue({ reminders: 0, summaries: 0 });
    const scheduler = new ReminderScheduler(
      { runOnce } as unknown as ReminderService,
      config(true),
    );

    scheduler.onApplicationBootstrap();
    await jest.advanceTimersByTimeAsync(34_000);
    expect(runOnce).toHaveBeenCalledTimes(0);
    await jest.advanceTimersByTimeAsync(1_000);
    expect(runOnce).toHaveBeenCalledTimes(1);
    await jest.advanceTimersByTimeAsync(60_000);
    expect(runOnce).toHaveBeenCalledTimes(2);

    scheduler.onModuleDestroy();
    await jest.advanceTimersByTimeAsync(120_000);
    expect(runOnce).toHaveBeenCalledTimes(2);
  });

  it('starts no timer when turned off', () => {
    jest.useFakeTimers();
    const runOnce = jest.fn();
    new ReminderScheduler(
      { runOnce } as unknown as ReminderService,
      config(false),
    ).onApplicationBootstrap();

    jest.advanceTimersByTime(10 * 60_000);
    expect(runOnce).not.toHaveBeenCalled();
  });

  it('never overlaps a slow tick, and survives a failing one', async () => {
    let finish: () => void = () => undefined;
    const runOnce = jest
      .fn()
      .mockImplementationOnce(() => new Promise<void>((resolve) => (finish = resolve)))
      .mockRejectedValueOnce(new Error('database unavailable'))
      .mockResolvedValue({ reminders: 0, summaries: 0 });
    const scheduler = new ReminderScheduler(
      { runOnce } as unknown as ReminderService,
      config(true),
    );

    const slow = scheduler.tick();
    await scheduler.tick();
    expect(runOnce).toHaveBeenCalledTimes(1);
    finish();
    await slow;

    await expect(scheduler.tick()).resolves.toBeUndefined();
    await scheduler.tick();
    expect(runOnce).toHaveBeenCalledTimes(3);
  });
});

describe('NotificationDeliveriesRepository.claim', () => {
  const data = {
    userId: 'user-1',
    kind: NotificationKind.TASK_REMINDER,
    dedupeKey: 'TASK_REMINDER:task-1:2026-11-02T06:50:00.000Z',
    scheduledFor: new Date('2026-11-02T06:50:00Z'),
  };

  it('wins on the first insert and loses on a duplicate key', async () => {
    const create = jest
      .fn()
      .mockResolvedValueOnce({})
      .mockRejectedValueOnce(
        new Prisma.PrismaClientKnownRequestError('Unique constraint failed', {
          code: 'P2002',
          clientVersion: 'test',
        }),
      );
    const repository = new NotificationDeliveriesRepository({
      notificationDelivery: { create },
    } as unknown as PrismaService);

    expect(await repository.claim(data)).toBe(true);
    expect(await repository.claim(data)).toBe(false);
  });

  it('passes other database errors on', async () => {
    const repository = new NotificationDeliveriesRepository({
      notificationDelivery: { create: jest.fn().mockRejectedValue(new Error('connection lost')) },
    } as unknown as PrismaService);

    await expect(repository.claim(data)).rejects.toThrow('connection lost');
  });
});
