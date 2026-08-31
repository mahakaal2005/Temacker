# 2026-08-27 (cont'd) — Rebrand: Relay → Temacker everywhere

## Context
The design spec (specs/UI/relay-all-phases-android-concept.html) used "Relay" as the product
name throughout — an internal codename from earlier planning sessions. The actual app is
Temacker. User asked for a full, conflict-free rename, confirmed via two explicit questions
before any file was touched.

## Done (both confirmed by user before executing)
- Renamed `specs/UI/relay-all-phases-android-concept.html` →
  `specs/UI/temacker-all-phases-android-concept.html`; updated every reference to the old path
  (`CLAUDE.md`, `specs/office/implementation-plan.md`, design system file comments).
- Renamed `ic_relay_mark.xml` → `ic_temacker_mark.xml`; updated both usages
  (SplashScreen.kt, LoginScreen.kt).
- Replaced "Relay" with "Temacker" throughout the spec HTML's actual UI copy (title, masthead,
  splash/login/app-bar text, empty-state copy, notification rationale, sustainability copy) —
  18 occurrences. Left one historical provenance comment alone (references a predecessor
  filename that no longer exists in the repo — renaming it would misrepresent history).
- Example invite-code format `RLY-XXXX-XX` → `TMK-XXXX-XX` in the spec (not yet implemented in
  code — feature_project will build real invite codes later).
- Fixed the same two prose mentions in `implementation-plan.md` and
  `phase-3-notifications-offline.md`.
- Splash/Login screens now render "Temacker", not "Relay".

## Also confirmed this session
- Both Google and email/password sign-in stay as permanent options for now (Google backend
  issue from earlier — see `2026-08-27-email-auth-and-google-signin-retry.md` — is resolved
  well enough that the user considers it working; no further auth code changes made here).

## Next
- Phase 1 remaining: feature_project scaffold (Project, Role, Membership, InviteCode) — user
  indicated readiness to move to this next.
