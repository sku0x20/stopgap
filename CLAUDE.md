# Claude Code Guidelines

## Commits
- Commit after every change, no matter how small.

## Planning
- For large refactors, ambiguous tasks, or research requests: propose a plan first, get approval, then make changes.

## Token Efficiency
- Skip unnecessary `git diff`, `git log`, `git status` reads unless directly needed.
- Don't re-read files after writing or editing them.
- File changes are pushed via `<system-reminder>` tags — trust those. If no notification arrived, the cached view is still valid. Only fall back to `stat` if there's reason to doubt (e.g. a long conversation gap).
- No preamble ("I'll now do X") or trailing summaries — just make the change.
- Use `Grep`/`Glob` directly for simple searches; avoid spawning subagents unnecessarily.
- Use `offset`/`limit` when only a section of a file is needed.

## Refactoring
- Prefer `private` visibility by default. Only make something public if it cannot be private.

## Tests
- Name test functions in camelCase, no backtick-quoted strings or spaces. Keep them short and crisp (e.g. `serializeNonEmpty` not `"serialize produces non-empty bytes"`).

## External Commands
- Never run heavy external commands (e.g. `./gradlew`). Ask me to run them and share the output.

## Subagents
- When spawning via the `Agent` tool, always pass `model: "haiku"`.

## Tool Usage
- Web search, doc fetching, etc. can be done without asking permission first.
- Keep confirmation prompts for external/destructive tools minimal — only ask when the risk is genuinely high.
