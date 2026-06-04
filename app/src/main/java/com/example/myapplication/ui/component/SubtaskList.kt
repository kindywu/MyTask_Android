package com.example.myapplication.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.TaskWithSubtasks

@Composable
fun SubtaskList(
    subtasks: List<TaskWithSubtasks>,
    level: Int = 0,
    onToggleComplete: (Long) -> Unit,
    onSubtaskClick: (Long) -> Unit,
) {
    Column {
        subtasks.forEach { item ->
            var expanded by remember { mutableStateOf(false) }
            val hasChildren = item.subtasks.isNotEmpty()
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubtaskClick(item.task.id) }
                        .padding(start = (level * 24).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hasChildren) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                if (expanded) Icons.Filled.KeyboardArrowDown
                                else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                            )
                        }
                    }
                    Checkbox(
                        checked = item.task.isCompleted,
                        onCheckedChange = { onToggleComplete(item.task.id) },
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (item.task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (expanded && hasChildren) {
                    SubtaskList(
                        subtasks = item.subtasks,
                        level = level + 1,
                        onToggleComplete = onToggleComplete,
                        onSubtaskClick = onSubtaskClick,
                    )
                }
            }
        }
    }
}
