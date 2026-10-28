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

### 2026-08-20 (코드 분석 산출물 문서화 규칙 추가)

- 목표: 사용자가 "코드 분석도 토큰을 많이 쓰니 분석할 때마다 내용을 별도 문서로 만들자"고 요청했다. `AskUserQuestion`으로 적용 범위(이 저장소 전역 규칙)와 저장 위치/형식(`local/<project-name>/analysis/`에 Markdown)을 확인한 뒤 반영했다.
- 변경: `project-rules.md`에 `코드 분석 산출물` 절을 추가해, 코드베이스·아키텍처를 설명·요약·평가하는 분석 작업(짧은 단일 질의·자명한 파일 확인 제외)은 `local/<project-name>/analysis/YYYY-MM-DD-<주제-슬러그>.md`로 남기고, 분석 문서만 단독으로 만들 때는 로컬 하네스 전체 생성을 강제하지 않으며, 재분석 전 기존 문서 확인을 먼저 하도록 명시했다. `AGENT.md`의 `작업 후 규칙`에 이 기준을 참조하는 문장을 추가했다. `local/README.md`의 권장 구조에 선택 항목 `analysis/`를 추가했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`를 실행한다.
- 남은 작업: Codex 쪽(`global/models/codex/project-rules.md` 등)에는 아직 이 규칙을 반영하지 않았다 — 필요하면 동일한 절을 추가해 두 모델 간 비대칭을 없앤다. `local/_template/`이나 `local/sample-project/`에 `analysis/` 예시 디렉터리는 아직 만들지 않았다.
- 주의 사항: 이 규칙은 기존 로컬 하네스 필수 파일 6종을 대체하지 않고, 분석 산출물만 별도로 다룬다.
