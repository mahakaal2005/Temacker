# 2026-09-25 — Phase 8 Group E: Onboarding (No project, Create project, Join project, Invited first run)

## No project
Lock icon (read as "forbidden") replaced with a baton-handoff illustration (`SwapHoriz` on an amber wash
circle, matching `EmptyState`'s visual language). Both CTAs are now `TmkButton`s (primary "Create a project",
secondary "I have an invite code") instead of a raw `Button`/`OutlinedButton` pair.

## Create project / Join project
Both are now scrollable with the primary button pinned below the scroll area, wrapped in
`imePadding()`/`navigationBarsPadding()` so it rides above the keyboard instead of the keyboard covering it.
Back arrows switched to `Icons.AutoMirrored.Rounded.ArrowBack` (and `Icons.Default.*` → `Icons.Rounded.*` for
the other icons on Create project, matching the rest of the redesign).

Join project's code field now shows a Paste trailing icon (`LocalClipboardManager`) and a
`VisualTransformation` that renders the field as `TMK-XXXX-XX` regardless of whether the underlying value
already has dashes — it strips punctuation first, then reinserts dashes by position, so a pasted
already-dashed code (the real format `GenerateInviteCodeUseCase` produces: `"TMK-${4 chars}-${2 chars}"`)
and a hand-typed undashed one both render correctly. `state.code` itself is never touched by the
transformation — matches the spec's "the stored value stays unchanged."

**Flagging, not fixing (out of scope):** if a user hand-types the 9 characters with no dashes at all,
`state.code` stays undashed, and the join call would not match the backend's dashed invite-code format. This
is a pre-existing `JoinProjectViewModel` gap (it never inserted dashes itself), not something introduced by
this display change — fixing it would mean touching ViewModel logic, out of this phase's presentation-only
scope. Noting it here per CLAUDE.md rule 14 instead of fixing it silently.

## Invited first run
The "what you can do now" capability rows (already had check/lock icons from Group C's original
`CapabilityRow`) now stagger in with a short per-row fade (45ms apart, `tween(220)`, skipped under reduced
motion) via a small `StaggerIn` wrapper. The locked "needs a role" rows stay static on purpose — the
celebratory half of the screen is the one that should draw the eye.

## Verified
Manual read-through only — no Android SDK here. No ViewModel/State/Action/Event changes in any of the four
screens.

## Next
Team (SegmentedTabs, Roster/Load/Stuck/Pulse), Manage roles, Role explainer, Succession.
