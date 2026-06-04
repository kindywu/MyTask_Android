package com.example.myapplication.data.repository

import com.example.myapplication.data.db.dao.TaskDao
import com.example.myapplication.data.db.entity.TaskEntity
import com.example.myapplication.domain.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

    fun search(query: String): Flow<List<TaskEntity>> = dao.search(query)
    fun getAll(): Flow<List<TaskEntity>> = dao.getAll()
}
