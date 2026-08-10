package com.crattendance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.model.LectureType
import com.crattendance.ui.theme.CrIcons
import com.crattendance.utils.DateUtils
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

fun LectureType.displayLabel(): String = when (this) {
    LectureType.LECTURE -> "Lecture"
    LectureType.TUTORIAL -> "Tutorial"
    LectureType.PRACTICAL_LAB -> "Practical Lab"
    LectureType.WORKSHOP -> "Workshop"
    LectureType.SEMINAR -> "Seminar"
    LectureType.OTHER -> "Other"
}

fun dayOfWeekLabel(day: Int): String =
    DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.ENGLISH)

/**
 * The shared class card used on Home and Manage Classes. [actions] renders
 * trailing buttons (delete/hide) only on the Manage Classes screen.
 */
@Composable
fun ClassCard(
    cls: ClassEntity,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = {}
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = cls.shortName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
            Text(
                text = cls.fullNameWithCode,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            SuggestionChip(
                onClick = {},
                label = { Text(cls.lectureType.displayLabel()) },
                enabled = false
            )
            Spacer(Modifier.height(12.dp))
            MetaRow(CrIcons.Calendar, "${dayOfWeekLabel(cls.dayOfWeek)} ${DateUtils.formatTime(cls.startTime)}")
            MetaRow(CrIcons.Person, cls.teacherName)
            MetaRow(CrIcons.Class, cls.location, bold = true)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Credit Hours: ${cls.creditHours}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = DateUtils.formatTimeRange(cls.startTime, cls.endTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    bold: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
