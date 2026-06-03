package com.example.myapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int = 0xFF4A90D9.toInt(),
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
