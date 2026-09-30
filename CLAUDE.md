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


## Subagents
- When spawning via the `Agent` tool, always pass `model: "haiku"`.

## Tool Usage
- Web search, doc fetching, etc. can be done without asking permission first.
- Keep confirmation prompts for external/destructive tools minimal — only ask when the risk is genuinely high.

## IntelliJ MCP Tools
- When `mcp__idea__*` tools are available, prefer them for semantic work:
  - Renames: `rename_refactoring`, not multi-file `Edit`/`sed`.
  - Symbol lookup / usages / call sites: `search_symbol`, `get_symbol_info`, `analyze_calls` over `Grep`.
  - Error checks after edits: `get_file_problems` or `mcp__ide__getDiagnostics` before a full Gradle build; `build_project` for cross-module checks.
  - Tests: `execute_run_configuration` when a matching config exists.
  - Formatting: `reformat_file`.
- Chain them for refactors, e.g. `search_symbol` → `analyze_calls` → `rename_refactoring` → `get_file_problems`.
- Keep `Read`/`Edit`/`Grep` for plain reading, small edits, and non-symbol text search.
- Fall back to standard tools if the IDE isn't connected or a call fails.
