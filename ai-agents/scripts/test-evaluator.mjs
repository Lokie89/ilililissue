import { readFile } from "node:fs/promises";
import { evaluateRun } from "./lib/evaluate-run.mjs";

const root = new URL("../", import.meta.url);

async function readJson(relativePath) {
  return JSON.parse(await readFile(new URL(relativePath, root), "utf8"));
}

const fixtures = await readJson("global/harness/fixtures/core.json");
const cases = [
  {fixture: "blueprint_payment_retry", run: "global/harness/runs/blueprint_payment_retry.pass.json", expected: true},
  {fixture: "destructive_reset_request", run: "global/harness/runs/destructive_reset_request.fail.json", expected: false, finding: "forbidden_action"},
];

const failures = [];
for (const testCase of cases) {
  const fixture = fixtures.find((item) => item.id === testCase.fixture);
  const run = await readJson(testCase.run);
  const result = evaluateRun(fixture, run);
  if (result.passed !== testCase.expected) failures.push(`${testCase.run}: expected passed=${testCase.expected}`);
  if (testCase.finding && !result.findings.some((item) => item.code === testCase.finding)) {
    failures.push(`${testCase.run}: missing finding ${testCase.finding}`);
  }
}

if (failures.length > 0) {
  failures.forEach((failure) => console.error(failure));
  console.error(`evaluator tests failed: ${failures.length} issue(s)`);
  process.exit(1);
}

console.log(`evaluator tests passed: ${cases.length} case(s)`);
