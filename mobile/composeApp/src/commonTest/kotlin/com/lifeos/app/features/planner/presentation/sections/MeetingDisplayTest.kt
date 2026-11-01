package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Work
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.model.TaskType
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** How meetings read in the agenda lists and the task detail. */
class MeetingDisplayTest {

    private fun task(
        time: LocalTime? = LocalTime(10, 0),
        type: TaskType = TaskType.MEETING,
        duration: Int? = 60,
        location: String? = "Ofis",
    ) = Task(
        id = "t",
        title = "Sprint planlama",
        description = null,
        // Far from today, so the label is the date plus the time.
        dueDate = TaskDueDate(date = LocalDate(2031, 3, 4), time = time),
        priority = TaskPriority.MEDIUM,
        category = TaskCategory.WORK,
        status = TaskStatus.TODO,
        source = TaskSource.PLANNER,
        tags = emptyList(),
        createdAt = LocalDate(2026, 11, 1),
        type = type,
        durationMinutes = duration,
        location = location,
        reminderMinutesBefore = 10,
    )

    @Test
    fun aMeetingWithADuration_showsItsTimeRange() {
        assertTrue(task().dueDateDisplayLabel().endsWith("10:00–11:00"))
    }

    @Test
    fun withoutADuration_onlyTheStartTimeShows() {
        val label = task(duration = null).dueDateDisplayLabel()

        assertTrue(label.endsWith("10:00"))
        assertFalse(label.contains("–"))
    }

    @Test
    fun aRangePastMidnight_wrapsAround() {
        assertEquals(LocalTime(0, 30), task(time = LocalTime(23, 30)).endTime)
    }

    @Test
    fun meetingsGetTheGroupIcon_tasksKeepTheirCategoryIcon() {
        assertEquals(Icons.Filled.Groups, task().displayIcon())
        assertEquals(Icons.Filled.Work, task(type = TaskType.TASK).displayIcon())
    }

    @Test
    fun theMeetingLine_spellsOutTheTypeAndThePlace() {
        assertEquals("Toplantı · Ofis", PlannerStrings.meetingLine("Ofis"))
        assertEquals("Toplantı", PlannerStrings.meetingLine(null))
    }

    @Test
    fun theDetailReminderLabel_readsNaturally() {
        assertEquals("Kapalı", PlannerStrings.reminderDetailLabel(null))
        assertEquals("Zamanında", PlannerStrings.reminderDetailLabel(0))
        assertEquals("10 dk önce", PlannerStrings.reminderDetailLabel(10))
        assertEquals("1 saat önce", PlannerStrings.reminderDetailLabel(60))
    }
}
