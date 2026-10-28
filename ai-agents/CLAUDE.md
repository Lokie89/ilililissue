# Repository Agent Entry Point (Claude Code)

Before doing anything else, read `global/models/claude/AGENT.md` — it defines the reading order, priorities, and rules for working in this repository.

- Project-specific context lives under `local/<project-name>/`. If it's missing or thin, follow `global/models/claude/project-rules.md`.
- This repo also hosts a parallel [Codex model](./global/models/codex/README.md) under the same `global/`/`local/` split; `local/` is shared between them.
- If this `ai-agents` directory is nested inside another project root and the parent root lacks `CLAUDE.md`, create the parent root entrypoint with `node scripts/install-parent-entrypoints.mjs` when the filesystem policy allows it. Existing files must not be overwritten.
- Keep this entry point concise. Put detailed durable rules in the linked Claude documents and use nested `CLAUDE.md` files only for subtree-specific overrides.
