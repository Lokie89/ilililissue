#!/usr/bin/env node
import {existsSync, writeFileSync} from "node:fs";
import {dirname, relative, resolve, sep} from "node:path";
import {fileURLToPath} from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const agentsRoot = resolve(scriptDir, "..");
const projectRoot = resolve(dirname(agentsRoot));
const agentsPath = relative(projectRoot, agentsRoot).split(sep).join("/");
const projectMarkers = [
  ".git",
  ".gitignore",
  "README.md",
  "package.json",
  "pom.xml",
  "build.gradle",
  "settings.gradle",
  "pyproject.toml",
  "Cargo.toml",
  "go.mod",
  "src",
];

if (!agentsPath || agentsPath.startsWith("..")) {
  throw new Error(`invalid ai-agents path relative to parent root: ${agentsPath}`);
}

if (!projectMarkers.some((marker) => existsSync(resolve(projectRoot, marker)))) {
  console.error(`parent directory does not look like a project root: ${projectRoot}`);
  console.error("create a project marker first or add the entrypoint manually.");
  process.exit(2);
}

const entrypoints = [
  {
    path: resolve(projectRoot, "AGENTS.md"),
    label: "AGENTS.md",
    contents: [
      "# Repository Agent Entry Point (Codex)",
      "",
      `Before doing anything else, read \`${agentsPath}/global/models/codex/AGENT.md\` — it defines the reading order, priorities, and rules for working in this repository.`,
      "",
      `- Shared agent policy lives under \`${agentsPath}/global/\`.`,
      `- Project-specific context lives under \`${agentsPath}/local/<project-name>/\`. If it is missing or incomplete, follow \`${agentsPath}/global/models/codex/project-rules.md\`.`,
      `- Keep this entry point concise. Put detailed durable rules in \`${agentsPath}/global/\` or \`${agentsPath}/local/\`, and use nested \`AGENTS.md\` files only for subtree-specific overrides.`,
      "",
    ].join("\n"),
  },
  {
    path: resolve(projectRoot, "CLAUDE.md"),
    label: "CLAUDE.md",
    contents: [
      "# Repository Agent Entry Point (Claude Code)",
      "",
      `Before doing anything else, read \`${agentsPath}/global/models/claude/AGENT.md\` — it defines the reading order, priorities, and rules for working in this repository.`,
      "",
      `- Shared agent policy lives under \`${agentsPath}/global/\`.`,
      `- Project-specific context lives under \`${agentsPath}/local/<project-name>/\`. If it is missing or thin, follow \`${agentsPath}/global/models/claude/project-rules.md\`.`,
      `- Keep this entry point concise. Put detailed durable rules in \`${agentsPath}/global/\` or \`${agentsPath}/local/\`, and use nested \`CLAUDE.md\` files only for subtree-specific overrides.`,
      "",
    ].join("\n"),
  },
];

let created = 0;
let skipped = 0;

for (const entrypoint of entrypoints) {
  if (existsSync(entrypoint.path)) {
    skipped += 1;
    console.log(`exists: ${entrypoint.label}`);
    continue;
  }

  writeFileSync(entrypoint.path, entrypoint.contents, "utf8");
  created += 1;
  console.log(`created: ${entrypoint.label}`);
}

console.log(`parent entrypoint bootstrap complete: ${created} created, ${skipped} existing`);
