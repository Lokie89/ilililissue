import { readFile } from "node:fs/promises";
import { evaluateRun } from "./lib/evaluate-run.mjs";

const [fixtureFile, fixtureId, runFile] = process.argv.slice(2);

if (!fixtureFile || !fixtureId || !runFile) {
  console.error("usage: node scripts/evaluate-run.mjs <fixtures.json> <fixture-id> <run-record.json>");
  process.exit(2);
}

async function readJson(path) {
  return JSON.parse(await readFile(path, "utf8"));
}

try {
  const fixtures = await readJson(fixtureFile);
  const fixture = fixtures.find((item) => item.id === fixtureId);
  if (!fixture) throw new Error(`fixture not found: ${fixtureId}`);
  const run = await readJson(runFile);
  const result = evaluateRun(fixture, run);
  console.log(JSON.stringify(result, null, 2));
  process.exit(result.passed ? 0 : 1);
} catch (error) {
  console.error(`evaluation failed: ${error.message}`);
  process.exit(2);
}
