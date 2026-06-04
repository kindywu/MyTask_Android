package com.example.myapplication.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.repository.SortBy
import com.example.myapplication.ui.component.EmptyState
import com.example.myapplication.ui.component.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onTaskClick: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query, onValueChange = viewModel::setQuery,
                        placeholder = { Text("Search tasks...") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    FilterChip(
                        selected = state.filterPriority != null,
                        onClick = { showPriorityMenu = true },
                        label = { Text(state.filterPriority?.let { when (it) { "HIGH" -> "High"; "MEDIUM" -> "Med"; else -> "Low" } } ?: "Priority") },
                        trailingIcon = if (state.filterPriority != null) ({ Icon(Icons.Filled.Close, "Clear", Modifier.clickable { viewModel.setFilterPriority(null) }) }) else null,
                    )
                    DropdownMenu(expanded = showPriorityMenu, onDismissRequest = { showPriorityMenu = false }) {
                        listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { viewModel.setFilterPriority(p); showPriorityMenu = false })
                        }
                    }
                }
                FilterChip(
                    selected = state.filterCompleted != null,
                    onClick = {
                        when (state.filterCompleted) {
                            null -> viewModel.setFilterCompleted(false)
                            false -> viewModel.setFilterCompleted(true)
                            true -> viewModel.setFilterCompleted(null)
                        }
                    },
                    label = { Text(when (state.filterCompleted) { null -> "Status"; false -> "Active"; true -> "Done" }) },
                )
                Box {
                    FilterChip(selected = false, onClick = { showSortMenu = true }, label = { Text("Sort") })
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        SortBy.entries.forEach { s ->
                            DropdownMenuItem(text = { Text(s.name) }, onClick = { viewModel.setSortBy(s); showSortMenu = false })
                        }
                    }
                }
            }

            if (state.tasks.isEmpty()) {
                EmptyState(if (state.query.isNotBlank()) "No matching tasks" else "Type to search")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            categoryName = state.categories.find { it.id == task.categoryId }?.name,
                            categoryColor = state.categories.find { it.id == task.categoryId }?.color,
                            onToggleComplete = {},
                            onClick = { onTaskClick(task.id) },
                        )
                    }
                }
            }
        }
    }
}
