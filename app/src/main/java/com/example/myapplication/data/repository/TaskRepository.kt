package com.example.myapplication.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.myapplication.data.db.dao.TaskDao
import com.example.myapplication.data.db.entity.TaskEntity
import com.example.myapplication.domain.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

enum class SortBy { DUE_DATE, PRIORITY, CREATED, TITLE }

class TaskRepository(private val dao: TaskDao) {
    val rootTasks: Flow<List<TaskEntity>> = dao.getRootTasks()

    suspend fun getById(id: Long): TaskEntity? = dao.getById(id)
    fun getByIdFlow(id: Long): Flow<TaskEntity?> = dao.getByIdFlow(id)

    suspend fun getSubtasks(parentId: Long): List<TaskEntity> = dao.getSubtasks(parentId)
    suspend fun getSubtaskCount(taskId: Long): Int = dao.getSubtaskCount(taskId)

    suspend fun createTask(
        title: String,
        notes: String = "",
        priority: String = "MEDIUM",
        dueDate: Long? = null,
        reminderMinutes: Int? = null,
        categoryId: Long? = null,
        parentTaskId: Long? = null,
        sortOrder: Int = 0,
    ): Long {
        val now = System.currentTimeMillis()
        return dao.insert(
            TaskEntity(
                title = title,
                notes = notes,
                priority = priority,
                dueDate = dueDate,
                reminderMinutes = reminderMinutes,
                categoryId = categoryId,
                parentTaskId = parentTaskId,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun updateTask(task: TaskEntity) {
        dao.update(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleComplete(taskId: Long) {
        val task = dao.getById(taskId) ?: return
        dao.update(task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(taskId: Long) = dao.deleteRecursively(taskId)

    suspend fun buildTaskTree(parentId: Long? = null): List<TaskWithSubtasks> {
        val tasks = if (parentId == null) dao.getRootTasks().first()
        else dao.getSubtasks(parentId)
        return tasks.map { task ->
            TaskWithSubtasks(task = task, subtasks = buildTaskTree(task.id))
        }
    }

    fun searchFilterSort(
        query: String,
        filterPriority: String?,
        filterCategoryId: Long?,
        filterCompleted: Boolean?,
        sortBy: SortBy,
    ): Flow<List<TaskEntity>> {
        val where = mutableListOf<String>()
        val args = mutableListOf<Any>()

        if (query.isNotBlank()) {
            where.add("(title LIKE '%' || ? || '%' OR notes LIKE '%' || ? || '%')")
            args.add(query)
            args.add(query)
        }

        if (filterPriority != null) {
            where.add("priority = ?")
            args.add(filterPriority)
        }

        if (filterCategoryId != null) {
            where.add("categoryId = ?")
            args.add(filterCategoryId)
        }

        if (filterCompleted != null) {
            where.add("isCompleted = ?")
            args.add(if (filterCompleted) 1 else 0)
        }

        val whereClause = if (where.isEmpty()) "" else "WHERE " + where.joinToString(" AND ")

        val orderBy = when (sortBy) {
            SortBy.DUE_DATE -> "ORDER BY CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate ASC"
            SortBy.PRIORITY -> "ORDER BY CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END ASC"
            SortBy.CREATED -> "ORDER BY createdAt DESC"
            SortBy.TITLE -> "ORDER BY title COLLATE NOCASE ASC"
        }

        val sql = "SELECT * FROM tasks $whereClause $orderBy"
        return dao.searchFilterSort(SimpleSQLiteQuery(sql, args.toTypedArray()))
    }

    fun search(query: String): Flow<List<TaskEntity>> = dao.search(query)
    fun getAll(): Flow<List<TaskEntity>> = dao.getAll()
}
