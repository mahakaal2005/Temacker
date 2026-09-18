package com.example.temacker.feature_tasks.data.mapper

import com.example.temacker.core.data.database.HandoffEntity
import com.example.temacker.core.data.database.TaskEntity
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus

fun Task.toEntity() = TaskEntity(
    id = id,
    projectId = projectId,
    title = title,
    description = description,
    holderUid = holderUid,
    holderDisplayName = holderDisplayName,
    status = status.name,
    dueDate = dueDate,
    timesHandedOver = timesHandedOver,
    createdByUid = createdByUid,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TaskEntity.toDomain() = Task(
    id = id,
    projectId = projectId,
    title = title,
    description = description,
    holderUid = holderUid,
    holderDisplayName = holderDisplayName,
    status = TaskStatus.valueOf(status),
    dueDate = dueDate,
    timesHandedOver = timesHandedOver,
    createdByUid = createdByUid,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Handoff.toEntity(projectId: String) = HandoffEntity(
    id = id,
    taskId = taskId,
    projectId = projectId,
    fromUid = fromUid,
    fromDisplayName = fromDisplayName,
    toUid = toUid,
    toDisplayName = toDisplayName,
    note = note,
    status = status.name,
    declineReason = declineReason,
    offeredAt = offeredAt,
    respondedAt = respondedAt
)

fun HandoffEntity.toDomain() = Handoff(
    id = id,
    taskId = taskId,
    fromUid = fromUid,
    fromDisplayName = fromDisplayName,
    toUid = toUid,
    toDisplayName = toDisplayName,
    note = note,
    status = HandoffStatus.valueOf(status),
    declineReason = declineReason,
    offeredAt = offeredAt,
    respondedAt = respondedAt
)
