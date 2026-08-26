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
- The app is strictly dark mode. All previews MUST use `@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)`. Never use `showBackground = true` (which implies light mode defaults).
- Include previews for all possible states (e.g., loading, success, error, different selections).
