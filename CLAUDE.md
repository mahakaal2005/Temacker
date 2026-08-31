# CLAUDE.md — Innogeeks App

Native Android (Kotlin, Jetpack Compose + Material 3) club app. Read this first, every session.

## Where things live

| What | Where |
|---|---|
| Architecture spec (source of truth — read before writing any code) | `specs/ultimate_android_architecture.md` |
| Skills (read the matching `SKILL.md` before touching that concern) | `.claude/skills/` |
| Product Requirements | `docs/PRD.md` |
| User flows | `docs/10_USER_FLOWS.md` |
| Phase breakdowns (written before work starts) | `specs/office/` |
| **Progress tracker (read FIRST, every session)** | `specs/office/progress.md` |
| Session dev journal (updated every session) | `specs/logs/` |

## Rules

0. Start every session by reading `specs/office/progress.md` — it's the current done/left status across all phases and says which phase to pick up.
1. Before coding on a concern, read its `SKILL.md` in `.claude/skills/`.
2. Never deviate from `specs/ultimate_android_architecture.md` without listing the proposed changes and getting explicit approval first.
3. Before starting a new feature, confirm its phase spec in `specs/office/` is written and agreed.
4. At end of session (or after finishing a task), log the session in `specs/logs/`, tick off completed items in `specs/office/progress.md`, and mark completed phases in their `specs/office/phase-N-*.md` file.
5. Whenever a session includes mentor/teaching Q&A (see `.claude/rules/senior-mentor.md`) — the user explaining their understanding of code and getting corrected or confirmed — log each concept covered to the matching topic file under `learning/` (e.g. `learning/navigation.md`, `learning/di-koin.md`): what they asked, what they already had right, what was corrected/clarified, and the key takeaway. See `learning/README.md` for the category list. Don't let one file grow unbounded — once a topic file passes roughly 300 lines, split the most self-contained section(s) into a new, more specific file and add it to the index. This is separate from `specs/logs/` (which tracks work done, not what was learned). A `.claude/hooks/` Stop hook checks that a teaching turn produced a note somewhere in `learning/`.
6. Don't grep/search across modules to locate a file. If the path isn't obvious from `specs/ultimate_android_architecture.md` or context, ask instead of exploring.
7. Read only the file(s) directly relevant to the current task. Don't open adjacent files "just in case."
8. Don't re-read a file already in this session's context unless it's been edited since.
9. Match existing naming, state, and MVI patterns exactly (see architecture spec). Propose deviations per rule 2, never introduce silently.
10. Before adding a new dependency (Gradle), check if an existing one already covers it. Flag the addition and get approval before running the sync.
11. Never invent or assume a Compose/Koin/Room API. If unsure an API exists or hasn't changed, say so rather than generating a plausible-looking call.
12. After any code change, run the relevant `./gradlew` build/lint/test command before marking a task done. Don't declare something finished on the basis of it "looking correct."
13. Keep new files under the project's line-length convention (check architecture spec). If a file is growing past it, propose a split rather than letting it balloon.
14. Don't touch files outside the current phase's scope (per `specs/office/phase-N-*.md`) without flagging it first, even if you notice an unrelated bug or improvement — note it instead of fixing it inline.
15. When a teaching/mentor turn happens, write the `learning/` note before ending the turn, not deferred to "end of session" catch-up.
16. If `specs/office/progress.md` and the actual code state disagree (e.g. a phase marked incomplete but the code shows it done), flag the mismatch, don't silently trust one over the other.

## Comment Style (STRICT — never violate)

Match the style of `SplashViewModel.kt` and `LoginScreen.kt` exactly:
- Short inline comments only. One sentence max.
- Place the comment on the same line as the code, or on the line directly above it.
- No KDoc (`/** */`) blocks unless the function is a public API consumed by other teams.
- No decorative separators (no `// ─────────`, no `// ====`, no `// ----`).
- No block-header comments explaining "WHY THIS FILE EXISTS".
- No multi-line paragraph comments.
- Good: `// popUpTo removes Splash so Back never returns here.`
- Bad: A 10-line block explaining the entire philosophy of navigation.

## UI Requirements
- Always write `@Preview` for all components, screens, and UI elements.
- Follow `specs/UI/temacker-all-phases-android-concept.html` — the actual design spec (brand tokens, screen-by-screen layout, copy) for every screen. Read the relevant phase section before building or restyling a screen; don't invent colors, type, or layout from scratch.
- The app is primarily **light mode** (warm paper/cream surfaces, per the spec's `--paper`/`--stone` tokens), with a dark navy treatment reserved for specific moments the spec calls out explicitly (splash, lock screen/notifications in Phase 3). Previews should use `showBackground = true` for light-mode screens; use `@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)` only for the screens the spec itself renders dark.
- Include previews for all possible states (e.g., loading, success, error, different selections).
