package com.lifeos.app.features.planner.data.datasource

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.PlannerOverview
import com.lifeos.app.features.planner.domain.model.Subtask
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskActivityEntry
import com.lifeos.app.features.planner.domain.model.TaskAttachment
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.util.CalendarMonthBuilder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * The one place fake Planner data lives, per this task's "keep fake data
 * inside dedicated fake data sources" rule. Holds no business logic —
 * [com.lifeos.app.features.planner.data.repository.FakePlannerRepository]
 * owns the (currently trivial) orchestration.
 *
 * Content mirrors design/stitch/planner.png as closely as the fake-data
 * constraint allows. Both [todayTasks] and [upcomingTasks] are mutable so
 * [toggleTaskCompletion] and (added for Task Detail) [deleteTask] can give
 * real, persisted-for-the-session interactivity — the same pattern
 * [com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource]
 * established for `saveTrip`.
 *
 * Every [Task.dueDate]/[Task.createdAt] here is a real [LocalDate]/[TaskDueDate]
 * value (this sprint's domain-date refactor) — no more per-task string
 * literals, and no more separate `taskDatesByTaskId` lookup table for
 * Calendar: [tasksForDay] now just compares real dates directly.
 */
class FakePlannerDataSource {

    private val todayTasks = mutableListOf(
        Task(
            id = "task-morning-routine",
            title = "Sabah Rutini",
            description = null,
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(8, 30)),
            priority = TaskPriority.LOW,
            category = TaskCategory.PERSONAL,
            status = TaskStatus.DONE,
            source = TaskSource.PLANNER,
            tags = emptyList(),
            hasReminder = false,
            createdAt = LocalDate(2024, 10, 20),
        ),
        Task(
            id = "task-team-meeting",
            title = "Ekip Toplantısı",
            description = "Sprint planlama ve önceliklendirme görüşmesi",
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(10, 0)),
            priority = TaskPriority.MEDIUM,
            category = TaskCategory.WORK,
            status = TaskStatus.IN_PROGRESS,
            source = TaskSource.PLANNER,
            tags = listOf("Ayşe", "Mehmet", "Zeynep", "Can"),
            hasReminder = true,
            createdAt = LocalDate(2024, 10, 21),
        ),
        Task(
            id = "task-lunch-break",
            title = "Öğle Arası",
            description = null,
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(13, 0)),
            priority = TaskPriority.LOW,
            category = TaskCategory.PERSONAL,
            status = TaskStatus.TODO,
            source = TaskSource.PLANNER,
            tags = emptyList(),
            hasReminder = false,
            createdAt = LocalDate(2024, 10, 21),
        ),
        Task(
            id = "task-project-work",
            title = "Proje Çalışması",
            description = "Üç aylık rapor teslim tarihi yaklaşıyor",
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(15, 30)),
            priority = TaskPriority.HIGH,
            category = TaskCategory.WORK,
            status = TaskStatus.TODO,
            source = TaskSource.PLANNER,
            tags = emptyList(),
            hasReminder = true,
            createdAt = LocalDate(2024, 10, 18),
        ),
        Task(
            id = "task-sport",
            title = "Spor",
            description = null,
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 24), time = LocalTime(19, 0)),
            priority = TaskPriority.MEDIUM,
            category = TaskCategory.HEALTH,
            status = TaskStatus.TODO,
            source = TaskSource.PLANNER,
            tags = emptyList(),
            hasReminder = true,
            createdAt = LocalDate(2024, 10, 22),
        ),
    )

    private val upcomingTasks = mutableListOf(
        Task(
            id = "task-cappadocia-trip",
            title = "Kapadokya Seyahati",
            description = "Balon turu ve mağara otel konaklaması",
            dueDate = TaskDueDate(date = LocalDate(2024, 11, 12)),
            priority = TaskPriority.MEDIUM,
            category = TaskCategory.TRAVEL,
            status = TaskStatus.TODO,
            source = TaskSource.TRAVEL,
            tags = listOf("seyahat"),
            hasReminder = true,
            createdAt = LocalDate(2024, 10, 1),
        ),
        Task(
            id = "task-doctor-appointment",
            title = "Doktor Randevusu",
            description = null,
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 25), time = LocalTime(14, 0)),
            priority = TaskPriority.HIGH,
            category = TaskCategory.HEALTH,
            status = TaskStatus.TODO,
            source = TaskSource.PLANNER,
            tags = emptyList(),
            hasReminder = true,
            createdAt = LocalDate(2024, 10, 15),
        ),
        Task(
            id = "task-presentation-prep",
            title = "Sunum Hazırlığı",
            description = "Yönetim kurulu toplantısı için slaytları tamamla",
            dueDate = TaskDueDate(date = LocalDate(2024, 10, 28)),
            priority = TaskPriority.HIGH,
            category = TaskCategory.WORK,
            status = TaskStatus.TODO,
            source = TaskSource.PLANNER,
            tags = emptyList(),
            hasReminder = false,
            createdAt = LocalDate(2024, 10, 19),
        ),
    )

    /**
     * Notes entered at creation time (Create Task's requirement) —
     * overrides [notesFor]'s hardcoded per-id fallback once a task has a
     * real entry here, so a note typed into Create Task's form is what
     * Task Detail actually shows afterward, not the empty default.
     */
    private val notesByTaskId = mutableMapOf<String, String>()

    /** Per-task subtask lists (Task Detail's requirement) — only tasks with real subtasks need an entry here. */
    private val subtasksByTaskId = mutableMapOf(
        "task-project-work" to mutableListOf(
            Subtask(id = "subtask-data-analysis", title = "Veri analizini tamamla", isCompleted = true),
            Subtask(id = "subtask-draft-slides", title = "Sunum taslağını hazırla", isCompleted = true),
            Subtask(id = "subtask-manager-review", title = "Yöneticiyle gözden geçir", isCompleted = false),
            Subtask(id = "subtask-final-touches", title = "Son düzeltmeleri yap", isCompleted = false),
        ),
        "task-team-meeting" to mutableListOf(
            Subtask(id = "subtask-share-agenda", title = "Gündem maddelerini paylaş", isCompleted = true),
            Subtask(id = "subtask-distribute-notes", title = "Toplantı notlarını dağıt", isCompleted = false),
        ),
    )

    fun getDashboard(): PlannerDashboard = PlannerDashboard(
        date = AppToday.date,
        aiInsightMessage = "Bugün yoğun bir günün var. Saat 15:30'daki görevin öncesinde kısa bir mola " +
            "vermeni öneriyorum.",
        overview = PlannerOverview(
            // Total/completed intentionally count more than todayTasks/upcomingTasks show —
            // this dashboard only ever renders "today" plus the next few important dates,
            // never the user's full backlog, the same way TravelStatistics.totalTripCount
            // includes archived trips no list on screen actually renders.
            totalTaskCount = todayTasks.size + upcomingTasks.size + OTHER_TASK_COUNT,
            upcomingEventCount = upcomingTasks.size,
            completedTaskCount = todayTasks.count { it.status == TaskStatus.DONE } + BASE_COMPLETED_COUNT,
            productivityPercent = PRODUCTIVITY_PERCENT,
        ),
        calendar = calendarMonth(AppToday.date.year, AppToday.date.monthNumber),
        todayTasks = todayTasks.toList(),
        upcomingTasks = upcomingTasks.toList(),
    )

    /**
     * Flips one today-task's completion state. Purely a same-session
     * in-memory mutation today (no persistence layer exists), but routed
     * through here — not the ViewModel — so a future real repository swap
     * only changes this class and [com.lifeos.app.features.planner.data.repository.FakePlannerRepository].
     */
    fun toggleTaskCompletion(taskId: String) {
        val index = todayTasks.indexOfFirst { it.id == taskId }
        if (index == -1) return
        val task = todayTasks[index]
        val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
        todayTasks[index] = task.copy(status = newStatus)
    }

    /**
     * Detail-only content for one task (Task Detail's requirement), keyed
     * off the same [Task.id] every list row already carries — no separate
     * "task detail id" concept needed. Returns `null` for an unknown id so
     * [com.lifeos.app.features.planner.data.repository.FakePlannerRepository]
     * can surface [com.lifeos.app.features.planner.domain.model.TaskNotFoundException].
     */
    fun getTaskDetail(taskId: String): TaskDetail? {
        val task = (todayTasks + upcomingTasks).find { it.id == taskId } ?: return null
        return TaskDetail(
            task = task,
            notes = notesFor(taskId),
            subtasks = subtasksByTaskId.getOrPut(taskId) { mutableListOf() }.toList(),
            attachments = attachmentsFor(taskId),
            activity = activityFor(task),
        )
    }

    /** Flips one subtask's completion state — see [toggleTaskCompletion]'s KDoc for why this lives here. */
    fun toggleSubtaskCompletion(taskId: String, subtaskId: String) {
        val subtasks = subtasksByTaskId[taskId] ?: return
        val index = subtasks.indexOfFirst { it.id == subtaskId }
        if (index == -1) return
        subtasks[index] = subtasks[index].copy(isCompleted = !subtasks[index].isCompleted)
    }

    /**
     * Removes a task from whichever list holds it. A real, in-memory
     * removal (not a no-op) — Task Detail's Delete button should make the
     * task actually disappear from the Planner Dashboard for the rest of
     * the session, the same "fake data, real mutation" standard every
     * other interactive action in this app already meets.
     */
    fun deleteTask(taskId: String) {
        todayTasks.removeAll { it.id == taskId }
        upcomingTasks.removeAll { it.id == taskId }
        subtasksByTaskId.remove(taskId)
    }

    /**
     * Builds and stores a brand-new [Task] from Create Task's form input —
     * this data source (not the ViewModel) assigns every field the form
     * itself has no way to know: [Task.id] (a stable, unique-per-session
     * suffix, the same [nextGeneratedTripId][com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource.nextGeneratedTripId]
     * pattern Travel already established), [Task.status] (always `TODO` —
     * a task is never created already done), [Task.source] (always
     * `PLANNER` — only Planner's own Create Task flow calls this), and
     * [Task.createdAt] (this app's fake "today", [AppToday.date]).
     *
     * Always appended to [todayTasks], never [upcomingTasks] — matching
     * this fake data's pre-existing behavior; [request.dueDate] itself may
     * of course be any real date, today or otherwise.
     */
    fun createTask(request: CreateTaskRequest): Task {
        val task = Task(
            id = "task-created-${nextTaskIdSuffix++}",
            title = request.title,
            description = request.description,
            dueDate = request.dueDate,
            priority = request.priority,
            category = request.category,
            status = TaskStatus.TODO,
            source = TaskSource.PLANNER,
            tags = request.tags,
            hasReminder = request.hasReminder,
            createdAt = AppToday.date,
            estimatedDurationLabel = request.estimatedDurationLabel,
        )
        todayTasks.add(task)
        if (!request.notes.isNullOrBlank()) {
            notesByTaskId[task.id] = request.notes
        }
        return task
    }

    private fun notesFor(taskId: String): String = notesByTaskId[taskId] ?: when (taskId) {
        "task-project-work" -> "Yönetim kuruluna sunmadan önce finans ekibinden geri bildirim al."
        else -> ""
    }

    private fun attachmentsFor(taskId: String): List<TaskAttachment> = when (taskId) {
        "task-project-work" -> listOf(
            TaskAttachment(id = "attachment-report-draft", fileName = "Q3_Rapor_Taslak.pdf", fileSizeLabel = "2.4 MB"),
            TaskAttachment(id = "attachment-notes", fileName = "Sunum_Notlari.docx", fileSizeLabel = "540 KB"),
        )
        else -> emptyList()
    }

    private fun activityFor(task: Task): List<TaskActivityEntry> = when (task.id) {
        "task-project-work" -> listOf(
            TaskActivityEntry(id = "activity-created", label = "Görev oluşturuldu", timestamp = task.createdAt),
            TaskActivityEntry(
                id = "activity-priority",
                label = "Öncelik \"Yüksek\" olarak ayarlandı",
                timestamp = task.createdAt,
            ),
            TaskActivityEntry(
                id = "activity-subtask-1",
                label = "\"Veri analizini tamamla\" alt görevi tamamlandı",
                timestamp = LocalDate(2024, 10, 20),
            ),
            TaskActivityEntry(
                id = "activity-subtask-2",
                label = "\"Sunum taslağını hazırla\" alt görevi tamamlandı",
                timestamp = LocalDate(2024, 10, 22),
            ),
        )
        else -> listOf(
            TaskActivityEntry(id = "activity-created-${task.id}", label = "Görev oluşturuldu", timestamp = task.createdAt),
        )
    }

    /**
     * A complete month grid for any (year, month), for previews only — the
     * grid arithmetic itself lives in [CalendarMonthBuilder] (shared with
     * the real repository); task counts per day come from this class's own
     * fake task set.
     */
    fun calendarMonth(year: Int, month: Int): PlannerCalendarMonth =
        CalendarMonthBuilder.build(year = year, month = month, tasks = todayTasks + upcomingTasks)

    /**
     * Every [Task] (today's or upcoming's — the exact same lists every
     * other screen reads) due on this real calendar date. A plain equality
     * filter on [Task.dueDate] now that it's a real date (this sprint's
     * refactor removed the separate `taskDatesByTaskId` lookup table this
     * used to need). Returns real [Task] objects, never a
     * `CalendarTask`/`CalendarEvent` — Calendar is simply another view over
     * the same task list.
     */
    fun tasksForDay(date: LocalDate): List<Task> = (todayTasks + upcomingTasks).filter { it.dueDate.date == date }

    /** A stable, unique-per-session suffix for [createTask] — see its KDoc for why. */
    private var nextTaskIdSuffix = 1

    private companion object {
        const val OTHER_TASK_COUNT = 4
        const val BASE_COMPLETED_COUNT = 7
        const val PRODUCTIVITY_PERCENT = 92
    }
}
