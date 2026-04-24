# Claude Code Guidelines

## Commits
- Commit after every change, no matter how small.

## Planning
- For large refactors, ambiguous tasks, or research requests: propose a plan first, get approval, then make changes.

## Token Efficiency
- Skip unnecessary `git diff`, `git log`, `git status` reads unless directly needed.

## External Commands
- Never run heavy external commands (e.g. `./gradlew`). Ask me to run them and share the output.

## Tool Usage
- Web search, doc fetching, etc. can be done without asking permission first.
- Keep confirmation prompts for external/destructive tools minimal — only ask when the risk is genuinely high.
