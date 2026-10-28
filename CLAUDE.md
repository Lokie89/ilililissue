# Repository Agent Entry Point (Claude Code)

Before doing anything else, read `ai-agents/global/models/claude/AGENT.md` — it defines the reading order, priorities, and rules for working in this repository.

- Shared agent policy lives under `ai-agents/global/`.
- Project-specific context lives under `ai-agents/local/<project-name>/`. If it is missing or thin, follow `ai-agents/global/models/claude/project-rules.md`.
- Keep this entry point concise. Put detailed durable rules in `ai-agents/global/` or `ai-agents/local/`, and use nested `CLAUDE.md` files only for subtree-specific overrides.
