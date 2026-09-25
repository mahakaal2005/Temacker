# Phase 8 — Look and feel redesign

Status: spec written 2026-09-25, awaiting approval. No code written yet.

The features stay the same. This phase changes only how the app looks, moves, sounds (haptics) and reads, so that a
new user's first five minutes feel deliberate. ViewModels, State, Actions, Events, use cases, repositories, Room,
Firestore and navigation routes are **not touched**. The one exception, which the user approved, is
`HandoffNotificationFactory`: its visuals change but none of its logic does.

## Decisions (user-approved 2026-09-25)

| Question | Decision |
|---|---|
| Bottom bar | **Floating animated pill** (replaces the spec's full-width `.navbar`; a spec deviation approved under rule 2) |
| Typeface | **Roboto + bundled Roboto Mono** (spec-faithful; font file in `res/font`, no Gradle dependency) |
| Motion | **Expressive springs only at hero moments**; calm, no-overshoot springs everywhere else; honours reduced motion |
| System notifications | **Restyle the visuals only** (large avatar, action icons, grouping by project) |

## Research this is based on

- **`ui-ux-pro-max` skill.** Its UX rules on these topics were adopted:
  - Motion: animate 1–2 key elements per view at most; respect reduced motion; ease-out when entering and ease-in when exiting; infinite animation only on loaders.
  - Touch: targets of at least 44–48dp with at least 8dp between them.
  - Content states: empty states always carry a message and an action; skeletons or feedback instead of blank screens.
  - Feedback: toasts auto-dismiss after 3–5s; haptics only on confirmations or important actions; confirm every success.
  - Accessibility: never convey state by colour alone.

  Its auto-generated design system suggested a red palette, Flat Design with no shadows, and Plus Jakarta Sans. That output was **rejected** because it contradicts the brand the spec fixes.
- **`android-compose-ui` skill.**
  - Animate below recomposition (`graphicsLayer`, offset lambdas, `Canvas`).
  - Use design-system components with slots.
  - Every screen gets realistic previews.
  - `contentDescription` on everything that isn't purely decorative.
- **Android haptics principles** (developer.android.com, *Haptics design principles*):
  - Less is more.
  - Use predefined constants (CONFIRM, REJECT, SEGMENT_TICK, LONG_PRESS, GESTURE_THRESHOLD).
  - The same interaction always gets the same haptic.
  - Keep haptics in sync with the visual.
  - Never use buzzy one-shot vibrations for touch.
- **Material 3 Expressive motion.**
  - Springs replace easing curves.
  - *Spatial* springs (position, size, shape) may overshoot. *Effects* springs (colour, alpha) must not.
  - The expressive, low-damping scheme is for hero moments; the standard scheme is for functional motion.
- **The spec** `specs/UI/temacker-all-phases-android-concept.html` remains the source of brand tokens, copy, and screen content.
- **The screen-by-screen critique** in `/root/.claude/plans/i-want-to-make-serialized-pancake.md` (summarised in §9).

## 1. Design principles for Temacker

1. **The holder is the headline.** Every task surface leads with who holds the baton, then what, then when.
2. **One header, one bottom bar, one primary action per screen.** No stacked title bars, and no competing filled buttons.
3. **Quiet by default, alive at the baton moments.** Motion and haptics concentrate on four moments:
   - offering a task,
   - accepting it,
   - declining it,
   - marking it done.

   Everything else is calm.
4. **A next step in every state.** Loading, empty, error, offline and "not allowed" states each say what's happening and offer the next action.
5. **Colour is never the only signal.** Every status carries an icon or a word as well as its colour.
6. **The thumb zone owns the actions.** Primary actions sit in the bottom third, and the top bar holds context (project, filters).

## 2. Foundation (`core/presentation/designsystem`)

- **Type (`Type.kt`):**
  - Define every M3 role in use: `headlineSmall`, `titleSmall`, `bodySmall`, `labelLarge` and `labelMedium` are currently missing.
  - Bundle `res/font/roboto_mono_medium.ttf` for `MonoFontFamily`.
  - Remove the 6 hard-coded `fontSize` call sites by adding `displaySmall` (the Login and Splash wordmark) and `brandTitle` roles.
- **Spacing (`Spacing.kt`, new):** a 4-pt scale — `xxs 4 · xs 8 · s 12 · m 16 · l 20 · xl 24 · xxl 32`. Screen gutter = 20.
- **Elevation (`Elevation.kt`, new):**
  - `e1` is for cards and `e3` for the floating bar, sheets and toasts. Both use the spec's navy-tinted shadows, drawn with `Modifier.dropShadow`/`shadow(ambientColor, spotColor = Navy900.copy(alpha))`. Verify the API against the pinned BOM before use (rule 11).
  - Add a `LineSoft` token.
- **Semantic colours:**
  - Add `onSurfaceMuted` (Ink500), `success` (Teal/TealInk/TealWash), `warning` (Amber family) and `danger` (Coral family) as an `AppColors` object.
  - Features stop importing `Ink500` directly. The 105 call sites migrate as each screen is touched.
- **Motion (`Motion.kt`, new):**
  - `spatialDefault = spring(dampingRatio = 0.9, stiffness = 380)`
  - `spatialExpressive = spring(dampingRatio = 0.6, stiffness = 380)`, for hero moments only
  - `effects = spring(dampingRatio = 1f, stiffness = 1500)`, with no overshoot
  - `enter = 220ms ease-out`, `exit = 160ms ease-in`
  - `LocalReducedMotion`, read from `Settings.Global.ANIMATOR_DURATION_SCALE == 0`. When true, springs snap and shimmer stops.
  - Note: the `android-compose-ui` skill says not to use custom CompositionLocals. The alternative is a `rememberReducedMotion()` function; use that.
- **Haptics (`Haptics.kt`, new):** a `rememberAppHaptics()` wrapper, so that the mapping in §5 is the only place haptic types are chosen. Confirm that the `HapticFeedbackType` constants (`Confirm`, `Reject`, `SegmentTick`, `ToggleOn`/`ToggleOff`, `LongPress`) exist in the pinned `compose-bom 2026.02.01` before relying on them, with `LongPress` as the fallback.
- **Icons:**
  - Use one set, Material Symbols **Rounded** (`Icons.Rounded.*`, `Icons.AutoMirrored.Rounded.*`). `material-icons-extended` is already a dependency.
  - Outlined when inactive, filled when active.
  - 24dp in bars and 20dp inline.
  - No text glyphs, no emoji.
  - The brand mark (`ic_temacker_mark`) appears only on Splash, Login, No project and in empty states.

## 3. Shared components (`core/presentation/components`, each with previews of every state)

| Component | Replaces | Notes |
|---|---|---|
| `TmkBottomBar` | `NavigationBar` in `AppScaffold` | See §4 |
| `ProjectHeader` | `SwitcherPill` + every per-tab `TopAppBar` | See §4 |
| `TmkSnackbarHost` / `TmkToast` | M3 snackbar, the red inline error rows | See §6 |
| `Avatar` | 6 hand-rolled initials circles | Correct initials ("Mei-Ling Chow" → "MC"); deterministic wash colour from the uid; sizes 28/36/40/56 |
| `TaskCard` | Board's private card | See §7 |
| `ListRow` | Inbox, Roster, Load, Stuck, Pulse, Queue, Handoff and Switch project rows | Leading slot (avatar or icon badge), headline, supporting text, trailing slot; min height 64dp; grouped inside rounded `InsetGroup` surfaces, not full-bleed dividers |
| `StatusChip` | "For you", "Queued", "Not sent", due and late pills | Icon plus word, so colour is never the only signal |
| `InfoStrip` | Sync, waiting-on-you and other-projects strips | The whole strip is tappable; it enters with `AnimatedVisibility(expandVertically + fadeIn)` |
| `EmptyState` | 8 ad-hoc empty columns | Illustration, title, body, a primary button and an optional text button |
| `SkeletonList` | Full-screen spinners on list screens | Shimmer drawn in `drawWithCache` with a `graphicsLayer` alpha; spinners remain only inside buttons |
| `TmkButton` (primary, secondary, destructive, text) | Mixed Button, OutlinedButton and TextButton usage | 56dp; a built-in loading state replaces the label with a small spinner, and the width is kept stable |
| `SegmentedTabs` | M3 `TabRow` on Board and Team | Pill track, a sliding white thumb (spatialDefault) and a bold count |
| `BatonTrail` | Task detail's dots | See §7 |
| `TmkSheet` | Full-screen Hand off, the reassign-role dialog | `ModalBottomSheet`, 28dp top radius, drag handle |
| `ConfirmDialog` | 6 `AlertDialog`s | 28dp radius; a destructive confirm is a filled coral button, and the cancel sits on the left |

## 4. Shell: top bar and bottom bar

**`ProjectHeader`**, the one header on all 4 tabs. It fixes the switcher that can't be found.
- Left side:
  - a 36dp rounded-square project tile (the project's initial, amber wash),
  - then the project name (`titleMedium`),
  - then "8 members · Leader" (mono label).

  The whole group sits inside a `surface` pill with a `Line` border and an **always visible** ▾ chevron. Its click label is "Switch or add a project".
- Right side: at most one contextual icon.
  - Board: a sync or queue icon with a dot when writes are queued.
  - Team (Leader only): an overflow menu with a labelled "Start new cycle" item.
- While the project loads, it shows a skeleton pill. The header is never missing.
- It has no tab title such as "Board" or "Inbox". The bottom bar already says where you are, which saves about 64dp.
- On scroll, a `LineSoft` hairline fades in underneath (an effects spring).

**`TmkBottomBar`**, the floating animated pill.
- **Size and position:** 64dp tall, 12dp from the side edges, and 12dp above `navigationBars` insets.
- **Surface:** White, with an `e3` shadow and a fully rounded shape.
- **Items:** 4 of them, each icon plus an always-visible label:
  - Board = `Rounded.Dashboard`
  - Inbox = `Rounded.Inbox`
  - Team = `Rounded.Groups`
  - You = `Rounded.Person`
- **Selection indicator:** one amber-30% pill that slides between items using `spatialExpressive`, drawn through an offset lambda so it doesn't cause recomposition. The icon switches from outlined to filled with a 150ms crossfade, and the label turns bold.
- **Inbox badge:** amber with a paper ring. It scales in with an overshoot when the count goes from 0 to 1+, and crossfades between numbers.
- **Haptics:** `SegmentTick` when you select a different tab, and nothing when you re-select the current one.
- **Content padding:** screens get `bottom = barHeight + 24dp` so the last card clears the bar.
- **Accessibility:** each item is 48dp+ wide and uses `Role.Tab`; the badge reads as "Inbox, 2 waiting".
- **Keyboard:** the bar hides while the IME is open.

**Board FAB:** an `ExtendedFloatingActionButton` ("＋ New task") that sits above the bar. It shrinks to an icon when the list scrolls and grows again at the top. It animates with `spatialDefault`.

## 5. Haptics map (the only haptics in the app)

| Moment | Type |
|---|---|
| Accept the baton, Send handoff, Mark done, Create task or project, Join project | `Confirm` |
| Send decline | `Confirm` (a decline is a valid answer, not an error) |
| Validation error, action failed, permission blocked | `Reject` |
| Bottom-bar tab change, segmented-tab change | `SegmentTick` |
| Permission switch in Manage roles | `ToggleOn` / `ToggleOff` |
| Copy invite code | `Confirm` |
| Long-press a task card (only if a menu is later added) | `LongPress` |

Nothing else vibrates: no scroll haptics and no haptic on plain navigation taps.

## 6. Toasts, errors and loading

- **`TmkToast`** is a custom `SnackbarHost` visual.
  - **Shape:** a Navy 900 surface (the spec's reserved dark accent, with high contrast on paper), 16dp radius and an `e3` shadow.
  - **Leading icon by kind:**
    - success = teal `CheckCircle`
    - info = `Info`
    - offline = `CloudOff`
    - error = coral `ErrorOutline`
  - **Action:** one amber action at most.
  - **Placement:** floats 12dp above the bottom bar (or above the nav inset on screens without the bar).
  - **Motion:** it slides up and fades with `spatialDefault`, and swipes away sideways.
  - **Duration:** 4s by default and 8s when it offers Undo. It never persists.
- **Rules:**
  - A failed *action* → toast plus a `Reject` haptic.
  - A failed *load* → an in-place error state with Retry, in the same slot as the empty state.
  - A successful baton moment → toast plus a `Confirm` haptic, e.g. "Handed to Daniel. It stays yours until he accepts."
  - Implemented in the UI only: `LaunchedEffect(state.error)` shows the toast and calls the existing `On…ErrorDismissed` action on dismissal. There is no ViewModel change.
- **Loading:** list screens show `SkeletonList` shaped like their real rows. Buttons show their own loading state. A full-screen spinner survives only on Splash and the Project gate.

## 7. Key surfaces

**`TaskCard`** (Board)
- Row 1: `Avatar 36` (the holder), the title (`titleMedium`, max 2 lines), and a trailing `StatusChip` — one of:
  - For you
  - Queued
  - Not sent
  - Due Fri
  - 2 days late (coral, with a clock icon)
- Row 2: a mono meta line, formatted by a pure presentation formatter from existing fields (`offeredAt`, `updatedAt`, `dueDate`):
  - an offered card: "Daniel Osei offered this · 2h"
  - otherwise: "Held by you · since 19 Aug"
- Offered cards get an amber-wash background and an amber border. Other cards are white with an `e1` shadow.
- Press state: scale 0.98 through `graphicsLayer` with `spatialDefault`.
- Lists use `Modifier.animateItem()` so cards glide between tabs and filters.
- For TalkBack, each card merges into one sentence: "Sponsor deck, final pass. Held by Mei-Ling Chow. Due Friday. Offered to you."

**Board layout**, top to bottom:
1. `ProjectHeader`
2. the waiting-on-you `InfoStrip`, above the tabs as the spec says; when its filter is on, it shows a "Show all" chip
3. `SegmentedTabs` To do · Doing · Done with bold counts
4. sync strip (only when relevant)
5. the list

**`BatonTrail`** (Task detail)
- A vertical 2dp `LineSoft` connector drawn in `Canvas`.
- Nodes:
  - amber = offered
  - teal = accepted
  - hollow = still open
  - coral outline = declined, with the reason shown in a quote block
- Each entry shows a date and "held N days".
- The newest node pulses once on entry (expressive; skipped when reduced motion is on).

**Task detail layout:**
- A scrollable body:
  1. the holder hero card
  2. description
  3. due date
  4. the trail
- A pinned bottom action bar:
  - Hand off (primary)
  - Mark done (secondary, promoted out of ⋮)
- ⋮ keeps Hand back and Delete.

**Hand off:** becomes a `TmkSheet`, per the spec.
- Member rows show `Avatar`, name, role and "4 batons". Load comes from the existing state, if present; otherwise it's omitted.
- The note field sits above a sheet-pinned "Send handoff" button that respects `imePadding`.

**Incoming (accept or decline)**, the hero moment:
- It enters with a staggered fade and rise: avatar → headline → note → card. Stagger is 40ms, used once.
- On Accept:
  - `Confirm` haptic,
  - the button morphs into a teal check (`spatialExpressive`),
  - a short baton-arc animation (a 600ms Lottie-free `Canvas` path sweep from the sender's avatar to yours),
  - then the existing navigation event continues.
- **Guard:** the animation must never delay or block navigation. It plays while the ViewModel works, and it's skipped under reduced motion.

## 8. Empty states (copy follows the spec's voice; every one has a next step)

| Screen and case | Title / body | Primary / secondary |
|---|---|---|
| Board — To do or Doing empty, can create | "Nothing to hand over yet" / spec copy | Add the first task |
| Board — can't create | "No tasks here yet" / "Tasks you're handed show up here. Ask {leader} for a role that can create them." | See what your role can do |
| Board — Done empty | "Nothing finished yet" / "Tasks land here when their holder marks them done." | — |
| Board — waiting filter empty | "You're all caught up" / "Nobody's waiting on you right now." | Show all tasks |
| Inbox | "Nothing waiting on you" / spec copy | Turn on notifications (only when they're off) |
| Roster — only you | "It's just you so far" / "Share the invite code, and teammates can join in seconds." Shows the code inline. | Copy invite code / Share |
| Hand off — nobody to hand to | "No one to hand this to yet" / "Invite a teammate first, so there's someone to accept it." | Invite a teammate (opens the Team tab) |
| Load | "Nobody's holding anything" / "When tasks are created, each holder's load shows here." | — |
| Stuck | Teal check illustration: "Every offer has an answer" / "Offers unanswered for 18h+ show up here." | — |
| Pulse | "It's quiet so far" / "Created, offered, accepted and done events will appear here." | — |
| Manage roles — Leader only | "Only the Leader role exists" / "Create a role to let members invite, assign or delete." | Create a role |
| Incoming — stale | the existing notice title and detail | Back to Inbox |
| Any load error | "Couldn't load this" / the error text | Retry (re-dispatches the screen's existing load or refresh action where one exists; otherwise Back) |

Illustrations are 3 vector drawables in `res/drawable`, all derived from the Temacker mark:
- a baton with a dashed receiving arc (empty),
- a check in a teal ring (done or caught up),
- a cloud with a slash (offline or error).

## 9. Screen-by-screen changes (presentation only)

- **Splash:**
  - Keep navy.
  - The mark scales from 0.92 to 1 with a fade (the one hero moment).
  - "Checking your session…" appears after 600ms.
- **Login:**
  - Make it scrollable with `imePadding`.
  - The brand block staggers in once.
  - Fields get IME Next and Done actions.
  - Errors show as an inline field error, not text at the bottom.
  - Remove the "No password to remember" line that contradicts the form.
  - Google becomes the first option and email the second (the spec calls Google the primary route).
- **No project:** the Lock icon is replaced by the empty-state baton illustration, and the CTAs become `TmkButton`s.
- **Create project and Join project:**
  - Make them scrollable with `imePadding`, and pin the primary button above the IME.
  - Join shows the code in mono uppercase, with a Paste trailing icon; the display formats as TMK-XXXX-XX through a `VisualTransformation`, and the stored value stays unchanged.
- **Invited first run:** the capability rows get icons and the allowed rows animate in with a stagger.
- **Board:** see §4, §7 and §8.
- **Inbox:**
  - Rows become `ListRow`: avatar, sentence, meta line, a status icon (offer, accepted, declined, unanswered) and a chevron.
  - Section headers are mono labels.
  - Skeleton while loading.
- **Task detail:** see §7.
- **Hand off:** see §7.
- **Incoming:** see §7.
- **Decline:**
  - The header shows the task being declined, via the existing state.
  - Reason chips become `FilterChip`s.
  - Make it scrollable with `imePadding`.
  - Send stays disabled while the reason is blank, but only if the ViewModel already guards that. If it doesn't, flag it and don't change it.
- **New task:**
  - The details field grows automatically.
  - The date field opens on a tap anywhere on it.
  - The advisory becomes an `InfoStrip`.
  - The button shows its own loading state.
- **Queue:** `ListRow` plus `StatusChip`. Retry and Discard become a trailing text button and an icon button. The offline note becomes an `InfoStrip`.
- **Team:**
  - `SegmentedTabs` (Roster · Load · Stuck · Pulse); the selection uses `rememberSaveable`.
  - Leader actions move into the header overflow.
  - **Roster:** an "Invite" tonal button on the right of the header row; "Manage roles" becomes a `ListRow` at the top of the list; member rows show the role chip plus a chevron hint.
  - **Load:** each row gets a horizontal bar showing the share of active tasks, drawn in `Canvas`.
  - **Stuck:** the hours show as an amber-to-coral chip, with coral at 48h+.
  - **Pulse:** each event gets an event-type icon badge, and events are grouped under day headers.
- **Manage roles and Role explainer:**
  - Permission groups use `InsetGroup` cards, and switches fire the toggle haptics.
  - Reassigning a role becomes a `TmkSheet` with radio rows.
- **Succession:** a stepper header (1 Name → 2 Confirm), and the destructive confirm uses `TmkButton(destructive)`.
- **You (Profile):**
  - Make it scrollable.
  - An identity header (`Avatar 56`, name, email).
  - A "Project" `InsetGroup`:
    - Current project ▸ (opens the switcher),
    - Role,
    - Member since.
  - A "Settings" group:
    - Notifications ▸ (opens the rationale screen or system settings through an existing route),
    - Your data ▸,
    - Plan & limits ▸.
  - Sign out becomes a neutral text button. Leave project stays the only coral item.
- **Your data:** "Delete my account" moves into a separate "Danger zone" group at the bottom, and Export stays pinned as the primary action.
- **Plan & limits:** the meters animate from 0 to their value once, with an effects spring.
- **Notification rationale:** the three rows animate in with a stagger, and the primary button gets the `Confirm` haptic.
- **Everywhere:**
  - Back arrows use `Icons.AutoMirrored.Rounded.ArrowBack`.
  - Bottom CTAs get `navigationBarsPadding()`.
  - Screens use the `Spacing` tokens and a 20dp gutter.

## 10. System notifications (visuals only, `HandoffNotificationFactory`)

- Large icon = the sender's initials, rendered on a wash-coloured circle bitmap, using the same algorithm as `Avatar`.
- Actions get icons: a check for Accept and a close icon for Decline.
- `setGroup(projectId)` plus a summary notification per project, so several offers stack neatly.
- The "still waiting" nudge uses `Coral`/`CoralInk` and the `Schedule` small-icon variant (a new `ic_stat_waiting.xml`). Offers keep amber.
- `setSubText(projectName)` is kept (Phase 7 Group B). Channel IDs and importance are unchanged. The intents, payload parsing and action wiring are **not touched**.

## Out of scope

- New features.
- Swipe-to-accept on cards: it would need a new action, and the ux rules warn against horizontal-swipe conflicts.
- Inline Accept on Board or Inbox rows: it would need new ViewModel actions.
- Dark theme.
- Tablet layouts.
- Onboarding coach marks: they would need persisted state.

## Build order (log each group in `specs/logs/`)

- **A. Foundation:** Type, Spacing, Elevation, Motion, Haptics, the icon swap, and the Roboto Mono font.
- **B. Shell:** `ProjectHeader`, `TmkBottomBar`, `TmkToast`, and the Board FAB. This alone fixes the "can't find project switching" complaint.
- **C. Components:** `Avatar`, `ListRow`, `StatusChip`, `InfoStrip`, `EmptyState`, `SkeletonList`, `TmkButton`, `SegmentedTabs`, `TmkSheet` and `ConfirmDialog`.
- **D. Tasks feature:** Board, Task detail and `BatonTrail`, Hand off (sheet), Incoming (hero), Decline, New task, and Queue.
- **E. Team, Project, Profile, Auth and onboarding screens.**
- **F. System notifications.**

## Verification

- This cloud container has **no Android SDK**. `./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest`
  must run on the user's machine, or an SDK must be installed through the environment setup script before code
  groups are marked done (rule 12).
- Every new component and changed screen has `@Preview`s for loading, empty, error, content and offline states.
- Existing ViewModel unit tests must still pass unchanged. That proves no business logic moved.
- Device pass on RZCWA28EAZF:
  - font scale 200%,
  - TalkBack,
  - animations off (reduced motion),
  - gesture navigation,
  - keyboard open on every form.
