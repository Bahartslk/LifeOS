import { TaskType } from '@prisma/client';
import { ScheduledTaskInfo } from '../../planner/planner.service';
import { PushMessage } from '../push/push-sender';

/** Android notification channels the app creates (mobile, PR 5). */
export const REMINDERS_CHANNEL = 'reminders';
export const DAILY_SUMMARY_CHANNEL = 'daily_summary';

/** "10 dakika sonra" / "1 saat sonra". */
function leadLabel(minutes: number): string {
  return minutes >= 60 && minutes % 60 === 0
    ? `${minutes / 60} saat sonra`
    : `${minutes} dakika sonra`;
}

/** "10:00" → "10:00–11:00" when a duration is known (wraps past midnight). */
function timeRange(task: ScheduledTaskInfo): string {
  const start = task.dueTime ?? '';
  if (!task.durationMinutes || !task.dueTime) return start;
  const [hour, minute] = task.dueTime.split(':').map(Number);
  const total = (hour * 60 + minute + task.durationMinutes) % (24 * 60);
  const end = `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
  return `${start}–${end}`;
}

/**
 * The push reminder for a task or meeting, in Turkish like the app. The only
 * place this wording lives. Tapping it opens the task (`data.taskId`).
 */
export function buildTaskReminder(task: ScheduledTaskInfo): PushMessage {
  const minutes = task.reminderMinutesBefore ?? 0;
  const isMeeting = task.type === TaskType.MEETING;
  const title = isMeeting
    ? minutes === 0
      ? `Toplantı başlıyor: ${task.title}`
      : `Toplantı ${leadLabel(minutes)}: ${task.title}`
    : minutes === 0
      ? `Zamanı geldi: ${task.title}`
      : `Hatırlatma: ${task.title}`;
  const when =
    minutes === 0 || isMeeting ? timeRange(task) : `${leadLabel(minutes)} · ${task.dueTime}`;
  const body = [when, isMeeting ? task.location : null].filter(Boolean).join(' · ');
  return {
    title,
    body,
    channelId: REMINDERS_CHANNEL,
    data: { type: 'TASK_REMINDER', taskId: task.id },
  };
}

/**
 * The 08:00 morning summary, built without AI (the free Gemini quota can't
 * cover a daily push for everyone). `tasks` are today's unfinished tasks,
 * earliest first. Tapping it opens the Daily Brief.
 */
export function buildDailySummary(tasks: ScheduledTaskInfo[], localDate: string): PushMessage {
  const meetings = tasks.filter((task) => task.type === TaskType.MEETING).length;
  const firstTimed = tasks.find((task) => task.dueTime);
  const parts = [
    meetings > 0 ? `${meetings} toplantı dahil.` : null,
    firstTimed ? `İlki ${firstTimed.dueTime}: ${firstTimed.title}.` : null,
    'Günün özeti seni bekliyor.',
  ];
  return {
    title: `Günaydın! Bugün ${tasks.length} görevin var`,
    body: parts.filter(Boolean).join(' '),
    channelId: DAILY_SUMMARY_CHANNEL,
    data: { type: 'DAILY_SUMMARY', date: localDate },
  };
}
