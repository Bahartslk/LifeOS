/**
 * Conversions between "YYYY-MM-DD" / "HH:mm" strings and the JS `Date`
 * values Prisma requires for `@db.Date`/`@db.Time` columns. Originally
 * Planner-local (`modules/planner/utils/task-date.util.ts`, matching
 * mobile's `TaskDueDate`) — promoted here in Sprint 16.0 now that Travel's
 * `Trip.startDate`/`endDate` and `ItineraryItem.date`/`startTime`/`endTime`
 * need the exact same conversions, per this project's "promote only once a
 * second feature needs the identical shape" rule.
 *
 * `DATE_PATTERN`/`TIME_PATTERN` (the validation-layer counterpart of the
 * conversions above) were promoted here in Sprint 17.5's architecture audit
 * — both regexes were independently redeclared, byte-for-byte identical, in
 * Planner's and Travel's DTOs (`CreateTaskDto`/`TaskQueryDto`,
 * `CreateTripDto`/`CreateItineraryItemDto`); a single source of truth here
 * removes that duplication per CLAUDE.md's "avoid duplicated code" rule.
 */

/** "YYYY-MM-DD", validated by `@Matches` on every date-only DTO field across Planner and Travel. */
export const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

/** 24-hour "HH:mm", validated by `@Matches` on every time-only DTO field across Planner and Travel. */
export const TIME_PATTERN = /^([01]\d|2[0-3]):[0-5]\d$/;

/** "YYYY-MM-DD" -> a `Date` at UTC midnight, the value Prisma expects for a `@db.Date` column. */
export function toDbDate(dateStr: string): Date {
  return new Date(`${dateStr}T00:00:00.000Z`);
}

/** "HH:mm" -> a `Date` on the Unix epoch date, the value Prisma expects for a `@db.Time` column. */
export function toDbTime(timeStr: string): Date {
  return new Date(`1970-01-01T${timeStr}:00.000Z`);
}

/** Inverse of `toDbDate` — extracts "YYYY-MM-DD" from a Prisma `@db.Date` value. */
export function fromDbDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

/** Inverse of `toDbTime` — extracts "HH:mm" from a Prisma `@db.Time` value, or `null`. */
export function fromDbTime(time: Date | null): string | null {
  return time ? time.toISOString().slice(11, 16) : null;
}
