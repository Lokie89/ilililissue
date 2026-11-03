# 기록 방식

## 아카이브 안내

- `현재 기록`에는 최근 5개 세션 기록만 남긴다.
- 그보다 오래된 기록은 오래된 순서대로 `./handoff-archive.md`로 옮긴다.
- 여기서 찾는 내용이 보이지 않으면 `./handoff-archive.md`(있다면)를 먼저 확인한다.

## 세션 기록 템플릿

### YYYY-MM-DD

- 목표:
- 변경:
- 검증:
- 남은 작업:
- 주의 사항:

## 현재 기록

### 2026-11-03 (오류·버그 수정 시 도메인·정책 우선 원칙)

- 목표: 사용자가 "그때그때 에러, 버그 수정을 위한 덧붙이기용 코드를 양산하면 안 되고, 명확한 도메인과 정책을 쌓아가야 하며, 궁금증이 있으면 도메인·정책 질문을 우선한다"는 원칙을 요청했다.
- 변경: Codex/Claude `domain-rules.md`에 `오류·버그 수정 원칙` 절을 추가했다. `goal.md` 금지 항목, `roles.md` Developer/Reviewer 관점, `harness/failure-cases.md` 도메인 정책 위반, `validation.md` 체크리스트에 같은 기준을 연결하고 `scripts/validate-docs.sh`에 핵심 문구 확인을 추가했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 추가 변경: 사용자가 global/local 샌드박스 정책을 확정했다. global은 원본 저장소에서 사용자만 쓰고 프로젝트로 한 방향으로만 배포하며, 프로젝트 세션은 local에만 쓰고 global로 올리지 않는다. 확인 순서는 global → local이고 충돌하면 local을 따른다. `AGENT.md` `글로벌과 로컬`, 실패 케이스, 검증 체크리스트에 반영했다.
- 배포 변경: 단일 `scripts/sync.mjs`(+`scripts/lib/sync.mjs`, 회귀 테스트 `scripts/test-sync.mjs`, 대상 `sync-targets.json`)로 기존 부트스트랩 스크립트 3개를 대체했다. 대상의 `ai-agents/`는 읽기 전용 사본으로 덮어쓰고, 루트 진입점은 관리 블록(`@import`)만 갱신하며, 루트 `.claude/`·`.codex/`에 에이전트와 hook을 설치하고, local 하네스는 `docs/agents/`(옛 `ai-agents/local/<project>/`는 이동·경로 수정)로 둔다. `_Study` 10개와 `_Project` fms2, fms3, fms-lb, fms-daeduck, fms-ygone, fms-shinsung에 적용했고 재실행 시 변경 없음을 확인했다(대상 커밋은 하지 않음).
- 배포 남은 작업: auto-simulate는 처음에 다른 세션이 폴더를 잡고 있어(EPERM) 이동이 실패했고, 잠금이 풀린 뒤 다시 적용했다. fms2는 사용자 결정에 따라 대상 하나로 두고 모듈별 local을 `docs/agents/fms/`, `docs/agents/fms-ui/`에 두며 `docs/agents/context-map.md`를 모듈 색인으로 삼았다(루트 평면 사본은 원본 이력 확인 후 제거). fms_simulator는 사용자 결정으로 동기화 대상에서 제외한다. chart-manager 루트 `CLAUDE.md`의 "ai-agents는 무관" 문구는 사용자 승인으로 "규칙을 따르고 수정하지 않는다"로 바꿨다. 큰 local 문서(auto-simulate, simulation-reporter, nyamlog, fms3, fms2/fms 등)와 템플릿 뼈대만 생긴 프로젝트의 `context-map.md` 채우기가 남았다.
- 남은 작업: 없음.
- 주의 사항: 정책과 무관한 단순 구현 실수는 최소 수정 대상이며, 임시 우회는 사용자 확인과 제거 조건 기록이 있을 때만 허용한다.

### 2026-08-30 (세션 첫 대화 인계 요약)

- 목표: 새 세션의 첫 응답에서 최근 작업, 진행 중인 내용, 남은 작업을 알려주도록 한다.
- 변경: Codex·Claude 실행 진입 문서, 산출물 스키마, fixture, 루브릭, 실패 케이스, 검증 기준에 첫 응답 인계 요약 규칙을 같은 의미로 추가했다. 문서 검증 스크립트도 두 모델의 핵심 문구와 fixture를 확인하도록 보강했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: 요약은 최신 글로벌 기록과 현재 요청에 해당하는 로컬 프로젝트 기록을 근거로 하며, 첫 응답 뒤에는 반복하지 않는다.

### 2026-08-27 (push 실패 시 원격 충돌 자동 해결 금지)

- 목표: push 요청 또는 자동 push 설정이 있더라도 원격 저장소 이력 충돌이 발생하면 에이전트가 merge/rebase/conflict resolution으로 해결하지 않고 사용자에게 넘기도록 규칙을 명시한다.
- 변경: Codex/Claude `tools.md`에 `Git 원격 동기화 경계` 절을 추가해 push 실패 시 원인 보고 후 중단하고, `git pull`, `git merge`, `git rebase`, conflict resolution, force push를 임의로 실행하지 않도록 했다. 양쪽 실패 케이스와 검증 문서, 문서 검증 스크립트에 같은 기준을 연결했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: 사용자가 merge/rebase까지 명시하더라도 원격 이력 변경이나 충돌 해결이 포함되면 별도 승인과 현재 변경 보호 확인 없이는 진행하지 않는다.

### 2026-08-27 (nested ai-agents 부모 진입점 자동 생성 보강)

- 목표: `ai-agents/` 디렉터리를 프로젝트 루트 안에 넣어 쓰는 경우에도 Claude가 한 번 지침을 읽은 뒤 부모 루트의 `CLAUDE.md`를 자동으로 보강할 수 있게 한다.
- 변경: `scripts/install-parent-entrypoints.mjs`를 추가해 부모 루트에 얇은 `AGENTS.md`/`CLAUDE.md` 포인터를 생성하도록 했다. 기존 파일은 덮어쓰지 않으며, 부모가 프로젝트 루트처럼 보이지 않으면 생성하지 않는다. 루트 `AGENTS.md`/`CLAUDE.md`, Codex/Claude `AGENT.md`, `project-rules.md`, `validation.md`, `README.md`, 기존 부트스트랩 스크립트와 문서 검증 스크립트를 같은 기준으로 갱신했다.
- 검증: 임시 nested 프로젝트에서 `node .../install-parent-entrypoints.mjs` 생성/재실행 skip 동작을 확인했다. `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `node scripts/ensure-entrypoints.mjs`, `git diff --check` 통과.
- 남은 작업: 실제 다른 프로젝트 루트에 `ai-agents/`를 넣고 Claude가 처음 읽는 세션에서 부모 루트 진입점 생성이 기대대로 작동하는지 사례를 확인할 수 있다.
- 주의 사항: 스크립트는 부모 루트에 쓰는 작업이므로 도구 파일시스템 정책이 허용하는 경우에만 실행된다. 기존 `AGENTS.md`/`CLAUDE.md`가 있으면 내용을 병합하지 않고 건너뛴다.

### 2026-08-27 (외부 연동 인터페이스 문서화 원칙 추가)

- 목표: 외부 연동 어댑터, API, 메시지, 파일 교환을 추가하거나 변경할 때 항상 인터페이스 문서를 남기도록 저장소 전역 규칙을 명시한다.
- 변경: Codex/Claude `project-rules.md`에 외부 연동 인터페이스 문서화 절을 추가하고, 새 프로젝트 완료 기준과 로컬 하네스 품질 기준에 연결했다. Codex/Claude `validation.md`와 실패 케이스에 인터페이스 문서 누락 검증을 추가했다. `local/_template/architecture.md`, `local/sample-project/architecture.md`, `local/README.md`에 인터페이스 문서 위치와 최소 계약 항목을 반영했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check` 통과.
- 남은 작업: 실제 외부 연동 프로젝트에서는 `architecture.md`, `README.md`, `docs/` 중 프로젝트 구조에 맞는 곳에 구체 인터페이스 문서를 작성해야 한다.
- 주의 사항: 비밀값은 실제 값을 쓰지 않고 이름, 목적, 필요 여부만 기록한다. 외부 제공자의 최신 API 동작과 제한은 공식 문서나 1차 출처로 확인한다.
