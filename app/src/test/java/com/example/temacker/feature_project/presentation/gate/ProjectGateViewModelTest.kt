package com.example.temacker.feature_project.presentation.gate

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.SyncUserProjectsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectGateViewModelTest {

    // cached = what Room holds now; onServer = what a successful sync writes into Room.
    private class FakeProjectRepository(
        cached: List<Project>,
        private val onServer: List<Project>?
    ) : ProjectRepository {
        val room = MutableStateFlow<Result<List<Project>, DataError>>(Result.Success(cached))
        var syncCalls = 0
        override fun observeUserProjects(): Flow<Result<List<Project>, DataError>> = room
        override suspend fun syncUserProjects(): EmptyResult<DataError> {
            syncCalls++
            val server = onServer ?: return Result.Error(DataError.Network.NO_INTERNET)
            room.value = Result.Success(server)
            return Result.Success(Unit)
        }
        override fun observeProject(projectId: String) = throw NotImplementedError()
        override suspend fun createProject(name: String, ownerDisplayName: String, ownerPhotoUrl: String?) =
            throw NotImplementedError()
        override suspend fun succeedProject(oldProjectId: String, newProjectName: String, leaderUid: String) =
            throw NotImplementedError()
    }

    private fun project(id: String) = Project(id, "Project $id", "owner", 0L)

    private fun gate(repo: ProjectRepository) =
        ProjectGateViewModel(ObserveUserProjectsUseCase(repo), SyncUserProjectsUseCase(repo))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fresh install with projects on the server goes to Home, not No-project`() = runTest {
        val repo = FakeProjectRepository(cached = emptyList(), onServer = listOf(project("p1")))
        gate(repo).events.test { assertThat(awaitItem()).isEqualTo(ProjectGateEvent.NavigateToHome) }
    }

    @Test
    fun `server confirming no projects goes to No-project`() = runTest {
        val repo = FakeProjectRepository(cached = emptyList(), onServer = emptyList())
        gate(repo).events.test { assertThat(awaitItem()).isEqualTo(ProjectGateEvent.NavigateToNoProject) }
    }

    @Test
    fun `cached projects go straight to Home without waiting on the server`() = runTest {
        val repo = FakeProjectRepository(cached = listOf(project("p1")), onServer = null)
        gate(repo).events.test { assertThat(awaitItem()).isEqualTo(ProjectGateEvent.NavigateToHome) }
        assertThat(repo.syncCalls).isEqualTo(0)
    }

    @Test
    fun `offline with an empty cache falls back to No-project`() = runTest {
        val repo = FakeProjectRepository(cached = emptyList(), onServer = null)
        gate(repo).events.test { assertThat(awaitItem()).isEqualTo(ProjectGateEvent.NavigateToNoProject) }
    }
}
