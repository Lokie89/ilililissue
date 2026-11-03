<!-- ai-agents:start -->
<!-- 원본 ai-agents 저장소의 scripts/sync.mjs가 관리하는 블록입니다. 블록 안은 직접 고치지 마세요. -->
## AI 에이전트 하네스

- 작업 전에 `ai-agents/global/models/codex/AGENT.md`를 읽고 그 읽는 순서를 따른다. 문서 이름은 `ai-agents/global/models/codex/` 기준이다.
- 그다음 이 프로젝트의 local 하네스 `docs/agents/context-map.md`를 읽는다.
- 공통 규칙(global): `ai-agents/global/`. 원본 `ai-agents` 저장소의 읽기 전용 사본이므로 이 프로젝트 세션에서는 수정하지 않는다.
- 이 프로젝트의 local 하네스: `docs/agents/`. global 문서의 `local/<project-name>/`는 이 경로를 뜻한다. 이 프로젝트에서 생기는 규칙과 기록은 여기에만 쓴다.
- 규칙 확인 순서: global을 먼저 보고 local을 본다. 충돌하면 local을 따른다.
<!-- ai-agents:end -->

# Repository Agent Entry Point (Codex)

Before doing anything else, read `ai-agents/global/models/codex/AGENT.md` — it defines the reading order, priorities, and rules for working in this repository.

- Shared agent policy lives under `ai-agents/global/`.
- Project-specific context lives under `ai-agents/local/<project-name>/`. If it is missing or incomplete, follow `ai-agents/global/models/codex/project-rules.md`.
- Keep this entry point concise. Put detailed durable rules in `ai-agents/global/` or `ai-agents/local/`, and use nested `AGENTS.md` files only for subtree-specific overrides.
