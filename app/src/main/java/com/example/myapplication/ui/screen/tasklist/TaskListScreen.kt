package com.example.myapplication.ui.screen.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.component.EmptyState
import com.example.myapplication.ui.component.FilterChips
import com.example.myapplication.ui.component.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onTaskClick: (Long) -> Unit,
    onNewTask: () -> Unit,
    onSearch: () -> Unit,
    onCategories: () -> Unit,
    onSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, "Search") }
                    Box {
                        IconButton(onClick = { showMenu = true }) { Text("⋯") }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Categories") },
                                onClick = { showMenu = false; onCategories() },
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { showMenu = false; onSettings() },
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewTask) { Icon(Icons.Filled.Add, "New Task") }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            FilterChips(
                selected = state.filter,
                onSelect = viewModel::setFilter,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.tasks.isEmpty() && state.completedTasks.isEmpty()) {
                EmptyState("No tasks yet")
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.tasks, key = { it.task.id }) { item ->
                        TaskCard(
                            task = item.task, subtaskCount = item.subtaskCount,
                            categoryName = item.categoryName, categoryColor = item.categoryColor,
                            onToggleComplete = { viewModel.toggleComplete(item.task.id) },
                            onClick = { onTaskClick(item.task.id) },
                        )
                    }
                    if (state.completedTasks.isNotEmpty()) {
                        item {
                            Row(
                                Modifier.fillMaxWidth().clickable { showCompleted = !showCompleted }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(if (showCompleted) "▾" else "▸", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.width(8.dp))
                                Text("Completed (${state.completedTasks.size})", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (showCompleted) {
                            items(state.completedTasks, key = { it.task.id }) { item ->
                                TaskCard(
                                    task = item.task, subtaskCount = item.subtaskCount,
                                    categoryName = item.categoryName, categoryColor = item.categoryColor,
                                    onToggleComplete = { viewModel.toggleComplete(item.task.id) },
                                    onClick = { onTaskClick(item.task.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
