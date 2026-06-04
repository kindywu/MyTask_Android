package com.example.myapplication.ui.screen.search

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
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    private val taskRepo: TaskRepository,
    private val catRepo: CategoryRepository,
) : ViewModel() {

    data class UiState(
        val query: String = "",
        val tasks: List<TaskEntity> = emptyList(),
        val categories: List<CategoryEntity> = emptyList(),
        val filterPriority: String? = null,
        val filterCategoryId: Long? = null,
        val filterCompleted: Boolean? = null,
        val sortBy: SortBy = SortBy.CREATED,
    )

    enum class SortBy { DUE_DATE, PRIORITY, CREATED, TITLE }

    private val _query = MutableStateFlow("")
    private val _filterPriority = MutableStateFlow<String?>(null)
    private val _filterCategoryId = MutableStateFlow<Long?>(null)
    private val _filterCompleted = MutableStateFlow<Boolean?>(null)
    private val _sortBy = MutableStateFlow(SortBy.CREATED)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<UiState> = combine(
        _query, _filterPriority, _filterCategoryId, _filterCompleted, _sortBy
    ) { q, p, c, done, s -> listOf(q, p, c, done, s) }
        .flatMapLatest { (q, pri, cat, done, sort) ->
            combine(
                if ((q as String).isNotBlank()) taskRepo.search(q) else taskRepo.getAll(),
                catRepo.allCategories,
            ) { tasks, cats ->
                var filtered = tasks
                if (pri != null) filtered = filtered.filter { it.priority == pri }
                if (cat != null) filtered = filtered.filter { it.categoryId == cat }
                if (done != null) filtered = filtered.filter { it.isCompleted == done }

                val sorter: Comparator<TaskEntity> = when (sort as SortBy) {
                    SortBy.DUE_DATE -> compareBy { it.dueDate ?: Long.MAX_VALUE }
                    SortBy.PRIORITY -> compareBy { when (it.priority) { "HIGH" -> 0; "MEDIUM" -> 1; else -> 2 } }
                    SortBy.CREATED -> compareByDescending { it.createdAt }
                    SortBy.TITLE -> compareBy { it.title.lowercase() }
                }
                UiState(
                    query = q, tasks = filtered.sortedWith(sorter), categories = cats,
                    filterPriority = pri as String?, filterCategoryId = cat as Long?,
                    filterCompleted = done as Boolean?, sortBy = sort as SortBy,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setQuery(q: String) { _query.value = q }
    fun setFilterPriority(p: String?) { _filterPriority.value = p }
    fun setFilterCategory(id: Long?) { _filterCategoryId.value = id }
    fun setFilterCompleted(c: Boolean?) { _filterCompleted.value = c }
    fun setSortBy(s: SortBy) { _sortBy.value = s }
    fun clearFilters() {
        _filterPriority.value = null; _filterCategoryId.value = null; _filterCompleted.value = null
    }

    class Factory(
        private val taskRepo: TaskRepository, private val catRepo: CategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(taskRepo, catRepo) as T
    }
}
