import { Injectable, Logger, OnApplicationBootstrap, OnModuleDestroy } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../../config/configuration';
import { ReminderService } from './reminder.service';

const MINUTE_MS = 60_000;
/** Ticks a moment after each minute starts, so a reminder for 10:00 is checked at 10:00:05. */
const TICK_OFFSET_MS = 5_000;

/**
 * Runs `ReminderService.runOnce` every minute inside the always-on backend
 * process: no separate cron service, so no extra cost on Railway (its
 * cron jobs also can't run more often than every 5 minutes). A tick never
 * overlaps the previous one, and an error only skips that tick.
 *
 * Off when `REMINDER_SCHEDULER_ENABLED=false` and always in tests, so test
 * suites never start timers.
 */
@Injectable()
export class ReminderScheduler implements OnApplicationBootstrap, OnModuleDestroy {
  private readonly logger = new Logger(ReminderScheduler.name);
  private startTimer: NodeJS.Timeout | null = null;
  private interval: NodeJS.Timeout | null = null;
  private running = false;

  constructor(
    private readonly reminders: ReminderService,
    private readonly config: ConfigService<AppConfig, true>,
  ) {}

  onApplicationBootstrap(): void {
    if (!this.config.get('notifications', { infer: true }).schedulerEnabled) {
      this.logger.log('Reminder scheduler is off.');
      return;
    }
    const delay = MINUTE_MS - (Date.now() % MINUTE_MS) + TICK_OFFSET_MS;
    this.startTimer = setTimeout(() => {
      void this.tick();
      this.interval = setInterval(() => void this.tick(), MINUTE_MS);
    }, delay);
    this.logger.log('Reminder scheduler started (every minute).');
  }

  onModuleDestroy(): void {
    if (this.startTimer) clearTimeout(this.startTimer);
    if (this.interval) clearInterval(this.interval);
  }

  /** One run; public so tests can drive it. Skips if the previous run is still going. */
  async tick(now: Date = new Date()): Promise<void> {
    if (this.running) return;
    this.running = true;
    try {
      await this.reminders.runOnce(now);
    } catch (error) {
      this.logger.error(
        `Reminder tick failed: ${error instanceof Error ? error.message : 'unknown error'}`,
      );
    } finally {
      this.running = false;
    }
  }
}
