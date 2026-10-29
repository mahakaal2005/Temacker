package com.example.temacker.feature_tasks.domain.import_tasks

import com.example.temacker.core.domain.model.ProjectMember
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class TaskImportParserTest {
    private val me = ProjectMember("leader", "Lena Leader", null, "Leader", true, true)
    private val members = listOf(me, ProjectMember("u2", "Tomas Vidal", null, "Default", false, false))

    @Test fun `matches assignee by name ignoring case`() {
        val p = TaskImportParser.parse("""{"tasks":[{"title":"A","assignee":"tomas vidal"}]}""", members, me)
        assertThat(p.tasks.single().assignee.uid).isEqualTo("u2")
        assertThat(p.tasks.single().matched).isTrue()
    }

    @Test fun `unmatched assignee falls back to the importer`() {
        val p = TaskImportParser.parse("""{"tasks":[{"title":"A","assignee":"Nobody"}]}""", members, me)
        assertThat(p.tasks.single().assignee.uid).isEqualTo("leader")
        assertThat(p.tasks.single().matched).isFalse()
    }

    @Test fun `parses due date as UTC midnight and flags a bad one`() {
        val ok = TaskImportParser.parse("""[{"title":"A","dueDate":"2026-10-31"}]""", members, me)
        assertThat(ok.tasks.single().dueDate).isEqualTo(1793404800000L)
        val bad = TaskImportParser.parse("""[{"title":"A","dueDate":"31/10"}]""", members, me)
        assertThat(bad.problems.size).isEqualTo(1)
    }

    @Test fun `skips tasks without a title and reports them`() {
        val p = TaskImportParser.parse("""{"tasks":[{"title":" "},{"title":"Real"}]}""", members, me)
        assertThat(p.tasks.map { it.title }).isEqualTo(listOf("Real"))
        assertThat(p.problems.size).isEqualTo(1)
    }

    @Test fun `rejects invalid json`() {
        val p = TaskImportParser.parse("not json", members, me)
        assertThat(p.tasks.isEmpty()).isTrue()
        assertThat(p.problems.size).isEqualTo(1)
    }
}
