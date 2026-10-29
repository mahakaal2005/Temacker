# 2026-09-26 — Phase 9: task assignment, JSON import, requests, extra permissions

Built per `specs/office/phase-9-task-assignment.md`.

- A: `importTasks` and `approveTaskRequests` flags mirrored everywhere; per-member `extraPermissions` (add-only, never deleteProject) with a Roster "Extra permissions" sheet; `Membership.effectivePermissions`; Room v8.
- B: assignee picker on New task; Leader "Reassign task" on Task detail; `TASK_REASSIGNED` event; createdByUid split from holder.
- C: Board menu "Import tasks" opens a JSON file picker, preview, then creates tasks. Format `{"tasks":[{"title","description","dueDate","assignee"}]}`.
- D: Handoff `kind` (OFFER, DROP_REQUEST, TRANSFER_REQUEST) and target fields, Room v9; holders who can't approve ask the Leader; Inbox and Incoming reworded for requests; push wording by kind.
- Verified: assembleDebug, lintDebug, testDebugUnitTest, `npm run test:rules` (128), functions Jest (17).
- Not done: deploy of rules/functions, device verification, "assigned to you" push, email matching, stale role explainer rebuild (done: member page).

## Member page (replaces "What <role> can do")
- Tapping a roster member opens `member_detail/MemberDetailScreen`:
  - Profile card with role chip, "+ N extra" and a "Can do N of 9" summary.
  - Collapsible role card listing all 9 permissions, with Change role inside.
  - Extra-permission toggles, editable only with Change roles and never on yourself or the Leader.
  - "Everyone can" list, then Make Leader and Remove.
- `role_copy/PermissionInfo` is now the single permission catalog, used by Manage roles, the extras sheet, the member page and first run. The stale 5-item `Capability` list is gone.
- Verified: assembleDebug, lintDebug, testDebugUnitTest. Device testing done by the user.

## App logo and splash
- Launcher icon is now the Temacker mark (adaptive + monochrome + legacy webp drawn with PIL).
- core-splashscreen 1.0.1 added: navy launch window with the mark, handing over to the Compose splash.
- Compose splash: mark stays visible, name and tagline rise in, glow pulses; SplashViewModel holds it for at least 1.4s.
