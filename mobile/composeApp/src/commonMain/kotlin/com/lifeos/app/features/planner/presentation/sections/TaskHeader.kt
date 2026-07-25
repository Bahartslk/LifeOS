package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The "Hero Header" (this task's requirement): a back button plus the
 * task's own title and due date, matching the [AppTopBar]-plus-title-block
 * shape [com.lifeos.app.features.travel.presentation.sections.HeroSection]
 * already established for Travel Detail — Task Detail has no cover photo,
 * so no [com.lifeos.app.core.designsystem.components.PhotoOverlayCard] is
 * used here (see this feature's "Design decisions" note).
 */
@Composable
fun TaskHeader(
    task: Task,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppTopBar(
            title = PlannerStrings.HEADER_TITLE,
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            navigationContentDescription = PlannerStrings.BACK_CONTENT_DESCRIPTION,
            onNavigationClick = onBackClick,
        )
        Column(modifier = Modifier.padding(horizontal = LifeOSSpacing.lg)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium,
                textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
            )
            Spacer(modifier = Modifier.height(LifeOSSpacing.xs))
            Text(
                text = task.dueDateDisplayLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun TaskHeaderPreview() {
    LifeOSTheme {
        TaskHeader(
            task = Task(
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
            onBackClick = {},
        )
    }
}
