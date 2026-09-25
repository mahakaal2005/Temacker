package com.example.temacker.feature_profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_project.domain.use_case.LeaveProjectUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Reads across feature_auth (identity) and feature_project (membership) — Profile is inherently
// a composite view, the same pragmatic exception CreateProjectViewModel/JoinProjectViewModel take.
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val leaveProject: LeaveProjectUseCase,
    private val currentProjectProvider: CurrentProjectProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()

    // Sign-out revokes the auth token while these Firestore listeners are still attached, so the
    // next snapshot they receive briefly fails as PERMISSION_DENIED — real, but not worth showing
    // the user on their way out the door.
    private var isSigningOut = false

    // Same idea while leaving: the just-left project's listeners lose permission before they're cancelled.
    // Cleared once the selected project changes.
    private var isLeaving = false

    init {
        viewModelScope.launch {
            authRepository.observeUser().collectLatest { user ->
                if (user == null) return@collectLatest
                _state.update { it.copy(displayName = user.displayName, email = user.email, photoUrl = user.photoUrl) }
            }
        }

        val projectId = currentProjectProvider.observeCurrentProjectId()
            .mapNotNull { (it as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.collect { id ->
                isLeaving = false
                _state.update { it.copy(projectId = id) }
            }
        }

        viewModelScope.launch {
            projectId.combine(observeUserProjects()) { id, projectsResult -> id to projectsResult }
                .collect { (id, projectsResult) ->
                    projectsResult
                        .onSuccess { projects ->
                            val project = projects.firstOrNull { it.id == id }
                            if (project == null) {
                                _state.update { it.copy(isLoading = false) }
                            } else {
                                _state.update {
                                    it.copy(projectName = project.name, hasOtherProjects = projects.size > 1, isLoading = false)
                                }
                            }
                        }
                        .onFailure { error -> if (!isSigningOut && !isLeaving) _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
                }
        }

        viewModelScope.launch {
            projectId.flatMapLatest { observeCurrentMembership(it) }.collect { membershipResult ->
                membershipResult
                    .onSuccess { membership ->
                        _state.update {
                            it.copy(
                                isLeader = membership?.isLeader == true,
                                roleName = membership?.roleName.orEmpty(),
                                memberSince = membership?.joinedAt?.let(::formatMonthYear).orEmpty()
                            )
                        }
                    }
                    .onFailure { error -> if (!isSigningOut && !isLeaving) _state.update { it.copy(error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeMembers(it) }.collect { membersResult ->
                membersResult.onSuccess { members -> _state.update { it.copy(memberCount = members.size) } }
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnSignOutClick -> viewModelScope.launch {
                isSigningOut = true
                authRepository.signOut()
                _events.send(ProfileEvent.NavigateToLogin)
            }
            ProfileAction.OnLeaveProjectClick -> _state.update { it.copy(isLeaveDialogVisible = true) }
            ProfileAction.OnLeaveProjectDismissed -> _state.update { it.copy(isLeaveDialogVisible = false) }
            ProfileAction.OnLeaveProjectConfirmed -> {
                val leftId = _state.value.projectId ?: return
                _state.update { it.copy(isLeaveDialogVisible = false) }
                viewModelScope.launch {
                    isLeaving = true
                    leaveProject(leftId)
                        .onSuccess {
                            val remaining = (observeUserProjects().first() as? Result.Success)?.data.orEmpty()
                            if (remaining.none { it.id != leftId }) _events.send(ProfileEvent.NavigateToProjectGate)
                        }
                        .onFailure { error ->
                            isLeaving = false
                            _state.update { it.copy(error = error.toUiText()) }
                        }
                }
            }
            ProfileAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun formatMonthYear(epochMillis: Long): String =
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(epochMillis))
}
