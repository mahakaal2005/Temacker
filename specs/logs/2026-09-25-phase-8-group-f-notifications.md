# 2026-09-25 — Phase 8 Group F: system notification visuals (Group F complete, Phase 8 complete)

## Context
The one named exception in this phase: `HandoffNotificationFactory` may have its visuals touched, as long as
intents, payload parsing, and action wiring stay untouched. Channel IDs and importance are unchanged.

## Changes

**`HandoffNotificationFactory.kt`:**
- Large icon: a 128px circular bitmap drawn with `android.graphics.Canvas`/`Paint`, filled with the same
  initials `Avatar` uses in-app (`initialsOf()`, imported from `core/presentation/components/Avatar.kt` — a
  plain non-`@Composable` function, safe to call from this data-layer file) on a wash-colored circle. Colors
  are hardcoded ARGB ints mirroring `TealWash`/`TealInk` (Avatar's default NEUTRAL tone) rather than
  importing Compose `Color` into a background-notification file.
- Small icon + accent color now vary by push type: `NUDGE_OFFERER`/`NUDGE_RECIPIENT` ("still waiting") use
  a new `ic_stat_waiting.xml` (clock glyph) + coral; every other type keeps the existing amber baton icon.
- Accept/Decline actions get real icons (`ic_check_small.xml`, `ic_close_small.xml`) instead of `0`
  (no icon).
- `setGroup(projectId)` on every push, plus a second `notify()` call per push posting a
  `setGroupSummary(true)` notification tagged by `projectId` (id `2`, distinct from individual pushes' id
  `1`, so tag/id pairs never collide) — several offers for the same project now stack under one summary
  instead of flooding the shade individually.
- `setSubText(projectName)` kept exactly as before (Phase 7 Group B). Intents, `parseHandoffPush`'s field
  extraction, and `HandoffActionReceiver`'s action wiring are untouched.

**`HandoffPush.kt` (same directory, sibling to the factory) — one additive field:**
- Added `actorName: String = ""`, populated in `parseHandoffPush()` from the same `from`/`to` local
  variables the existing `title`/`text` copy already derives (`from` for OFFERED/NUDGE_RECIPIENT, `to` for
  ACCEPTED/DECLINED/NUDGE_OFFERER — whichever person the notification's large icon should represent). This
  was necessary because `HandoffPush` only stored the already-formatted `title`/`text` strings, not the raw
  name needed to compute initials — there was no way to build the spec's "large icon = sender's initials"
  requirement without it. Purely additive (default `""`, no existing call site touched besides the one
  `return` statement), doesn't change parsing behavior, payload shape, or any consumer's logic — flagging
  per CLAUDE.md rule 14 since it's technically outside `HandoffNotificationFactory.kt` itself, but it's the
  minimal change inside the same "system notification visuals" unit needed to satisfy the spec.

**New drawables:** `ic_stat_waiting.xml`, `ic_check_small.xml`, `ic_close_small.xml` — simple white-stroke
vector drawables matching `ic_stat_baton.xml`'s existing style (48x48 viewport, Android auto-tints status
bar/action icons).

## Verified
Manual read-through only — no Android SDK here, and notification rendering can't be visually verified
without a device besides. Flagged for the user's real device check once buildable: the large-icon bitmap
sizing/positioning and the group-summary collapsing behavior.

## Group F — complete. Phase 8 (look-and-feel redesign) — all 6 groups (A–F) now coded.
Nothing in Groups A–F has been build-verified in this container (no Android SDK). The user should run
`./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest` locally before merging PR #1, and do a
device pass comparing screens against `specs/UI/temacker-all-phases-android-concept.html` and this phase's
own spec (`specs/office/phase-8-ui-redesign.md`).
