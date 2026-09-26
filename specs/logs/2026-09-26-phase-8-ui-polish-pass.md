# 2026-09-26 — Phase 8 UI polish pass (PR branch)

Device-checked on RZCWA28EAZF. Build, lint and unit tests pass.

- Bottom bar: drawn once in MainActivity above the NavHost (no fade with screens); one amber pill slides between tabs; press scales instead of a rectangular ripple. SegmentedTabs ripple removed.
- Back stack: every tab switch pops to Board; setup flows (gate, create project, first run) clear the stack so Board is the base. Back from a tab goes to Board, Back from Board exits.
- Header: project switcher is a white bordered card with an up/down badge; "1 member" pluralised everywhere.
- Board: New task FAB hidden while the empty state shows its own CTA. Inbox empty state uses the shared EmptyState.
- Switch project, You, Team roster and Manage roles restyled as grouped cards. New shared parts: TmkMenu, TmkSwitch, SettingsParts.
- New task: due-date field opens the picker (readOnly field swallowed the wrapper's click), clear button, past dates blocked, themed dialog.

Not done: Phase 7 two-account flows are still not device-verified. Body/label text and buttons still use the monospace label style app-wide.
