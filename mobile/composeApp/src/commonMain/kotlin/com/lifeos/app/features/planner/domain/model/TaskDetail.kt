package com.lifeos.app.features.planner.domain.model

import kotlinx.datetime.LocalDate

/**
 * The Task Detail screen's aggregate — wraps the existing [Task] rather
 * than adding detail-only fields to it, the same choice
 * [com.lifeos.app.features.travel.domain.model.TripDetail] made for [Task]'s
 * sibling [com.lifeos.app.features.travel.domain.model.Trip]: subtasks,
 * attachments, and activity history are never needed for a list row (the
 * Planner Dashboard, Home's future "Today's Priorities" mirror, or a
 * Packing-Checklist-mapped Travel task), only for this one detail screen —
 * so [Task] itself stays the lean, universally-reusable model this task's
 * "Task is now the central entity of LifeOS" requirement calls for.
 *
 * [notes] lives here, not on [Task], for the same reason: Trip Detail's
 * notes field only ever existed on `TripDetail`, never on `Trip`.
 */
data class TaskDetail(
    val task: Task,
    val notes: String,
    val subtasks: List<Subtask>,
    val attachments: List<TaskAttachment>,
    val activity: List<TaskActivityEntry>,
)

/**
 * One item in [TaskDetail.subtasks] — deliberately minimal (no priority,
 * due date, or category of its own): a subtask is a checklist line inside
 * a [Task], not a second [Task]-shaped entity. If a subtask ever needs to
 * become independently schedulable, it should be promoted to a real [Task]
 * with this one demoted to a reference, not grown in place.
 */
data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
)

/** A UI-only attachment reference (this task's "Attachments (UI only)" requirement) — no real file storage exists yet. */
data class TaskAttachment(
    val id: String,
    val fileName: String,
    val fileSizeLabel: String,
)

/**
 * One entry in [TaskDetail.activity] — a human-readable audit line, e.g.
 * "Subtask X completed". [timestamp] is a real date (this sprint's
 * refactor), formatted into Turkish text only where it's displayed
 * ([com.lifeos.app.features.planner.presentation.sections.TimelineSection]),
 * never inside `domain`.
 */
data class TaskActivityEntry(
    val id: String,
    val label: String,
    val timestamp: LocalDate,
)
