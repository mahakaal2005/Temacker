package com.example.temacker.feature_tasks.data.remote

import com.example.temacker.core.data.firebase.safeFirestoreCall
import com.example.temacker.core.data.firebase.snapshots
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.data.mapper.eventFirestoreMap
import com.example.temacker.feature_tasks.data.mapper.toEvent
import com.example.temacker.feature_tasks.data.mapper.toFirestoreMap
import com.example.temacker.feature_tasks.data.mapper.toHandoff
import com.example.temacker.feature_tasks.data.mapper.toTask
import com.example.temacker.feature_tasks.domain.model.Event
import com.example.temacker.feature_tasks.domain.model.EventType
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreTaskRemoteDataSource(
    private val firestore: FirebaseFirestore
) : TaskRemoteDataSource {

    private fun tasksRef(projectId: String) =
        firestore.collection("projects").document(projectId).collection("tasks")

    private fun handoffsRef(projectId: String, taskId: String) =
        tasksRef(projectId).document(taskId).collection("handoffs")

    private fun eventsRef(projectId: String) =
        firestore.collection("projects").document(projectId).collection("events")

    override fun observeTasks(projectId: String): Flow<List<Task>> =
        tasksRef(projectId).snapshots().map { snapshot -> snapshot.documents.mapNotNull { it.toTask(projectId) } }

    override fun observeTask(projectId: String, taskId: String): Flow<Task?> =
        tasksRef(projectId).document(taskId).snapshots().map { it.toTask(projectId) }

    override fun observeHandoffs(projectId: String, taskId: String): Flow<List<Handoff>> =
        handoffsRef(projectId, taskId).snapshots().map { snapshot -> snapshot.documents.mapNotNull { it.toHandoff(taskId) } }

    override fun observeEvents(projectId: String, limit: Int): Flow<List<Event>> =
        eventsRef(projectId)
            .orderBy("at", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toEvent(projectId) } }

    override fun observePendingHandoffs(projectId: String, toUid: String): Flow<List<Handoff>> =
        firestore.collectionGroup("handoffs")
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("toUid", toUid)
            .whereEqualTo("status", HandoffStatus.OFFERED.name)
            .snapshots()
            .map { it.toHandoffs() }

    override fun observeSentHandoffs(projectId: String, fromUid: String): Flow<List<Handoff>> =
        firestore.collectionGroup("handoffs")
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("fromUid", fromUid)
            .snapshots()
            .map { it.toHandoffs() }

    // taskId is the handoff doc's grandparent id (tasks/{taskId}/handoffs/{id}).
    private fun QuerySnapshot.toHandoffs(): List<Handoff> = documents.mapNotNull { doc ->
        val taskId = doc.reference.parent.parent?.id ?: return@mapNotNull null
        doc.toHandoff(taskId)
    }

    override suspend fun createTask(
        projectId: String,
        title: String,
        description: String?,
        dueDate: Long?,
        holderUid: String,
        holderDisplayName: String,
        createdByDisplayName: String,
        taskId: String?
    ): Result<Task, DataError> = safeFirestoreCall {
        // A queued create carries its own id so a replay overwrites the same doc instead of duplicating it.
        val ref = taskId?.let { tasksRef(projectId).document(it) } ?: tasksRef(projectId).document()
        val now = System.currentTimeMillis()
        val task = Task(
            id = ref.id,
            projectId = projectId,
            title = title,
            description = description,
            holderUid = holderUid,
            holderDisplayName = holderDisplayName,
            status = TaskStatus.TODO,
            dueDate = dueDate,
            timesHandedOver = 0,
            createdByUid = holderUid,
            createdByDisplayName = createdByDisplayName,
            createdAt = now,
            updatedAt = now
        )
        firestore.batch()
            .set(ref, task.toFirestoreMap())
            .set(eventsRef(projectId).document(), eventFirestoreMap(EventType.TASK_CREATED, task.id, title, holderUid, createdByDisplayName))
            .commit()
            .await()
        task
    }

    override suspend fun offerHandoff(
        projectId: String,
        taskId: String,
        toUid: String,
        toDisplayName: String,
        note: String?
    ): Result<Handoff, DataError> = safeFirestoreCall {
        val taskRef = tasksRef(projectId).document(taskId)
        val handoffRef = handoffsRef(projectId, taskId).document()
        firestore.runTransaction { txn ->
            val taskSnap = txn.get(taskRef)
            val fromUid = taskSnap.getString("holderUid")
                ?: throw FirebaseFirestoreException("Task not found", FirebaseFirestoreException.Code.NOT_FOUND)
            val fromDisplayName = taskSnap.getString("holderDisplayName") ?: fromUid
            val taskTitle = taskSnap.getString("title") ?: "Untitled task"
            val now = System.currentTimeMillis()
            val handoff = Handoff(
                id = handoffRef.id,
                taskId = taskId,
                fromUid = fromUid,
                fromDisplayName = fromDisplayName,
                toUid = toUid,
                toDisplayName = toDisplayName,
                note = note,
                status = HandoffStatus.OFFERED,
                declineReason = null,
                offeredAt = now,
                respondedAt = null
            )
            txn.set(handoffRef, handoff.toFirestoreMap(projectId))
            txn.set(eventsRef(projectId).document(), eventFirestoreMap(EventType.HANDOFF_OFFERED, taskId, taskTitle, fromUid, fromDisplayName))
            handoff
        }.await()
    }

    override suspend fun acceptHandoff(projectId: String, taskId: String, handoffId: String): Result<Task, DataError> =
        safeFirestoreCall {
            val taskRef = tasksRef(projectId).document(taskId)
            val handoffRef = handoffsRef(projectId, taskId).document(handoffId)
            firestore.runTransaction { txn ->
                val handoffSnap = txn.get(handoffRef)
                val status = handoffSnap.getString("status")
                if (status != HandoffStatus.OFFERED.name) {
                    throw FirebaseFirestoreException("Handoff already resolved", FirebaseFirestoreException.Code.FAILED_PRECONDITION)
                }
                val toUid = handoffSnap.getString("toUid")
                    ?: throw FirebaseFirestoreException("Handoff not found", FirebaseFirestoreException.Code.NOT_FOUND)
                val toDisplayName = handoffSnap.getString("toDisplayName") ?: toUid
                val now = System.currentTimeMillis()

                val taskSnap = txn.get(taskRef)
                val currentStatus = taskSnap.getString("status")?.let { runCatching { TaskStatus.valueOf(it) }.getOrNull() } ?: TaskStatus.TODO
                val timesHandedOver = ((taskSnap.getLong("timesHandedOver") ?: 0L) + 1).toInt()
                val newStatus = if (currentStatus == TaskStatus.TODO) TaskStatus.DOING else currentStatus
                val taskTitle = taskSnap.getString("title") ?: "Untitled task"

                txn.update(
                    handoffRef,
                    mapOf("status" to HandoffStatus.ACCEPTED.name, "respondedAt" to now)
                )
                txn.update(
                    taskRef,
                    mapOf(
                        "holderUid" to toUid,
                        "holderDisplayName" to toDisplayName,
                        "status" to newStatus.name,
                        "timesHandedOver" to timesHandedOver,
                        "updatedAt" to now
                    )
                )
                txn.set(eventsRef(projectId).document(), eventFirestoreMap(EventType.HANDOFF_ACCEPTED, taskId, taskTitle, toUid, toDisplayName))
                taskSnap.toTask(projectId)?.copy(
                    holderUid = toUid,
                    holderDisplayName = toDisplayName,
                    status = newStatus,
                    timesHandedOver = timesHandedOver,
                    updatedAt = now
                ) ?: throw FirebaseFirestoreException("Task not found", FirebaseFirestoreException.Code.NOT_FOUND)
            }.await()
        }

    override suspend fun declineHandoff(projectId: String, taskId: String, handoffId: String, reason: String): Result<Handoff, DataError> =
        safeFirestoreCall {
            val taskRef = tasksRef(projectId).document(taskId)
            val handoffRef = handoffsRef(projectId, taskId).document(handoffId)
            firestore.runTransaction { txn ->
                val handoffSnap = txn.get(handoffRef)
                val status = handoffSnap.getString("status")
                if (status != HandoffStatus.OFFERED.name) {
                    throw FirebaseFirestoreException("Handoff already resolved", FirebaseFirestoreException.Code.FAILED_PRECONDITION)
                }
                val toUid = handoffSnap.getString("toUid")
                    ?: throw FirebaseFirestoreException("Handoff not found", FirebaseFirestoreException.Code.NOT_FOUND)
                val toDisplayName = handoffSnap.getString("toDisplayName") ?: toUid
                val taskTitle = txn.get(taskRef).getString("title") ?: "Untitled task"
                val now = System.currentTimeMillis()
                txn.update(
                    handoffRef,
                    mapOf("status" to HandoffStatus.DECLINED.name, "declineReason" to reason, "respondedAt" to now)
                )
                txn.set(eventsRef(projectId).document(), eventFirestoreMap(EventType.HANDOFF_DECLINED, taskId, taskTitle, toUid, toDisplayName))
                handoffSnap.toHandoff(taskId)?.copy(status = HandoffStatus.DECLINED, declineReason = reason, respondedAt = now)
                    ?: throw FirebaseFirestoreException("Handoff not found", FirebaseFirestoreException.Code.NOT_FOUND)
            }.await()
        }

    override suspend fun markTaskDone(projectId: String, taskId: String, byUid: String, byDisplayName: String): Result<Task, DataError> =
        safeFirestoreCall {
            val taskRef = tasksRef(projectId).document(taskId)
            val now = System.currentTimeMillis()
            val taskTitle = taskRef.get().await().getString("title") ?: "Untitled task"
            firestore.batch()
                .update(taskRef, mapOf("status" to TaskStatus.DONE.name, "updatedAt" to now))
                .set(eventsRef(projectId).document(), eventFirestoreMap(EventType.TASK_MARKED_DONE, taskId, taskTitle, byUid, byDisplayName))
                .commit()
                .await()
            taskRef.get().await().toTask(projectId)
                ?: throw FirebaseFirestoreException("Task not found", FirebaseFirestoreException.Code.NOT_FOUND)
        }

    // Handoff subcollection docs under the deleted task are not cascade-deleted — same accepted gap
    // as roles/members cascade-delete (specs/office/progress.md, audit item #7): no bulk-delete-by-
    // parent feature exists yet, and an orphaned handoff can't surface anywhere a user would see it.
    override suspend fun deleteTask(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError> =
        safeFirestoreCall {
            val taskRef = tasksRef(projectId).document(taskId)
            val title = taskRef.get().await().getString("title") ?: "Untitled task"
            firestore.batch()
                .delete(taskRef)
                .set(eventsRef(projectId).document(), eventFirestoreMap(EventType.TASK_DELETED, taskId, title, byUid, byDisplayName))
                .commit()
                .await()
            Unit
        }

    override suspend fun getHandoffStatus(projectId: String, taskId: String, handoffId: String): Result<HandoffStatus?, DataError> =
        safeFirestoreCall { handoffsRef(projectId, taskId).document(handoffId).get().await().toHandoff(taskId)?.status }
}
