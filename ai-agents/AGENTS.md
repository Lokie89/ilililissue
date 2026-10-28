# Repository Agent Entry Point (Codex)

Before doing anything else, read `global/models/codex/AGENT.md` — it defines the reading order, priorities, and rules for working in this repository.

- Project-specific context lives under `local/<project-name>/`. If it is missing or incomplete, follow `global/models/codex/project-rules.md`.
- This repository also hosts a parallel [Claude model](./global/models/claude/README.md); both models share `local/`.
- If this `ai-agents` directory is nested inside another project root and the parent root lacks `AGENTS.md`, create the parent root entrypoint with `node scripts/install-parent-entrypoints.mjs` when the filesystem policy allows it. Existing files must not be overwritten.
- Keep this entry point concise. Put detailed durable rules in the linked Codex documents and use nested `AGENTS.md` files only for subtree-specific overrides.
