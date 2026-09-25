package com.example.temacker.feature_project.domain.use_case

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LeadershipAndLeaveUseCaseTest {

    private fun membership(userId: String, isLeader: Boolean) = Membership(
        "p1", userId, "role", if (isLeader) "Leader" else "Default", RolePermissions.NONE, "Name $userId", null, 0L, isLeader
    )

    private class FakeMembershipRepository(private val me: Membership?) : MembershipRepository {
        var transferCall: List<String>? = null
        var removeCall: Pair<String, String>? = null

        override fun observeMembers(projectId: String): Flow<Result<List<Membership>, DataError>> = MutableStateFlow(Result.Success(emptyList()))
        override fun observeMembership(projectId: String): Flow<Result<Membership?, DataError>> = MutableStateFlow(Result.Success(me))
        override suspend fun joinProject(code: String, displayName: String, photoUrl: String?) = throw NotImplementedError()
        override suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError> {
            removeCall = projectId to userId
            return Result.Success(Unit)
        }
        override suspend fun reassignRole(projectId: String, userId: String, roleId: String, byUid: String, byDisplayName: String) =
            throw NotImplementedError()
        override suspend fun transferLeadership(projectId: String, fromUid: String, fromDisplayName: String, toUid: String): EmptyResult<DataError> {
            transferCall = listOf(projectId, fromUid, fromDisplayName, toUid)
            return Result.Success(Unit)
        }
    }

    private class FakeSessionManager(private val uid: String?) : SessionManager {
        override fun isLoggedIn(): Flow<Boolean> = MutableStateFlow(uid != null)
        override suspend fun getUid(): String? = uid
        override suspend fun setSession(uid: String?) = Unit
    }

    @Test
    fun `the Leader can transfer leadership and it carries their own uid and name`() = runTest {
        val repo = FakeMembershipRepository(membership("leader", isLeader = true))
        val result = TransferLeadershipUseCase(repo)("p1", "member")
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(repo.transferCall).isEqualTo(listOf("p1", "leader", "Name leader", "member"))
    }

    @Test
    fun `a non-Leader cannot transfer leadership`() = runTest {
        val repo = FakeMembershipRepository(membership("member", isLeader = false))
        val result = TransferLeadershipUseCase(repo)("p1", "other")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.PERMISSION_DENIED))
        assertThat(repo.transferCall).isEqualTo(null)
    }

    @Test
    fun `the Leader cannot transfer leadership to themselves`() = runTest {
        val repo = FakeMembershipRepository(membership("leader", isLeader = true))
        val result = TransferLeadershipUseCase(repo)("p1", "leader")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.PERMISSION_DENIED))
        assertThat(repo.transferCall).isEqualTo(null)
    }

    @Test
    fun `no membership at all means transfer is refused`() = runTest {
        val result = TransferLeadershipUseCase(FakeMembershipRepository(null))("p1", "other")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.PERMISSION_DENIED))
    }

    @Test
    fun `leaving removes the signed-in user's own membership`() = runTest {
        val repo = FakeMembershipRepository(null)
        val result = LeaveProjectUseCase(repo, FakeSessionManager("me"))("p1")
        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(repo.removeCall).isEqualTo("p1" to "me")
    }

    @Test
    fun `leaving while signed out is unauthorized and removes nothing`() = runTest {
        val repo = FakeMembershipRepository(null)
        val result = LeaveProjectUseCase(repo, FakeSessionManager(null))("p1")
        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNAUTHORIZED))
        assertThat(repo.removeCall).isEqualTo(null)
    }
}
