package com.example.myapplication.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.db.converter.Converters
import com.example.myapplication.data.db.dao.CategoryDao
import com.example.myapplication.data.db.dao.TaskDao
import com.example.myapplication.data.db.entity.CategoryEntity
import com.example.myapplication.data.db.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
}
