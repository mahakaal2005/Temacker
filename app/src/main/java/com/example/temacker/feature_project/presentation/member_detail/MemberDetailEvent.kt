package com.example.temacker.feature_project.presentation.member_detail

sealed interface MemberDetailEvent {
    data object NavigateBack : MemberDetailEvent
}
