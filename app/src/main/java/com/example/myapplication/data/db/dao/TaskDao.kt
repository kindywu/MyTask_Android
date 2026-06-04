package com.example.myapplication.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.myapplication.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE parentTaskId IS NULL ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, sortOrder ASC")
    fun getRootTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentId ORDER BY sortOrder ASC")
    suspend fun getSubtasks(parentId: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')")
    fun search(query: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE parentTaskId = :parentId")
    suspend fun getSubtaskCount(parentId: Long): Int

    @RawQuery(observedEntities = [TaskEntity::class])
    fun searchFilterSort(query: SupportSQLiteQuery): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllSnapshot(): List<TaskEntity>

    @Transaction
    suspend fun deleteRecursively(taskId: Long) {
        for (sub in getSubtasks(taskId)) {
            deleteRecursively(sub.id)
        }
        deleteById(taskId)
    }
}
