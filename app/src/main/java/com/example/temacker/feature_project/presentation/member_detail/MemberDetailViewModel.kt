package com.example.temacker.feature_project.presentation.member_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveRolesUseCase
import com.example.temacker.feature_project.domain.use_case.ReassignMemberRoleUseCase
import com.example.temacker.feature_project.domain.use_case.RemoveMemberUseCase
import com.example.temacker.feature_project.domain.use_case.SetMemberExtraPermissionsUseCase
import com.example.temacker.feature_project.domain.use_case.TransferLeadershipUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MemberDetailViewModel(
    private val userId: String,
    private val observeMembers: ObserveMembersUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val observeRoles: ObserveRolesUseCase,
    private val reassignMemberRole: ReassignMemberRoleUseCase,
    private val setMemberExtraPermissions: SetMemberExtraPermissionsUseCase,
    private val transferLeadership: TransferLeadershipUseCase,
    private val removeMember: RemoveMemberUseCase,
    private val currentProjectProvider: CurrentProjectProvider
) : ViewModel() {

    private val _state = MutableStateFlow(MemberDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<MemberDetailEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: String? = null

    init {
        val project = currentProjectProvider.observeCurrentProjectId()
            .mapNotNull { (it as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch { project.collect { projectId = it } }
        viewModelScope.launch {
            project.flatMapLatest { observeMembers(it) }.collect { result ->
                result
                    .onSuccess { members ->
                        val member = members.firstOrNull { it.userId == userId }
                        _state.update {
                            it.copy(
                                member = member,
                                isLoading = false,
                                error = if (member == null) UiText.DynamicString("This member is no longer in the project.") else null
                            )
                        }
                    }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            project.flatMapLatest { observeCurrentMembership(it) }.collect { result ->
                result.onSuccess { me ->
                    val p = me?.effectivePermissions
                    _state.update {
                        it.copy(
                            canManageRoles = p?.manageRoles == true,
                            canRemove = p?.removeMembers == true,
                            viewerIsLeader = me?.isLeader == true,
                            isSelf = me?.userId == userId
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            project.flatMapLatest { observeRoles(it) }.collect { result ->
                result.onSuccess { roles -> _state.update { it.copy(roles = roles) } }
            }
        }
    }

    fun onAction(action: MemberDetailAction) {
        when (action) {
            MemberDetailAction.OnBackClick -> viewModelScope.launch { _events.send(MemberDetailEvent.NavigateBack) }
            MemberDetailAction.OnToggleRole -> _state.update { it.copy(isRoleExpanded = !it.isRoleExpanded) }
            MemberDetailAction.OnChangeRoleClick -> _state.update { it.copy(isRolePickerVisible = true) }
            MemberDetailAction.OnDismissRolePicker -> _state.update { it.copy(isRolePickerVisible = false) }
            is MemberDetailAction.OnRoleSelected -> {
                _state.update { it.copy(isRolePickerVisible = false) }
                if (action.roleId == _state.value.member?.roleId) return
                run { reassignMemberRole(it, userId, action.roleId) }
            }
            is MemberDetailAction.OnExtrasChanged -> run { setMemberExtraPermissions(it, userId, action.extras) }
            MemberDetailAction.OnRemoveClick -> _state.update { it.copy(confirm = MemberConfirm.REMOVE) }
            MemberDetailAction.OnMakeLeaderClick -> _state.update { it.copy(confirm = MemberConfirm.MAKE_LEADER) }
            MemberDetailAction.OnDismissConfirm -> _state.update { it.copy(confirm = MemberConfirm.NONE) }
            MemberDetailAction.OnConfirmClick -> {
                val confirm = _state.value.confirm
                _state.update { it.copy(confirm = MemberConfirm.NONE) }
                when (confirm) {
                    MemberConfirm.REMOVE -> run(onSuccess = { _events.send(MemberDetailEvent.NavigateBack) }) { removeMember(it, userId) }
                    MemberConfirm.MAKE_LEADER -> run { transferLeadership(it, userId) }
                    MemberConfirm.NONE -> Unit
                }
            }
            MemberDetailAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun run(onSuccess: suspend () -> Unit = {}, block: suspend (String) -> Result<*, com.example.temacker.core.domain.util.DataError>) {
        val id = projectId ?: return
        viewModelScope.launch {
            when (val result = block(id)) {
                is Result.Success -> onSuccess()
                is Result.Error -> _state.update { it.copy(error = result.error.toUiText()) }
            }
        }
    }
}
