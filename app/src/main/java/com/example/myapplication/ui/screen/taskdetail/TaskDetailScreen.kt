package com.example.myapplication.ui.screen.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.component.CategoryPicker
import com.example.myapplication.ui.component.PriorityPicker
import com.example.myapplication.ui.component.SubtaskList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    onSubtaskClick: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSubtaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved, state.isDeleted) {
        if (state.isSaved || state.isDeleted) onBack()
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task?") },
            text = { Text("This will also delete all subtasks.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setDueDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    if (showCategoryPicker) {
        CategoryPicker(
            categories = state.categories,
            selectedId = state.categoryId,
            onSelect = viewModel::setCategoryId,
            onDismiss = { showCategoryPicker = false },
        )
    }

    if (showSubtaskDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSubtaskDialog = false },
            title = { Text("New Subtask") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Title") }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = { if (name.isNotBlank()) { viewModel.addSubtask(name); showSubtaskDialog = false } }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showSubtaskDialog = false }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, "Delete")
                        }
                    }
                    TextButton(onClick = { viewModel.save() }) { Text("Save") }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(value = state.title, onValueChange = viewModel::setTitle, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = state.notes, onValueChange = viewModel::setNotes, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Text("Priority", style = MaterialTheme.typography.labelLarge)
            PriorityPicker(selected = state.priority, onSelect = viewModel::setPriority)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Due Date: ", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { showDatePicker = true }) {
                    Text(state.dueDate?.let { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Not set")
                }
                if (state.dueDate != null) TextButton(onClick = { viewModel.setDueDate(null) }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            }

            Text("Reminder", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(null to "None", 0 to "At time", 5 to "5 min", 15 to "15 min", 30 to "30 min", 60 to "1 hr", 1440 to "1 day").forEach { (v, label) ->
                    OutlinedButton(onClick = { viewModel.setReminder(v) }) {
                        Text(label, color = if (state.reminderMinutes == v) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Category: ", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { showCategoryPicker = true }) {
                    Text(state.categories.find { it.id == state.categoryId }?.name ?: "None")
                }
            }

            if (!state.isNew) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtasks", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showSubtaskDialog = true }) { Icon(Icons.Filled.Add, "Add Subtask") }
                }
                SubtaskList(subtasks = state.taskTree, onToggleComplete = viewModel::toggleSubtaskComplete, onSubtaskClick = onSubtaskClick)
            }
        }
    }
}
