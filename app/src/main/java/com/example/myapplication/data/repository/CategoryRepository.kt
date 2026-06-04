package com.example.myapplication.data.repository

import com.example.myapplication.data.db.dao.CategoryDao
import com.example.myapplication.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    val allCategories: Flow<List<CategoryEntity>> = dao.getAll()

    suspend fun createCategory(name: String, color: Int): Long =
        dao.insert(CategoryEntity(name = name, color = color))

    suspend fun updateCategory(category: CategoryEntity) = dao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        dao.deleteWithReferencesCleared(category)

    suspend fun getTaskCount(categoryId: Long): Int = dao.getTaskCountForCategory(categoryId)

    suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)
}
