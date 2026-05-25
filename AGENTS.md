# Agent Guardrails

Use these rules whenever an LLM is operating in this repo.

## Core safety
- Never commit, push, or amend commits without explicit user permission.
- Never run destructive commands (e.g., `rm -rf`, `git reset --hard`) unless explicitly asked.
- Do not change or remove files outside the repo.

## Code changes
- After any code change, run the appropriate tests for the affected area.
- If tests fail and the fix is within scope, update the tests and/or code, then rerun.
- If tests are too slow or require credentials, explain why and ask before skipping.
- Avoid unrelated refactors; keep changes narrowly scoped to the request.

## Validation
- If you can’t run tests, state what was skipped and why.
- Summarize modified files and key behavior changes.

## Playwright MCP testing
- When using the Playwright MCP server to navigate and test the app, create or use two accounts that are connected to each other. Otherwise, the dashboard will not show any connections.

## Communication
- Ask clarifying questions when requirements are ambiguous.
- State assumptions explicitly.
