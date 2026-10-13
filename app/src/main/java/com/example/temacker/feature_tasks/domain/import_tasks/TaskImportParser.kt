package com.example.temacker.feature_tasks.domain.import_tasks

import com.example.temacker.core.domain.model.ProjectMember
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class ImportedTask(
    val title: String,
    val description: String?,
    val dueDate: Long?,
    // What the file said; null when it named nobody.
    val assigneeQuery: String?,
    // Matched member, or the importer when nobody matched (there are no unassigned tasks yet).
    val assignee: ProjectMember,
    val matched: Boolean
)

data class ImportPreview(val tasks: List<ImportedTask>, val problems: List<String>)

object TaskImportParser {
    const val MAX_TASKS = 100

    // {"tasks":[{"title","description","dueDate":"yyyy-MM-dd","assignee":"Member name"}]}
    fun parse(text: String, members: List<ProjectMember>, me: ProjectMember): ImportPreview {
        val root = try {
            Json.parseToJsonElement(text)
        } catch (e: Exception) {
            return ImportPreview(emptyList(), listOf("That file isn't valid JSON."))
        }
        val array = when (root) {
            is JsonArray -> root
            is JsonObject -> root["tasks"] as? JsonArray
            else -> null
        } ?: return ImportPreview(emptyList(), listOf("Expected a \"tasks\" list at the top of the file."))

        val problems = mutableListOf<String>()
        val tasks = mutableListOf<ImportedTask>()
        array.forEachIndexed { index, element ->
            val number = index + 1
            val obj = element as? JsonObject
            val title = obj?.string("title")?.trim().orEmpty()
            when {
                obj == null -> problems += "Task $number isn't an object."
                title.isBlank() -> problems += "Task $number has no title."
                tasks.size >= MAX_TASKS -> if (problems.none { it.startsWith("Only the first") }) problems += "Only the first $MAX_TASKS tasks are imported."
                else -> {
                    val due = obj.string("dueDate")?.let(::parseDate)
                    if (obj.string("dueDate") != null && due == null) problems += "Task $number: due date should look like 2026-10-31."
                    val query = obj.string("assignee")?.trim()?.takeIf { it.isNotEmpty() }
                    val match = query?.let { q -> members.firstOrNull { it.displayName.equals(q, ignoreCase = true) } }
                    tasks += ImportedTask(title, obj.string("description")?.trim()?.ifBlank { null }, due, query, match ?: me, match != null)
                }
            }
        }
        if (tasks.isEmpty() && problems.isEmpty()) problems += "The file has no tasks."
        return ImportPreview(tasks, problems)
    }

    private fun JsonObject.string(key: String): String? = (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    // UTC midnight, matching what the due-date picker stores.
    private fun parseDate(value: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC"); isLenient = false }.parse(value)?.time
    } catch (e: Exception) {
        null
    }
}
