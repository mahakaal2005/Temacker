package com.example.temacker.feature_tasks.data.mapper

import com.example.temacker.core.data.database.PendingWriteEntity
import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.PendingWriteType
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface PendingPayload {
    val type: PendingWriteType

    @Serializable
    @SerialName("create")
    data class CreateTask(
        val title: String,
        val description: String?,
        val dueDate: Long?,
        val holderUid: String,
        val holderDisplayName: String,
        val createdByDisplayName: String
    ) : PendingPayload {
        override val type get() = PendingWriteType.CREATE_TASK
    }

    @Serializable
    @SerialName("offer")
    data class Offer(val toUid: String, val toDisplayName: String, val note: String?) : PendingPayload {
        override val type get() = PendingWriteType.OFFER
    }

    @Serializable
    @SerialName("accept")
    data class Accept(val handoffId: String) : PendingPayload {
        override val type get() = PendingWriteType.ACCEPT
    }

    @Serializable
    @SerialName("decline")
    data class Decline(val handoffId: String, val reason: String) : PendingPayload {
        override val type get() = PendingWriteType.DECLINE
    }

    @Serializable
    @SerialName("markDone")
    data class MarkDone(val byUid: String, val byDisplayName: String) : PendingPayload {
        override val type get() = PendingWriteType.MARK_DONE
    }
}

fun PendingPayload.encode(): String = Json.encodeToString(PendingPayload.serializer(), this)

fun decodePayload(json: String): PendingPayload? =
    runCatching { Json.decodeFromString(PendingPayload.serializer(), json) }.getOrNull()

fun PendingWriteEntity.toDomain(): PendingWrite? {
    val writeType = runCatching { PendingWriteType.valueOf(type) }.getOrNull() ?: return null
    val writeStatus = runCatching { PendingWriteStatus.valueOf(status) }.getOrNull() ?: return null
    val draft = (decodePayload(payload) as? PendingPayload.CreateTask)?.let {
        Task(
            id = taskId,
            projectId = projectId,
            title = it.title,
            description = it.description,
            holderUid = it.holderUid,
            holderDisplayName = it.holderDisplayName,
            status = TaskStatus.TODO,
            dueDate = it.dueDate,
            timesHandedOver = 0,
            createdByUid = it.holderUid,
            createdByDisplayName = it.createdByDisplayName,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }
    return PendingWrite(id, writeType, projectId, taskId, taskTitle, writeStatus, attempts, lastError, createdAt, draft)
}
