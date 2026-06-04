package com.example.myapplication.ui.screen.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.db.entity.CategoryEntity
import com.example.myapplication.data.db.entity.TaskEntity
import com.example.myapplication.data.repository.CategoryRepository
import com.example.myapplication.data.repository.TaskRepository
import com.example.myapplication.domain.model.TaskWithSubtasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: Long,
    private val parentTaskId: Long?,
    private val taskRepo: TaskRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    data class UiState(
        val isNew: Boolean = false,
        val title: String = "",
        val notes: String = "",
        val priority: String = "MEDIUM",
        val dueDate: Long? = null,
        val reminderMinutes: Int? = null,
        val categoryId: Long? = null,
        val categories: List<CategoryEntity> = emptyList(),
        val taskTree: List<TaskWithSubtasks> = emptyList(),
        val isSaved: Boolean = false,
        val isDeleted: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState(isNew = taskId == 0L))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        if (taskId > 0) loadTask()
        viewModelScope.launch {
            catRepo.allCategories.collect { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            val task = taskRepo.getById(taskId)
            if (task != null) {
                _state.update {
                    it.copy(
                        title = task.title, notes = task.notes, priority = task.priority,
                        dueDate = task.dueDate, reminderMinutes = task.reminderMinutes,
                        categoryId = task.categoryId,
                    )
                }
            }
            refreshSubtasks()
        }
    }

    private fun refreshSubtasks() {
        viewModelScope.launch {
            val tree = if (taskId > 0) taskRepo.buildTaskTree(taskId) else emptyList()
            _state.update { it.copy(taskTree = tree) }
        }
    }

    fun setTitle(t: String) { _state.update { it.copy(title = t) } }
    fun setNotes(n: String) { _state.update { it.copy(notes = n) } }
    fun setPriority(p: String) { _state.update { it.copy(priority = p) } }
    fun setDueDate(d: Long?) { _state.update { it.copy(dueDate = d) } }
    fun setReminder(m: Int?) { _state.update { it.copy(reminderMinutes = m) } }
    fun setCategoryId(id: Long?) { _state.update { it.copy(categoryId = id) } }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            if (s.title.isBlank()) return@launch
            if (s.isNew) {
                taskRepo.createTask(
                    title = s.title, notes = s.notes, priority = s.priority,
                    dueDate = s.dueDate, reminderMinutes = s.reminderMinutes,
                    categoryId = s.categoryId, parentTaskId = parentTaskId,
                )
            } else {
                val existing = taskRepo.getById(taskId) ?: return@launch
                taskRepo.updateTask(
                    existing.copy(
                        title = s.title, notes = s.notes, priority = s.priority,
                        dueDate = s.dueDate, reminderMinutes = s.reminderMinutes,
                        categoryId = s.categoryId,
                    )
                )
            }
            _state.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        viewModelScope.launch { taskRepo.deleteTask(taskId); _state.update { it.copy(isDeleted = true) } }
    }

    fun toggleSubtaskComplete(id: Long) {
        viewModelScope.launch { taskRepo.toggleComplete(id); refreshSubtasks() }
    }

    fun addSubtask(title: String) {
        viewModelScope.launch { taskRepo.createTask(title = title, parentTaskId = taskId); refreshSubtasks() }
    }

    class Factory(
        private val taskId: Long, private val parentTaskId: Long?,
        private val taskRepo: TaskRepository, private val catRepo: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskDetailViewModel(taskId, parentTaskId, taskRepo, catRepo) as T
    }
}
