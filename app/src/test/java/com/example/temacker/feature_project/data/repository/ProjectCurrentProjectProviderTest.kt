package com.example.temacker.feature_project.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ProjectCurrentProjectProviderTest {

    private fun project(id: String) = Project(id, "Project $id", "owner", 0L)

    private class FakeSelectedProjectStore(initial: String?) : SelectedProjectStore {
        val selectedId = MutableStateFlow(initial)
        override fun observeSelectedProjectId(): Flow<String?> = selectedId
        override suspend fun setSelectedProjectId(id: String) {
            selectedId.value = id
        }
    }

    private class FakeProjectRepository(projects: List<Project>) : ProjectRepository {
        val projects = MutableStateFlow<Result<List<Project>, DataError>>(Result.Success(projects))
        override fun observeUserProjects(): Flow<Result<List<Project>, DataError>> = projects
        override fun observeProject(projectId: String) = throw NotImplementedError()
        override suspend fun createProject(name: String, ownerDisplayName: String, ownerPhotoUrl: String?) =
            throw NotImplementedError()
        override suspend fun succeedProject(oldProjectId: String, newProjectName: String, leaderUid: String) =
            throw NotImplementedError()
    }

    @Test
    fun `selected id present in the list is kept as-is`() = runTest {
        val store = FakeSelectedProjectStore(initial = "p2")
        val repo = FakeProjectRepository(listOf(project("p1"), project("p2")))
        val provider = ProjectCurrentProjectProvider(repo, store)

        provider.observeCurrentProjectId().test {
            assertThat(awaitItem()).isEqualTo(Result.Success("p2"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(store.selectedId.value).isEqualTo("p2")
    }

    @Test
    fun `no selection falls back to the first project and persists it`() = runTest {
        val store = FakeSelectedProjectStore(initial = null)
        val repo = FakeProjectRepository(listOf(project("p1"), project("p2")))
        val provider = ProjectCurrentProjectProvider(repo, store)

        provider.observeCurrentProjectId().test {
            assertThat(awaitItem()).isEqualTo(Result.Success("p1"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(store.selectedId.value).isEqualTo("p1")
    }

    @Test
    fun `stale selection not in the list falls back to the first project and persists it`() = runTest {
        val store = FakeSelectedProjectStore(initial = "gone")
        val repo = FakeProjectRepository(listOf(project("p1"), project("p2")))
        val provider = ProjectCurrentProjectProvider(repo, store)

        provider.observeCurrentProjectId().test {
            assertThat(awaitItem()).isEqualTo(Result.Success("p1"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(store.selectedId.value).isEqualTo("p1")
    }

    @Test
    fun `a project-list error passes through without touching the stored selection`() = runTest {
        val store = FakeSelectedProjectStore(initial = "p1")
        val repo = FakeProjectRepository(emptyList())
        repo.projects.value = Result.Error(DataError.Network.UNKNOWN)
        val provider = ProjectCurrentProjectProvider(repo, store)

        provider.observeCurrentProjectId().test {
            assertThat(awaitItem()).isEqualTo(Result.Error(DataError.Network.UNKNOWN))
        }
        assertThat(store.selectedId.value).isEqualTo("p1")
    }
}
