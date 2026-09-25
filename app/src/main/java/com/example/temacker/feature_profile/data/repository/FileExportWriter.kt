package com.example.temacker.feature_profile.data.repository

import android.content.Context
import android.util.Log
import com.example.temacker.core.domain.model.ExportHandoff
import com.example.temacker.core.domain.model.ExportTask
import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_profile.domain.model.ExportFormat
import com.example.temacker.feature_profile.domain.model.ProjectExportSnapshot
import com.example.temacker.feature_profile.domain.repository.ExportFileWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// One file per project, written to cacheDir/exports so FileProvider can share it. Builds JSON
// manually (buildJsonObject) rather than annotating the domain models with @Serializable, so
// export-format concerns stay out of core/domain (same reasoning as the ExportFileWriter interface
// itself).
class FileExportWriter(
    private val context: Context
) : ExportFileWriter {

    override suspend fun write(
        projectName: String,
        snapshot: ProjectExportSnapshot,
        format: ExportFormat
    ): Result<String, DataError> = withContext(Dispatchers.IO) {
        try {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
            val safeName = projectName.replace(Regex("[^A-Za-z0-9-]+"), "_")
            val extension = if (format == ExportFormat.JSON) "json" else "csv"
            val file = File(exportsDir, "$safeName-$timestamp.$extension")
            val content = when (format) {
                ExportFormat.JSON -> snapshot.toJson(projectName)
                ExportFormat.CSV -> snapshot.toCsv()
            }
            file.writeText(content)
            Result.Success(file.absolutePath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "write: failed to write export file", e)
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    private fun ProjectExportSnapshot.toJson(projectName: String): String {
        val json = buildJsonObject {
            put("project", projectName)
            putJsonArray("tasks") { export.tasks.forEach { add(it.toJsonObject()) } }
            putJsonArray("handoffs") { export.handoffs.forEach { add(it.toJsonObject()) } }
            putJsonArray("members") { members.forEach { add(it.toJsonObject()) } }
        }
        return prettyJson.encodeToString(JsonObject.serializer(), json)
    }

    private fun ProjectExportSnapshot.toCsv(): String = buildString {
        appendLine("Tasks")
        appendLine("id,title,status,holder,createdBy,createdAt,updatedAt")
        export.tasks.forEach {
            appendLine(csvRow(it.id, it.title, it.status, it.holderDisplayName, it.createdByDisplayName, it.createdAt, it.updatedAt))
        }
        appendLine()
        appendLine("Handoffs")
        appendLine("id,task,from,to,note,status,declineReason,offeredAt,respondedAt")
        export.handoffs.forEach {
            appendLine(csvRow(it.id, it.taskTitle, it.fromDisplayName, it.toDisplayName, it.note.orEmpty(), it.status, it.declineReason.orEmpty(), it.offeredAt, it.respondedAt ?: ""))
        }
        appendLine()
        appendLine("Members")
        appendLine("displayName,role")
        members.forEach { appendLine(csvRow(it.displayName, it.roleName)) }
    }

    private fun csvRow(vararg fields: Any) = fields.joinToString(",") { field ->
        val text = field.toString().replace("\"", "\"\"")
        "\"$text\""
    }

    private companion object {
        const val TAG = "FileExportWriter"
        val prettyJson = Json { prettyPrint = true }
    }
}

private fun ExportTask.toJsonObject() = buildJsonObject {
    put("id", id); put("title", title); put("status", status)
    put("holder", holderDisplayName); put("createdBy", createdByDisplayName)
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun ExportHandoff.toJsonObject() = buildJsonObject {
    put("id", id); put("task", taskTitle); put("from", fromDisplayName); put("to", toDisplayName)
    put("note", note); put("status", status); put("declineReason", declineReason)
    put("offeredAt", offeredAt); put("respondedAt", respondedAt)
}

private fun ProjectMember.toJsonObject() = buildJsonObject {
    put("displayName", displayName); put("role", roleName)
}

private fun JsonObjectBuilder.putJsonArray(
    key: String,
    builder: JsonArrayBuilder.() -> Unit
) = put(key, buildJsonArray(builder))
