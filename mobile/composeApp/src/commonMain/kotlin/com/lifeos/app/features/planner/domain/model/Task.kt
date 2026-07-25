package com.lifeos.app.features.planner.domain.model

import kotlinx.datetime.LocalDate

/**
 * The single, canonical task representation for the entire application —
 * per this task's "Planner is the central source of truth for every task"
 * requirement. Deliberately generic rather than Planner-specific, so it can
 * be reused, unmodified, once these future integrations are built:
 *
 * - Home's "Today's Priorities" reading directly from Planner instead of
 *   carrying its own `PriorityTask` model.
 * - Travel mapping Packing Checklist items into `Task`s (`source` = TRAVEL).
 * - AI Assistant creating `Task`s from conversation (`source` = AI_ASSISTANT),
 *   per docs/09-ai-features.md's "accepted suggestions become real Task
 *   records through the normal domain use cases" rule.
 *
 * Mirrors docs/14-database-design.md's `tasks` table columns
 * (`title`/`description`/`due_date`/`priority`/`status`) plus three
 * additions the current schema doesn't have yet but this task's screen
 * content requires: [category] (Quick Categories), [source] (provenance
 * once other features start creating tasks), and [tags]/[hasReminder]
 * (planner.png's "+4" attendee-style indicator and reminder bell). Adding
 * columns for these later is a additive, non-breaking migration.
 *
 * [dueDate]/[createdAt] are real domain date values, not pre-formatted
 * Turkish display strings — an architectural refactor sprint's requirement,
 * so `domain` no longer depends on presentation-shaped text and Calendar,
 * Home, and a future ISO-8601 backend can all consume real dates directly
 * instead of parsing them back out of frozen strings. Formatting into
 * Turkish text now lives entirely in `core/date` ([com.lifeos.app.core.date.AppDateFormatter],
 * [com.lifeos.app.core.date.RelativeDateFormatter]) — nothing in `domain`
 * ever formats a date.
 *
 * [estimatedDurationLabel] remains a pre-formatted display string
 * (Create Task's "45 dakika" free-text field, deliberately not a
 * structured duration type) — it is a label, not a point in time, so it
 * sits outside this refactor's scope entirely.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: TaskDueDate,
    val priority: TaskPriority,
    val category: TaskCategory,
    val status: TaskStatus,
    val source: TaskSource,
    val tags: List<String>,
    val hasReminder: Boolean,
    val createdAt: LocalDate,
    val estimatedDurationLabel: String? = null,
)

/** Mirrors `tasks.priority`'s `low`/`medium`/`high` check constraint (FR-PLANNER-06). */
enum class TaskPriority { LOW, MEDIUM, HIGH }

/** Mirrors `tasks.status`'s `todo`/`in_progress`/`done` check constraint. */
enum class TaskStatus { TODO, IN_PROGRESS, DONE }

/**
 * Not yet a `tasks` column in docs/14-database-design.md — added here to
 * support this task's "Quick Categories" screen requirement. A future
 * backend migration would add a matching `category` column; until then this
 * is a client-only enrichment, the same way [com.lifeos.app.features.travel.domain.model.Trip]
 * carries fields no `trips` column backs either.
 */
enum class TaskCategory { WORK, PERSONAL, HEALTH, TRAVEL, FINANCE }

/**
 * Which feature created this task — the provenance field that makes
 * "Travel maps Packing Checklist items into Tasks" and "AI Assistant
 * generates Planner tasks" safe to build later without ambiguity about
 * where a given task came from or how it should be displayed/edited.
 */
enum class TaskSource { PLANNER, HOME, TRAVEL, AI_ASSISTANT }
