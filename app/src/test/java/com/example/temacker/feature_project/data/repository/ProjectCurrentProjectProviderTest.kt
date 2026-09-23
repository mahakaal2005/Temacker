package com.example.temacker.feature_project.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.core.domain.model.ProjectSummary
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ProjectCurrentProjectProviderTest {

    private fun project(id: String) = Project(id, "Project $id", "owner", 0L)

    private fun membership(projectId: String, userId: String, roleName: String) = Membership(
        projectId, userId, "role1", roleName, RolePermissions.NONE, "Name", null, 0L, false
    )

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

    private class FakeMembershipRepository(
        private val members: Map<String, List<Membership>> = emptyMap(),
        private val myMembership: Map<String, Membership?> = emptyMap()
    ) : MembershipRepository {
        override fun observeMembers(projectId: String): Flow<Result<List<Membership>, DataError>> =
            MutableStateFlow(Result.Success(members[projectId].orEmpty()))
        override fun observeMembership(projectId: String): Flow<Result<Membership?, DataError>> =
            MutableStateFlow(Result.Success(myMembership[projectId]))
        override suspend fun joinProject(code: String, displayName: String, photoUrl: String?) = throw NotImplementedError()
        override suspend fun removeMember(projectId: String, userId: String) = throw NotImplementedError()
        override suspend fun reassignRole(projectId: String, userId: String, roleId: String, byUid: String, byDisplayName: String) =
            throw NotImplementedError()
    }

    @Test
    fun `selected id present in the list is kept as-is`() = runTest {
        val store = FakeSelectedProjectStore(initial = "p2")
        val repo = FakeProjectRepository(listOf(project("p1"), project("p2")))
        val provider = ProjectCurrentProjectProvider(repo, FakeMembershipRepository(), store)

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
        val provider = ProjectCurrentProjectProvider(repo, FakeMembershipRepository(), store)

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
        val provider = ProjectCurrentProjectProvider(repo, FakeMembershipRepository(), store)

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
        val provider = ProjectCurrentProjectProvider(repo, FakeMembershipRepository(), store)

        provider.observeCurrentProjectId().test {
            assertThat(awaitItem()).isEqualTo(Result.Error(DataError.Network.UNKNOWN))
        }
        assertThat(store.selectedId.value).isEqualTo("p1")
    }

    @Test
    fun `summary reflects the current project's name, member count and the viewer's role`() = runTest {
        val store = FakeSelectedProjectStore(initial = "p1")
        val repo = FakeProjectRepository(listOf(project("p1"), project("p2")))
        val membershipRepo = FakeMembershipRepository(
            members = mapOf("p1" to listOf(membership("p1", "u1", "Leader"), membership("p1", "u2", "Default"))),
            myMembership = mapOf("p1" to membership("p1", "u1", "Leader"))
        )
        val provider = ProjectCurrentProjectProvider(repo, membershipRepo, store)

        provider.observeCurrentProjectSummary().test {
            assertThat(awaitItem()).isEqualTo(
                Result.Success(ProjectSummary(name = "Project p1", memberCount = 2, roleName = "Leader", hasOtherProjects = true))
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `summary is null once the current project is no longer in the list`() = runTest {
        val store = FakeSelectedProjectStore(initial = "p1")
        val repo = FakeProjectRepository(emptyList())
        val provider = ProjectCurrentProjectProvider(repo, FakeMembershipRepository(), store)

        provider.observeCurrentProjectSummary().test {
            assertThat(awaitItem()).isEqualTo(Result.Success(null))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
