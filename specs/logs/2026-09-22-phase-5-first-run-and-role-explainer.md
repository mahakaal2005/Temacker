# 2026-09-22 — Phase 5: invited first run + role explainer

## Built
- Spec + architecture doc updated first (real-gating list, who-set-the-role data, join-flow-only first run, switcher deferred).
- Data: `Membership` gains nullable `roleSetByUid` / `roleSetByDisplayName` / `roleSetAt` (Room v7, `AutoMigration(6,7)`, `7.json`).
  The invite code Firestore doc gains `createdByUid` / `createdByDisplayName` (not in the domain model or Room); `joinProject` copies them onto the new member.
  Reassign stamps the acting user; the creator Leader stamps themselves; succession copies the stamp through.
  `ReassignMemberRoleUseCase` and `GenerateInviteCodeUseCase` read the caller's own membership for uid and name.
- Rules: member update must carry `roleSetByUid == auth.uid`; invite-code create must carry `createdByUid == auth.uid`. 92/92 rules tests pass. Deployed to `temacker-a0252` by the user.
- Screens (`feature_project/presentation/`): `invited_first_run/` (reached only from the join flow; Back also goes to the board) and `role_explainer/` (opened by tapping a member row on Roster). Shared pure logic in `role_copy/RoleCapabilities.kt`. 5 previews each.
- Tests: `RoleCapabilitiesTest` (7). 59 unit tests in total, build and lint clean.

## Verified on device (RZCWA28EAZF, Rudra and FAIQUA accounts)
- Fresh invite code accepted by the deployed rules; doc carries the creator.
- FAIQUA removed, signed in, joined with the new code: first-run screen shows "Rudra Sharma added you a moment ago", Default role, correct can-do and needs-a-role lists, "Ask Rudra Sharma, the Leader, for a role."
- Back from first run lands on the Board (read-only, no FAB); relaunch skips first run.
- Explainer: joiner shows "Set by Rudra Sharma · 22 Sept" and the read-only note (viewer lacks manageRoles); Leader shows all five marks, no note, no set-by (old doc); an old member shows no set-by line.
- Reassign FAIQUA to Editor: Firestore doc got the stamp, explainer showed Editor's single grant plus "Set by Rudra Sharma". Reassigned back to Default afterwards.
- Added `statusBarsPadding` to the first-run screen after seeing it sit close to the status bar.

## Accessibility follow-up (same day)
- Dumped the first-run and explainer screens' accessibility trees via `uiautomator`: each capability row was two
  separate stops (a check/lock icon's content-desc, then the label text), and the section headers ("WHAT YOU CAN
  DO NOW", "TEAM", etc.) were not marked as headings.
- Added `feature_project/presentation/role_copy/CapabilityRow.kt`: one composable, `Modifier.semantics(mergeDescendants
  = true)` merges each row into a single announcement ("Invite members, needs a role"); both screens now use it.
  Section titles get `Modifier.semantics { heading() }`. Roster's member-row `clickable` gets an `onClickLabel`
  ("See what <role> can do").
- Computed WCAG contrast ratios for the design system's text/icon colors against Paper/NeutralWash: the Teal check
  icon (`#2FBFA8` on `#FAF9F6`) was 2.18:1, below the 3:1 non-text minimum — changed the check icon to `TealInk`
  (`#0D7060`, 5.69:1). Every other pair already cleared its threshold.
- Re-verified on device: `uiautomator` dump on both screens confirms the merged content-desc strings; visual
  re-check of the darker check icon not done (previews only).
- Not done: an actual TalkBack pass with someone listening, and the full contrast/TalkBack audit the phase spec
  lists as a separate item.

## Known limits
- Old members and old invite codes show no "set by" line (no backfill).
- A joiner can write any inviter name on their own member doc; rules cannot read the code doc from there. Cosmetic.
- Rules and app build must go out together; an older app build's reassign and code generation are rejected.
- Not checked on device: partial-role first-run (covered by preview), TalkBack.
- Project switcher is deferred, needs its own spec (see phase-5 spec).
