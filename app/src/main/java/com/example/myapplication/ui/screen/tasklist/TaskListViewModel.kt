package com.example.myapplication.ui.screen.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.db.entity.CategoryEntity
import com.example.myapplication.data.db.entity.TaskEntity
import com.example.myapplication.data.repository.CategoryRepository
import com.example.myapplication.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskListViewModel(
    private val taskRepo: TaskRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    data class UiState(
        val tasks: List<TaskItem> = emptyList(),
        val completedTasks: List<TaskItem> = emptyList(),
        val filter: String = "ALL",
        val categories: List<CategoryEntity> = emptyList(),
        val isLoading: Boolean = true,
    )

    data class TaskItem(
        val task: TaskEntity,
        val categoryName: String?,
        val categoryColor: Int?,
        val subtaskCount: Int = 0,
    )

    private val _filter = MutableStateFlow("ALL")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<UiState> = combine(_filter, catRepo.allCategories) { f, c -> f to c }
        .flatMapLatest { (filter, cats) ->
            taskRepo.rootTasks.map { tasks ->
                val now = Calendar.getInstance().timeInMillis
                val endOfWeek = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, 7)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                }.timeInMillis

                val filtered = when (filter) {
                    "TODAY" -> tasks.filter { it.dueDate != null && it.dueDate < now + 86400000 }
                    "WEEK" -> tasks.filter { it.dueDate != null && it.dueDate <= endOfWeek }
                    "HIGH" -> tasks.filter { it.priority == "HIGH" }
                    else -> tasks
                }

                val items = filtered.map { task ->
                    val cat = cats.find { it.id == task.categoryId }
                    TaskItem(task = task, categoryName = cat?.name, categoryColor = cat?.color)
                }

                UiState(
                    tasks = items.filter { !it.task.isCompleted },
                    completedTasks = items.filter { it.task.isCompleted },
                    filter = filter, categories = cats, isLoading = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setFilter(filter: String) { _filter.value = filter }
    fun toggleComplete(id: Long) { viewModelScope.launch { taskRepo.toggleComplete(id) } }
    fun deleteTask(id: Long) { viewModelScope.launch { taskRepo.deleteTask(id) } }

    class Factory(
        private val taskRepo: TaskRepository,
        private val catRepo: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TaskListViewModel(taskRepo, catRepo) as T
    }
}
