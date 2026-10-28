import { readFile } from "node:fs/promises";

const fixturePath = new URL("../global/harness/fixtures/core.json", import.meta.url);
const runPaths = [
  new URL("../global/harness/runs/blueprint_payment_retry.pass.json", import.meta.url),
  new URL("../global/harness/runs/destructive_reset_request.fail.json", import.meta.url),
];
const schemaPaths = [
  new URL("../global/harness/schemas/fixture.schema.json", import.meta.url),
  new URL("../global/harness/schemas/run-record.schema.json", import.meta.url),
];

const failures = [];

async function readJson(path) {
  try {
    return JSON.parse(await readFile(path, "utf8"));
  } catch (error) {
    failures.push(`invalid JSON: ${path.pathname}: ${error.message}`);
    return null;
  }
}

function requireCondition(condition, message) {
  if (!condition) failures.push(message);
}

for (const path of schemaPaths) {
  const schema = await readJson(path);
  if (schema) {
    requireCondition(schema.$schema === "https://json-schema.org/draft/2020-12/schema", `unexpected schema draft: ${path.pathname}`);
    requireCondition(Array.isArray(schema.required) && schema.required.length > 0, `schema has no required fields: ${path.pathname}`);
  }
}

const fixtures = await readJson(fixturePath);
if (fixtures) {
  requireCondition(Array.isArray(fixtures) && fixtures.length > 0, "fixtures must be a non-empty array");
  const ids = new Set();

  for (const fixture of fixtures) {
    const label = fixture?.id ?? "<missing-id>";
    requireCondition(typeof fixture?.id === "string" && /^[a-z0-9][a-z0-9_-]+$/.test(fixture.id), `${label}: invalid id`);
    requireCondition(!ids.has(fixture?.id), `${label}: duplicate id`);
    ids.add(fixture?.id);
    requireCondition(typeof fixture?.request === "string" && fixture.request.length > 0, `${label}: request is required`);
    requireCondition(typeof fixture?.expected?.writes_allowed === "boolean", `${label}: expected.writes_allowed is required`);
    requireCondition(Array.isArray(fixture?.expected?.required_evidence), `${label}: required_evidence must be an array`);
    requireCondition(Array.isArray(fixture?.expected?.forbidden_actions), `${label}: forbidden_actions must be an array`);

    const limits = fixture?.limits ?? {};
    requireCondition(Number.isInteger(limits.max_turns) && limits.max_turns > 0, `${label}: max_turns must be positive`);
    requireCondition(Number.isInteger(limits.max_tool_calls) && limits.max_tool_calls >= 0, `${label}: max_tool_calls must be non-negative`);
    requireCondition(Number.isInteger(limits.timeout_seconds) && limits.timeout_seconds > 0, `${label}: timeout_seconds must be positive`);
    requireCondition(Number.isInteger(limits.max_repeated_failures) && limits.max_repeated_failures > 0, `${label}: max_repeated_failures must be positive`);

    const approvals = fixture?.expected?.approval_required_for ?? [];
    if (approvals.length > 0 && fixture.expected.terminal_status === "completed") {
      requireCondition(fixture.expected.required_evidence.includes("explicit_approval") || !approvals.some((item) => item.includes("reset") || item.includes("discard")), `${label}: destructive completion requires explicit_approval evidence`);
    }
  }
}

for (const path of runPaths) {
  const run = await readJson(path);
  if (run) {
    requireCondition(typeof run.run_id === "string" && run.run_id.length > 0, `${path.pathname}: run_id is required`);
    requireCondition(typeof run.fixture_id === "string" && run.fixture_id.length > 0, `${path.pathname}: fixture_id is required`);
    requireCondition(Array.isArray(run.events), `${path.pathname}: events must be an array`);
    requireCondition(Array.isArray(run.evidence), `${path.pathname}: evidence must be an array`);
    requireCondition(Array.isArray(run.validation), `${path.pathname}: validation must be an array`);
    requireCondition(Array.isArray(run.violations), `${path.pathname}: violations must be an array`);
  }
}

if (failures.length > 0) {
  for (const failure of failures) console.error(failure);
  console.error(`harness validation failed: ${failures.length} issue(s)`);
  process.exit(1);
}

console.log(`harness validation passed: ${fixtures.length} fixture(s)`);
