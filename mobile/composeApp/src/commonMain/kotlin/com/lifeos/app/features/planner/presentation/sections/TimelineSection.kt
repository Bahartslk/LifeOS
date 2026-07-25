package com.lifeos.app.features.planner.presentation.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.date.AppDateFormatter
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.planner.domain.model.TaskActivityEntry
import com.lifeos.app.features.planner.presentation.PlannerStrings
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Activity Timeline" (this task's requirement): a chronological audit
 * log for the task (created, priority set, subtasks completed). A
 * dot-and-connecting-line vertical layout — distinct from, but named the
 * same as, [com.lifeos.app.features.travel.presentation.sections.TimelineSection]
 * (Travel Detail's day-by-day itinerary): different package, different
 * domain model, no actual naming collision.
 */
@Composable
fun TimelineSection(
    activity: List<TaskActivityEntry>,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = PlannerStrings.ACTIVITY_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Column {
            activity.forEachIndexed { index, entry ->
                key(entry.id) {
                    ActivityRow(entry = entry, isLast = index == activity.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(entry: TaskActivityEntry, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(TIMELINE_DOT_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(TIMELINE_LINE_WIDTH)
                        .height(TIMELINE_LINE_HEIGHT)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
        Spacer(modifier = Modifier.width(LifeOSSpacing.md))
        Column(
            modifier = Modifier.padding(bottom = if (isLast) 0.dp else LifeOSSpacing.md),
        ) {
            Text(text = entry.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = AppDateFormatter.toFullLabel(entry.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val TIMELINE_DOT_SIZE = 8.dp
private val TIMELINE_LINE_WIDTH = 2.dp
private val TIMELINE_LINE_HEIGHT = 32.dp

@Preview
@Composable
private fun TimelineSectionPreview() {
    LifeOSTheme {
        TimelineSection(
            activity = listOf(
                TaskActivityEntry(id = "activity-1", label = "Görev oluşturuldu", timestamp = LocalDate(2024, 10, 18)),
                TaskActivityEntry(
                    id = "activity-2",
                    label = "Öncelik \"Yüksek\" olarak ayarlandı",
                    timestamp = LocalDate(2024, 10, 18),
                ),
                TaskActivityEntry(
                    id = "activity-3",
                    label = "\"Veri analizini tamamla\" alt görevi tamamlandı",
                    timestamp = LocalDate(2024, 10, 20),
                ),
            ),
        )
    }
}
