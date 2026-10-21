# Claude Code Guidelines

## Workflow

- Commit after every change, no matter how small. Stage the edited files by name and commit directly;
  no `git status`/`git diff` first.
- For large refactors, ambiguous tasks, or research requests: propose a plan first, get approval, then make changes.
- Only do what's asked. Asked for a test → write only the test, not the implementation it needs.
- Check existing code for an established pattern before proposing a design.
- Keep IDE import changes (star imports, reordering); don't revert them.

## Communication

- Terse responses. No preamble ("I'll now do X") or trailing summaries.
- Self-documenting code: names tell what, the body tells how, comments tell why. Never restate the code. When a
  comment or KDoc is needed, keep it to one or two short, crisp sentences.
- Teach as you go: when you spot a non-obvious concept or pitfall (races, HTTP semantics, DB internals), add a short
  note with the why and a concrete example from the code. Point out bugs found this way; don't fix them unasked.

## Token Efficiency

- Skip `git diff`, `git log`, `git status` unless directly needed.
- Don't re-read files after writing or editing them.
- Search directly (grep/find); don't spawn subagents for simple searches.
- Use `offset`/`limit` when only a section of a file is needed.

## Kotlin Style

- `private` by default. Only make something public if it cannot be private.
- No class inheritance: no `open`/`abstract`/base classes, no template method. Use interfaces, `by` delegation, strategy
  (interfaces / function types), sealed interfaces + a single `when`. Interface-extends-interface is fine. Exception:
  exception classes extending `HttpException`.
- Positional arguments at call sites; name args only when skipping a default or position is ambiguous.
- Explicit control flow (`if`, `for`) over `?.let`, `forEach`, scope functions.
- Null check with a single-line body (throw, return, default) → elvis: `notes[id] ?: throw NoteNotFoundException(id)`.
- Single-expression helper functions use an expression body with explicit return type. Endpoint (route-annotated)
  methods keep `{ return ... }` block bodies.
- Expression body over ~100 chars → break after `=` and put the expression on the next line:
  ```kotlin
  private fun parseId(noteId: String): Uuid =
      Uuid.parseOrNull(noteId) ?: throw InvalidNoteIdException(noteId)
  ```
- No `inline` on functions that don't take lambdas.
- Pass a bare lambda to Java functional interfaces (`executor.submit { ... }`), no explicit `Runnable { ... }` wrapper.
- Any annotated param → one param per line, annotation on its own line above the param:
  ```kotlin
  fun update(
      @PathParam("noteId")
      noteId: String,
      body: NoteReq
  ): Note {
  ```
- Booleans get an `is`/`has`/`should` prefix (`eagerly` stays as is).
- One model type per top-level file; no nesting in a container class. Grouping sealed subtypes in one file only when
  asked.
- Package names use the full concept, lowercase: `CustomSerdeCatalog*` → `customserdecatalog`.
- `lateinit var` for state set at the start of a method and used by private helpers, instead of threading a param — only
  if the method is called more than once or deferred from construction. Otherwise put it in the constructor.

## API Design

- Storage interfaces are named `*Store`, not repository.
- Store methods take a single request object (`create(NoteReq)`, `update(UpdateReq)`), not multiple params.
- Clean contracts: non-null returns; throw a named exception for missing entities, no `T?`/`Boolean` for absence.
  Updates return the updated entity.
- Client-facing errors: a named exception class extending `HttpException` with its status (see `BadClientException`).
  Never `throw HttpException(...)` directly. One exception type per status. No webserver error handlers.
- `POST` to create (server-generated ids), `PATCH` to update only the fields sent. No `PUT`.
- `400` for malformed input (e.g. invalid UUID), `404` for a missing resource on get/update/delete.

## Tests

- Name test functions in camelCase, no backtick-quoted strings or spaces. Keep them short and crisp (e.g.
  `serializeNonEmpty` not `"serialize produces non-empty bytes"`).

## Subagents

- When spawning via the `Agent` tool, always pass `model: "haiku"`.

## IntelliJ MCP Tools

- When `mcp__idea__*` tools are available, prefer them for semantic work:
  - Renames: `rename_refactoring`, not multi-file `Edit`/`sed`.
  - Symbol lookup / usages / call sites: `search_symbol`, `get_symbol_info`, `analyze_calls` over grep.
  - Error checks after edits: `get_file_problems` or `mcp__ide__getDiagnostics` before a full Gradle build;
    `build_project` for cross-module checks.
  - Tests: `execute_run_configuration` when a matching config exists.
  - Formatting: `reformat_file`.
- Chain them for refactors, e.g. `search_symbol` → `analyze_calls` → `rename_refactoring` → `get_file_problems`.
- Keep `Read`/`Edit`/grep for plain reading, small edits, and non-symbol text search.
- Fall back to standard tools if the IDE isn't connected or a call fails.
