package com.example.myapplication.domain.model

import com.example.myapplication.data.db.entity.TaskEntity

data class TaskWithSubtasks(
    val task: TaskEntity,
    val subtasks: List<TaskWithSubtasks> = emptyList(),
)
