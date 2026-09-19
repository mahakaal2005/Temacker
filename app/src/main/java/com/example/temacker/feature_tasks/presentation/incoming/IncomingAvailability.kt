package com.example.temacker.feature_tasks.presentation.incoming

import com.example.temacker.feature_tasks.domain.model.HandoffStatus

data class IncomingNotice(val title: String, val detail: String)

// Null while loading or while the offer is still open; otherwise says why there is nothing to act on.
fun IncomingState.unavailableNotice(): IncomingNotice? {
    if (isLoading || !isTrailLoaded) return null
    val handoff = handoff
    if (task == null || handoff == null) {
        return IncomingNotice("This handoff isn't available anymore", "It may have been answered or the task removed.")
    }
    return when (handoff.status) {
        HandoffStatus.OFFERED -> null
        HandoffStatus.ACCEPTED -> IncomingNotice("You've already accepted this", "It's on your board now.")
        HandoffStatus.DECLINED -> IncomingNotice("You declined this handoff", "It stayed with ${handoff.fromDisplayName}.")
    }
}
