package com.crattendance.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crattendance.data.model.StudentEntity
import com.crattendance.ui.theme.SuccessGreen
import com.crattendance.ui.theme.SuccessGreenDark

private val SrNoWidth = 44.dp
private val RegNoWidth = 96.dp

/**
 * Per-class attendance table: Sr. No | Name | Reg No | Present switch.
 * Rows are clickable to open the student popup. Serial numbers are per-section
 * (the list is already filtered), while ordering follows the global drag order.
 */
@Composable
fun AttendanceTable(
    students: List<StudentEntity>,
    attendance: Map<String, Boolean>,
    enabled: Boolean,
    onToggle: (studentId: String) -> Unit,
    onStudentClick: (StudentEntity) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sr. No",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(SrNoWidth)
            )
            Text(
                "Name",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Reg No",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(RegNoWidth)
            )
            Text(
                "Present",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(56.dp)
            )
        }
        HorizontalDivider()

        students.forEachIndexed { index, student ->
            val present = attendance[student.id] ?: false
            Surface(
                color = if (present && enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStudentClick(student) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(SrNoWidth)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = student.registrationNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(RegNoWidth)
                    )
                    Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                        Switch(
                            checked = present,
                            enabled = enabled,
                            onCheckedChange = { onToggle(student.id) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = SuccessGreen,
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
    }
}
