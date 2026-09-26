package com.example.temacker.feature_project.presentation.team

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.components.SegmentedTabs
import com.example.temacker.core.presentation.components.SwitcherPill
import com.example.temacker.core.presentation.components.bottomBarContentPadding
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_project.presentation.load.LoadAction
import com.example.temacker.feature_project.presentation.load.LoadState
import com.example.temacker.feature_project.presentation.load.LoadTabContent
import com.example.temacker.feature_project.presentation.load.LoadViewModel
import com.example.temacker.feature_project.presentation.pulse.PulseAction
import com.example.temacker.feature_project.presentation.pulse.PulseState
import com.example.temacker.feature_project.presentation.pulse.PulseTabContent
import com.example.temacker.feature_project.presentation.pulse.PulseViewModel
import com.example.temacker.feature_project.presentation.roster.RosterAction
import com.example.temacker.feature_project.presentation.roster.RosterEvent
import com.example.temacker.feature_project.presentation.roster.RosterState
import com.example.temacker.feature_project.presentation.roster.RosterTabContent
import com.example.temacker.feature_project.presentation.roster.RosterViewModel
import com.example.temacker.feature_project.presentation.stuck.StuckAction
import com.example.temacker.feature_project.presentation.stuck.StuckState
import com.example.temacker.feature_project.presentation.stuck.StuckTabContent
import com.example.temacker.feature_project.presentation.stuck.StuckViewModel
import org.koin.androidx.compose.koinViewModel

enum class TeamTab(val label: String) {
    ROSTER("Roster"), LOAD("Load"), STUCK("Stuck"), PULSE("Pulse")
}

@Composable
fun TeamRoot(
    onNavigateToBoard: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToYou: () -> Unit,
    onNavigateToManageRoles: () -> Unit,
    onNavigateToRoleExplainer: (String) -> Unit,
    onNavigateToSuccession: () -> Unit,
    onNavigateToSwitchProject: () -> Unit,
    rosterViewModel: RosterViewModel = koinViewModel(),
    loadViewModel: LoadViewModel = koinViewModel(),
    stuckViewModel: StuckViewModel = koinViewModel(),
    pulseViewModel: PulseViewModel = koinViewModel()
) {
    val rosterState by rosterViewModel.state.collectAsStateWithLifecycle()
    val loadState by loadViewModel.state.collectAsStateWithLifecycle()
    val stuckState by stuckViewModel.state.collectAsStateWithLifecycle()
    val pulseState by pulseViewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    ObserveAsEvents(rosterViewModel.events) { event ->
        when (event) {
            RosterEvent.NavigateToManageRoles -> onNavigateToManageRoles()
            is RosterEvent.NavigateToRoleExplainer -> onNavigateToRoleExplainer(event.userId)
        }
    }

    TeamScreen(
        rosterState = rosterState,
        loadState = loadState,
        stuckState = stuckState,
        pulseState = pulseState,
        onRosterAction = { action ->
            if (action is RosterAction.OnCopyCodeClick) {
                rosterState.inviteCode?.let { clipboard.setText(AnnotatedString(it)) }
            }
            rosterViewModel.onAction(action)
        },
        onLoadAction = loadViewModel::onAction,
        onStuckAction = stuckViewModel::onAction,
        onPulseAction = pulseViewModel::onAction,
        onNavigateToBoard = onNavigateToBoard,
        onNavigateToInbox = onNavigateToInbox,
        onNavigateToYou = onNavigateToYou,
        onNavigateToSuccession = onNavigateToSuccession,
        onNavigateToSwitchProject = onNavigateToSwitchProject
    )
}

@Composable
fun TeamScreen(
    rosterState: RosterState,
    loadState: LoadState,
    stuckState: StuckState,
    pulseState: PulseState,
    onRosterAction: (RosterAction) -> Unit,
    onLoadAction: (LoadAction) -> Unit,
    onStuckAction: (StuckAction) -> Unit,
    onPulseAction: (PulseAction) -> Unit,
    onNavigateToBoard: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToYou: () -> Unit,
    onNavigateToSuccession: () -> Unit,
    onNavigateToSwitchProject: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(TeamTab.ROSTER) }

    AppScaffold(
        selected = AppDestination.TEAM,
        onSelect = { destination ->
            when (destination) {
                AppDestination.BOARD -> onNavigateToBoard()
                AppDestination.INBOX -> onNavigateToInbox()
                AppDestination.TEAM -> Unit
                AppDestination.YOU -> onNavigateToYou()
            }
        },
        header = {
            SwitcherPill(
                projectName = rosterState.projectName,
                metaLine = "${rosterState.members.size} members · ${if (rosterState.isLeader) "Leader" else "Member"}",
                hasOtherProjects = rosterState.hasOtherProjects,
                onClick = onNavigateToSwitchProject,
                trailing = {
                    // Leader-only affordance — SuccessionScreen re-checks isLeader itself and
                    // bounces back if it ever gets reached by anyone else (see plan §6). Moved from a
                    // bare icon into a labelled overflow item — nobody could guess what a lone
                    // sparkle icon did.
                    if (rosterState.isLeader) {
                        var menuOpen by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Team actions")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("Start new cycle") },
                                    leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
                                    onClick = { menuOpen = false; onNavigateToSuccession() }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottomBarContentPadding(padding))) {
            SegmentedTabs(
                items = TeamTab.entries,
                selected = selectedTab,
                onSelect = { selectedTab = it },
                label = { it.label },
                modifier = Modifier.padding(top = Spacing.xs)
            )

            when (selectedTab) {
                TeamTab.ROSTER -> RosterTabContent(state = rosterState, onAction = onRosterAction)
                TeamTab.LOAD -> LoadTabContent(state = loadState, onAction = onLoadAction)
                TeamTab.STUCK -> StuckTabContent(state = stuckState, onAction = onStuckAction)
                TeamTab.PULSE -> PulseTabContent(state = pulseState, onAction = onPulseAction)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamScreenPreview() {
    TemackerTheme {
        TeamScreen(
            rosterState = RosterState(isLoading = false, isLeader = true, projectName = "Aurora Launch", hasOtherProjects = true),
            loadState = LoadState(isLoading = false),
            stuckState = StuckState(isLoading = false),
            pulseState = PulseState(isLoading = false),
            onRosterAction = {},
            onLoadAction = {},
            onStuckAction = {},
            onPulseAction = {},
            onNavigateToBoard = {},
            onNavigateToInbox = {},
            onNavigateToYou = {},
            onNavigateToSuccession = {},
            onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamScreenNonLeaderPreview() {
    TemackerTheme {
        TeamScreen(
            rosterState = RosterState(isLoading = false, isLeader = false, projectName = "Aurora Launch", hasOtherProjects = false),
            loadState = LoadState(isLoading = false),
            stuckState = StuckState(isLoading = false),
            pulseState = PulseState(isLoading = false),
            onRosterAction = {},
            onLoadAction = {},
            onStuckAction = {},
            onPulseAction = {},
            onNavigateToBoard = {},
            onNavigateToInbox = {},
            onNavigateToYou = {},
            onNavigateToSuccession = {},
            onNavigateToSwitchProject = {}
        )
    }
}
