package com.example.myapplication.ui.screen.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.db.entity.CategoryEntity
import com.example.myapplication.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repo: CategoryRepository) : ViewModel() {

    data class UiState(val categories: List<CategoryEntity> = emptyList())

    val state: StateFlow<UiState> = repo.allCategories
        .map { UiState(categories = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun create(name: String, color: Int) {
        viewModelScope.launch { repo.createCategory(name, color) }
    }
    fun update(cat: CategoryEntity) {
        viewModelScope.launch { repo.updateCategory(cat) }
    }
    fun delete(cat: CategoryEntity) {
        viewModelScope.launch { repo.deleteCategory(cat) }
    }

    class Factory(private val repo: CategoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CategoriesViewModel(repo) as T
    }
}
