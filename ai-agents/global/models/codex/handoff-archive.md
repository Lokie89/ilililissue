# 기록 아카이브

`handoff-log.md`의 최근 기록 보관 한도를 넘긴 이전 세션 기록을 오래된 순서로 보관한다.

## 2026-08-27 (nested ai-agents 부모 진입점 자동 생성 보강)

- 목표: `ai-agents/` 디렉터리를 프로젝트 루트 안에 넣어 쓰는 경우에도 Codex가 한 번 지침을 읽은 뒤 부모 루트의 `AGENTS.md`를 자동으로 보강할 수 있게 한다.
- 변경: `scripts/install-parent-entrypoints.mjs`를 추가해 부모 루트에 얇은 `AGENTS.md`/`CLAUDE.md` 포인터를 생성하도록 했다. 기존 파일은 덮어쓰지 않으며, 부모가 프로젝트 루트처럼 보이지 않으면 생성하지 않는다. 루트 `AGENTS.md`/`CLAUDE.md`, Codex/Claude `AGENT.md`, `project-rules.md`, `validation.md`, `README.md`, 기존 부트스트랩 스크립트와 문서 검증 스크립트를 같은 기준으로 갱신했다.
- 검증: 임시 nested 프로젝트에서 `node .../install-parent-entrypoints.mjs` 생성/재실행 skip 동작을 확인했다. `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `node scripts/ensure-entrypoints.mjs`, `git diff --check` 통과.
- 남은 작업: 실제 다른 프로젝트 루트에 `ai-agents/`를 넣고 Codex가 처음 읽는 세션에서 부모 루트 진입점 생성이 기대대로 작동하는지 사례를 확인할 수 있다.
- 주의 사항: 스크립트는 부모 루트에 쓰는 작업이므로 Codex 파일시스템 정책이 허용하는 경우에만 실행된다. 기존 `AGENTS.md`/`CLAUDE.md`가 있으면 내용을 병합하지 않고 건너뛴다.

## 2026-08-27 (외부 연동 인터페이스 문서화 원칙 추가)

- 목표: 외부 연동 어댑터, API, 메시지, 파일 교환을 추가하거나 변경할 때 항상 인터페이스 문서를 남기도록 저장소 전역 규칙을 명시한다.
- 변경: Codex/Claude `project-rules.md`에 외부 연동 인터페이스 문서화 절을 추가하고, 새 프로젝트 완료 기준과 로컬 하네스 품질 기준에 연결했다. Codex/Claude `validation.md`와 실패 케이스에 인터페이스 문서 누락 검증을 추가했다. `local/_template/architecture.md`, `local/sample-project/architecture.md`, `local/README.md`에 인터페이스 문서 위치와 최소 계약 항목을 반영했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check` 통과.
- 남은 작업: 실제 외부 연동 프로젝트에서는 `architecture.md`, `README.md`, `docs/` 중 프로젝트 구조에 맞는 곳에 구체 인터페이스 문서를 작성해야 한다.
- 주의 사항: 비밀값은 실제 값을 쓰지 않고 이름, 목적, 필요 여부만 기록한다. 외부 제공자의 최신 API 동작과 제한은 공식 문서나 1차 출처로 확인한다.

## 2026-07-22

- 목표: AI Agents 저장소의 기본 모델 기준을 만들고 Codex 모델을 첫 번째로 정의한다.
- 변경: `README.md`와 `global/models/codex/` 문서 세트를 생성했다. Codex 모델에 청사진 우선 모드를 추가했다. 공통 문서는 `global/`, 프로젝트별 문서는 `local/`로 나누었다. 글로벌 Codex 규칙에 보이스카웃 규칙, 도메인 우선 원칙, 도메인 모델과 데이터베이스 모델 분리 원칙을 추가했다. 도메인 처리 방식은 글로벌 `domain-rules.md`로, 실제 도메인 정책은 로컬 `domain-policy.md`로 분리했다. 사람용 `README.md`와 에이전트용 `AGENT.md`를 분리했다. 역할별 모델 선택 루브릭을 `model-routing.md`로 추가하고, 작업 조건별 승격/강등 기준을 보강했다.
- 검증: 문서 구조와 내부 링크를 확인한다.
- 남은 작업: 실제 프로젝트 유형별 Codex 운영 예시와 템플릿을 추가할 수 있다.
- 주의 사항: 이 기록의 모델 별칭과 YAML frontmatter 기준은 2026-08-09 현행화에서 폐기됐다. 현재 기준은 `model-routing.md`를 따른다.

## 2026-07-26

- 목표: Codex 운영 문서를 실제 하네스로 검증하기 위한 기준 자료를 추가한다.
- 변경: `global/models/codex/harness/`에 상태 전이, 실패 케이스, 산출물 스키마, fixture 문서를 추가했다. 루트 README, Codex README, 컨텍스트 지도, 검증 문서에 하네스 참조를 연결했다. `local/sample-project/`에 샘플 로컬 정책과 검증 문서를 추가했다.
- 검증: `./scripts/validate-docs.sh`로 문서 구조와 핵심 참조를 확인한다.
- 남은 작업: 필요하면 Markdown 전체 링크 검사나 fixture별 응답 채점기를 추가할 수 있다.
- 주의 사항: 당시 자동 검증은 필수 파일과 핵심 참조 확인에 한정됐으며, 2026-08-09에 구조화 fixture와 실행 기록 evaluator가 추가됐다.

## 2026-07-28

- 목표: Codex 하네스가 실제 작업 판정에 더 직접적으로 쓰이도록 fixture와 판정 기준을 보강한다.
- 변경: `evaluation-rubric.md`를 추가해 요청 유형, 통과 기준, 실패 우선 원칙, 판정 기록 형식을 정의했다. 승인 필요한 명령, 사용자 변경 보호, 검증 실패 보고, HTML 템플릿 내 JavaScript 수정 fixture를 추가했다. 실패 케이스에 사용자 변경 보호, 승인 및 실행 경계, 최신 정보 사용, 템플릿 내 JavaScript 검증 누락을 추가했다. 하네스 README, 컨텍스트 지도, 검증 문서, 문서 검증 스크립트에 새 루브릭 참조를 연결했다. `compare-report.html`처럼 JavaScript가 들어간 HTML 템플릿을 수정한 뒤에는 Java 컴파일과 별도로 script를 추출해 `node --check`를 실행하도록 검증 기준을 보강했다.
- 검증: `bash scripts/validate-docs.sh`는 WSL 배포판 부재로 실행되지 않았다. 같은 파일 존재와 핵심 참조 조건을 PowerShell로 대체 실행해 통과했다.
- 남은 작업: fixture별 실제 응답 샘플과 자동 채점 스크립트를 추가할 수 있다.
- 주의 사항: 새 루브릭은 체크리스트 판정 기준이며, 실제 프로젝트 테스트나 보안 검토를 대체하지 않는다.

## 2026-08-04

- 목표: Codex 운영 규칙에서 청사진, handoff, TDD, 프로젝트 진입 기준을 실제 작업 흐름에 맞게 조정한다.
- 변경: 청사진만 요청받은 경우와 명확한 구현 요청을 분리했다. handoff 기록은 문서, 정책, 하네스, 장기 작업처럼 인계 가치가 있는 변경에 필수로 두고 작은 단일 변경은 최종 보고로 대체할 수 있게 했다. TDD 기준은 동작 변경 중심으로 완화하고 문서, 설정 변경에는 대체 검증 기준을 사용할 수 있게 했다. `project-rules.md`를 추가해 로컬 규칙이 없거나 부족한 프로젝트에서 로컬 하네스 대화를 생성하고, 필요한 경우 `local/<project-name>/` 문서를 만들도록 기준을 정의했다. `local/_template/`에 `goal.md`, `context-map.md`, `tools.md`, `validation.md`, `handoff-log.md` 템플릿을 추가했다. 새 프로젝트 기본 완료 체크리스트, 프로젝트 유형별 최소 기준, 프로젝트 생성 결과 보고 스키마를 추가했다. 문서 검증 스크립트에 새 경계 규칙, 프로젝트 규칙, 로컬 템플릿, 하네스 fixture 참조 확인을 추가했다.
- 검증: sandbox 내부 `bash scripts/validate-docs.sh`는 `E_ACCESSDENIED`로 실패했다. 권한 밖에서 같은 명령을 다시 실행해 `document validation passed`를 확인했고, PowerShell 대체 검증으로 필수 파일과 핵심 문구도 확인했다.
- 남은 작업: 필요하면 Markdown 링크 전체 검사 스크립트를 별도 추가할 수 있다.
- 주의 사항: 명확한 구현 요청은 실행할 수 있지만, 도메인 정책, 보안, 권한, 데이터 손실, 되돌리기 어려운 변경이 불명확하면 구현 전에 질문해야 한다. 새 프로젝트 생성이나 프로젝트별 실행/검증 방식이 필요한 작업은 로컬 하네스 필요 여부를 먼저 판단해야 한다.

## 2026-08-09

- 목표: Claude 문서의 개선 사항 중 Codex에도 공통으로 유효한 운영 규칙을 선별해 반영한다.
- 변경: 루트 `AGENTS.md` 진입점을 추가했다. Codex 내장 안전 규칙 우선, 공유 상태 변경 사전 확인, hook/서명 우회 금지, 모델 간 `local/` 공유 원칙, handoff 최근 5개 보관 및 아카이브 규칙, UI/프론트엔드 실동작 검증 기준을 Codex 문서에 추가했다. Claude 전용 도구명과 Workflow 규칙은 옮기지 않았다.
- 검증: `bash scripts/validate-docs.sh`로 문서 구조와 핵심 참조를 확인한다.
- 남은 작업: `handoff-archive.md`는 현재 기록이 5개를 초과할 때 생성한다.
- 주의 사항: 모델별 도구 이름과 실행 방식은 각 모델 문서에만 두고, 공유 `local/` 문서는 프로젝트 기준으로 유지한다.

## 2026-08-09 (Codex 문서 현행화)

- 목표: 현재 Codex 문서를 공식 Codex 운영 방식과 대조해 실행 경계와 설정 정확성을 보완한다.
- 변경: 계획 단계에서 읽기 전용 조사와 안전한 진단은 허용하되 파일·외부 상태 변경은 금지하도록 경계를 바로잡았다. 추상 모델 별칭과 YAML frontmatter 예시를 제거하고 실제 모델 ID, `model_reasoning_effort`, `.codex/agents/<name>.toml`, `[agents]` 설정 기준으로 모델 라우팅 문서를 개편했다. `AGENTS.md` 계층을 간결하게 유지하는 원칙, 메인 컨텍스트 절약, 읽기 중심 서브에이전트 우선, 동시 쓰기 충돌 방지 기준을 추가했다. `global/harness/`에 런타임 계층, 종료·재시도·승인·관측성 계약, JSON Schema, 공통 fixture를 추가하고 무의존성 정적 검증기를 연결했다.
- 검증: 공식 Codex 매뉴얼의 Best practices, Subagents, AGENTS.md 섹션과 대조했다. `node scripts/validate-harness.mjs`, `bash scripts/validate-docs.sh`, `git diff --check`를 실행한다.
- 남은 작업: 실제 커스텀 에이전트를 도입할 때만 `.codex/config.toml`과 `.codex/agents/*.toml`을 추가한다.
- 주의 사항: 모델 ID와 지원 추론 강도는 제품·계정·시점에 따라 바뀔 수 있으므로 글로벌 문서에 특정 ID를 영구 기본값으로 고정하지 않는다.

## 2026-08-09 (실행 기록 자동 판정)

- 목표: 문서 fixture를 실제 실행 기록과 비교할 수 있는 결정론적 evaluator를 추가한다.
- 변경: 실행 기록에 `evidence`와 도구의 `mutates_state`를 표현하도록 스키마를 확장했다. fixture와 run record를 비교해 종료 상태, 쓰기 금지, 금지 행동, 승인 누락, 필수 근거, 예산 초과, 보고된 위반을 판정하는 evaluator와 CLI를 추가했다. 정상 계획 실행과 승인 없는 파괴적 실행 샘플을 회귀 테스트로 연결했다.
- 검증: `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, 정상 실행 기록에 대한 `node scripts/evaluate-run.mjs ...`, `bash scripts/validate-docs.sh`, `git diff --check`가 통과했다.
- 남은 작업: 실제 Codex 또는 Claude 실행을 `run-record.schema.json`으로 변환하는 adapter와 자연어 의미 evaluator는 아직 없다.
- 주의 사항: 현재 evaluator는 기록된 event와 evidence를 신뢰하는 결정론적 판정기다. trace 수집기가 연결되기 전에는 실제 행동을 독립적으로 증명하지 않는다.

## 2026-08-11 (Codex 운영 공백 보완)

- 목표: 현재 문서 기준에서 Codex 작업 시 빠지기 쉬운 실행 환경, 하위 지침, 사용자 변경 보호, 최신 정보 검증, 최종 리뷰 기준을 보완한다.
- 변경: `AGENT.md`에 하위 `AGENTS.md` 적용 범위, UTF-8 읽기, dirty worktree 확인, Codex hook 부재 시 수동 Reviewer 확인, 최신 정보 출처 확인 기준을 추가했다. `context-map.md`, `tools.md`, `validation.md`, 하네스 실패 케이스와 루브릭에 같은 기준을 연결했다. Windows에서 `bash scripts/validate-docs.sh`가 불가할 때 PowerShell과 Node 기반 대체 검증을 기록하도록 했다.
- 검증: `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, PowerShell 필수 문구 확인, `git diff --check`가 통과했다. `bash scripts/validate-docs.sh`는 sandbox에서 `E_ACCESSDENIED`, 권한 밖 재시도에서 CRLF 줄끝 해석 문제로 실패해 대체 검증으로 보완했다.
- 남은 작업: Codex 전용 PowerShell 문서 검증 스크립트가 필요하면 `scripts/validate-docs.ps1`로 별도 추가할 수 있다.
- 주의 사항: Claude 전용 hook, Workflow, Agent 도구명은 Codex 문서로 옮기지 않았다. Codex에서는 자동 hook 대신 최종 리뷰 체크를 문서 기준으로 수행한다.

## 2026-08-11 (숙의형 멀티 에이전트 패턴 추가)

- 목표: 여러 에이전트가 독립 의견, 비판, 수정, 합성, 검증 라운드로 더 나은 결론을 만드는 공통 패턴을 문서화한다.
- 변경: `global/harness/deliberation.md`를 추가해 Orchestrator, Proposer, Critic, Alternative, Synthesizer, Verifier 역할과 라운드, 산출물 형식, 라운드별 요약 로그, 실패 케이스, 종료 조건을 정의했다. 공통 하네스 README/architecture, 루트 README, Codex/Claude 모델 라우팅과 검증 문서, 모델별 실패 케이스, 문서 검증 스크립트에 참조를 연결했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, PowerShell 필수 문구 확인, `git diff --check`가 통과했다. 기존 CRLF 줄끝 문제는 `scripts/validate-docs.sh`를 LF로 정리해 해결했다. 라운드별 요약 로그 문구도 문서 검증 스크립트에 추가했다.
- 남은 작업: 실제 런타임을 만들 때 `AgentOpinion`, `Critique`, `FinalSynthesis` 같은 JSON 스키마와 샘플 run record를 추가할 수 있다.
- 주의 사항: 이 변경은 런타임 구현이 아니라 공통 설계 계약이다. 특정 도메인 판단은 hard gate와 프로젝트 정책이 우선하고, 숙의는 애매한 영역의 의견 품질 개선에 사용한다.

## 2026-08-12 (로컬 아키텍처 청사진 추가)

- 목표: 프로젝트별 로컬 하네스가 도메인 정책뿐 아니라 아키텍처 청사진도 먼저 확인하도록 기준을 확장한다.
- 변경: `local/_template/architecture.md`와 `local/sample-project/architecture.md`를 추가했다. Codex/Claude 프로젝트 규칙, 목표, AGENT, 컨텍스트 지도, 검증, 하네스 산출물, fixture, 실패 케이스에 `architecture.md` 확인과 청사진 질문 기준을 연결했다. `local/README.md`, 샘플 context-map, 루트 README, 문서 검증 스크립트도 새 필수 문서에 맞췄다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 실제 프로젝트에 적용하면서 아키텍처 질문 항목이 과하거나 부족한지 사례를 보고 조정할 수 있다.
- 주의 사항: 아키텍처는 현재 기준의 청사진이며 모든 항목을 한 번에 확정하지 않는다. 구현 전제에 직접 영향을 주는 항목만 질문으로 승격하고, 나머지는 `미정` 또는 `해당 없음`으로 기록한다.

## 2026-08-12 (프로젝트 시작 청사진 선행 강화)

- 목표: 새 프로젝트 생성이나 초기 세팅 요청에서 파일 생성보다 청사진 제시가 먼저 일어나도록 하네스 기준을 강화한다.
- 변경: Codex/Claude `AGENT.md`, `goal.md`, `project-rules.md`, `context-map.md`, 상태 머신, 평가 루브릭, fixture, 실패 케이스, 산출물 스키마, 검증 문서에 프로젝트 bootstrap의 blueprint-first 규칙을 추가했다. 구조화 fixture `project_bootstrap_customer_support`도 `writes_allowed: false`로 바꿔 청사진 승인 전 파일 생성이 실패로 판정되게 했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 실제 새 프로젝트 시작 세션에서 agent가 청사진 후 승인 대기하는지 사례로 확인할 수 있다.
- 주의 사항: 사용자가 "바로 파일 생성해", "청사진 생략하고 구현해"처럼 명시적으로 승인한 경우에는 구현으로 넘어갈 수 있지만, 기본값은 청사진 선행이다.

## 2026-08-12 (문서 언어 정책 추가)

- 목표: 저장소 운영 문서와 로컬 하네스 문서의 본문 언어 기준을 명시한다.
- 변경: Codex/Claude `AGENT.md`에 문서 언어 섹션을 추가해 본문은 기본적으로 한국어, 파일명·코드 식별자·명령어·스키마 필드명·모델 ID·외부 API 이름은 원문 또는 영어 유지 기준을 정의했다. 양쪽 `project-rules.md`, `validation.md`, `local/README.md`, 문서 검증 스크립트에도 같은 기준을 연결했다.
- 검증: `bash scripts/validate-docs.sh`, `git diff --check`를 실행한다.
- 남은 작업: 실제 프로젝트별 로컬 문서에서 영어 본문이 필요한 경우 예외 이유를 남기는 관례를 적용한다.
- 주의 사항: 루트 `AGENTS.md`/`CLAUDE.md` 같은 짧은 부트스트랩 문서는 영어를 허용하고, 상세 정책은 한국어 문서에 둔다.

## 2026-08-13 (루트 진입 문서 부트스트랩)

- 목표: `ai-agents` 폴더를 다른 프로젝트로 복사했을 때 루트 `AGENTS.md` 또는 `CLAUDE.md`가 없으면 바로 생성할 수 있게 한다.
- 변경: `scripts/ensure-entrypoints.mjs`를 추가해 Codex용 `AGENTS.md`와 Claude Code용 `CLAUDE.md`를 생성한다. 기존 파일은 덮어쓰지 않는다. `README.md`, Codex/Claude `project-rules.md`, Codex/Claude `validation.md`, `scripts/validate-docs.sh`에 부트스트랩 명령과 검증 기준을 연결했다.
- 검증: `node scripts/ensure-entrypoints.mjs`는 기존 파일을 감지하고 `0 created`로 종료했다. `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: 이 스크립트는 루트 진입 문서만 생성하며 `global/` 또는 `local/` 문서 세트가 누락된 경우까지 복원하지 않는다.

## 2026-08-13 (테스트 코드 문서성 명시)

- 목표: 테스트 코드가 기능 구현 시 자주 참조해야 하는 문서 역할도 한다는 기준을 명시한다.
- 변경: Codex/Claude `roles.md`의 Developer 기준에 "테스트 코드도 문서"라는 원칙과 요구사항, 사용 예, 경계 조건 참고 자료로 활용해야 한다는 문장을 추가했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: 테스트를 문서로 보되, 실제 제품 문서나 도메인 정책을 대체하는 것으로 취급하지 않는다.

## 2026-08-13 (기본 토론 에이전트 추가)

- 목표: 하네스와 에이전트 운영 정책을 토론할 때 기본으로 쓸 Codex 커스텀 에이전트와 제품 개발 토론용 기본 역할 에이전트 세트를 추가한다.
- 변경: `.codex/agents/harness-deliberator.toml`, `product-planner.toml`, `ux-ui-designer.toml`, `frontend-developer.toml`, `backend-developer.toml`, `database-specialist.toml`, `product-tester.toml`을 추가했다. 공통 `global/harness/deliberation.md`에 토론 후 문서화 기준을 추가하고, Codex/Claude 검증·실패 케이스·산출물 스키마에 같은 기준을 연결했다. Codex `model-routing.md`에는 하네스·제품·기능 토론에서 이 커스텀 에이전트들을 우선 고려하도록 명시했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, TOML 문법 검사, `git diff --check`를 실행한다.
- 남은 작업: 실제 Codex 런타임에서 커스텀 에이전트들이 로드되는지는 다음 세션에서 호출해 확인할 수 있다.
- 주의 사항: 에이전트에는 모델을 고정하지 않아 현재 세션, 명시적 spawn 값, `[agents]` 기본값, 부모 설정을 상속하게 했다.

## 2026-08-13 (프로젝트 루트 부트스트랩 확장)

- 목표: 이 문서 세트를 다른 프로젝트에 복사했을 때 루트 진입 문서와 기본 Codex 에이전트 세트를 자동으로 보강할 수 있게 한다.
- 변경: `scripts/bootstrap-project-root.mjs`를 추가해 `AGENTS.md`, `CLAUDE.md`, `.codex/agents/*.toml` 기본 세트를 생성하도록 했다. 기존 파일은 덮어쓰지 않는다. `README.md`, Codex/Claude `project-rules.md`, Codex/Claude `validation.md`, `scripts/validate-docs.sh`에 새 명령과 검증 기준을 연결했다.
- 검증: `node scripts/bootstrap-project-root.mjs`, 임시 디렉터리 생성 검증, `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: `node scripts/ensure-entrypoints.mjs`는 루트 진입 문서만 보강하는 경량 명령으로 유지한다.

## 2026-08-19 (DB 접근 시 auto-commit 해제 원칙 추가)

- 목표: 데이터베이스에 접근하는 코드/스크립트가 기본 auto-commit 모드를 쓰지 않고 명시적 트랜잭션으로 커밋/롤백을 제어하도록 저장소 전역 규칙을 추가한다. Claude 세션에서 사용자가 "Database 접근 시에는 auto-commit 해제한 상태로 하도록" 문서화를 요청했고, 저장소 전체 글로벌 규칙으로 Claude/Codex 양쪽에 반영하기로 확인했다.
- 변경: `global/models/codex/domain-rules.md`(및 대응하는 `global/models/claude/domain-rules.md`)에 "데이터베이스 접근 시 트랜잭션 원칙" 절을 추가해 auto-commit 해제, 트랜잭션 경계 직접 제어, 실패 시 롤백, 예외 시 이유 기록 기준을 명시했다. `.codex/agents/database-specialist.toml`(및 `.claude/agents/database-specialist.md`)의 책임 목록에도 이 원칙을 확인하는 항목을 추가했고, `scripts/bootstrap-project-root.mjs`의 두 에이전트 생성 블록도 같은 문구로 맞췄다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`(6 fixture), `node scripts/test-evaluator.mjs`(2 case) 모두 통과.
- 남은 작업: 실제 DB를 쓰는 local 프로젝트가 생기면 해당 `architecture.md`/`domain-policy.md`에 트랜잭션 격리 수준, 커밋 시점, 예외 처리 방식을 구체적으로 기록해야 한다.
- 주의 사항: 이 저장소에는 현재 실제 DB를 쓰는 local 프로젝트가 없어 글로벌 원칙 수준으로만 문서화했다. 이 세션 자체는 Claude에서 진행됐고, 두 모델 문서 세트를 동시에 갱신한 뒤 양쪽 handoff-log에 같은 내용을 기록했다.

## 2026-08-20 (코드 분석 산출물 문서화 규칙 동기화)

- 목표: Claude 쪽에만 반영된 코드 분석 산출물 문서화 규칙을 확인하고 Codex 쪽에도 동일한 공통 정책을 적용한다.
- 변경: `global/models/codex/project-rules.md`에 `코드 분석 산출물` 절을 추가해 코드베이스·아키텍처 분석 결과를 `local/<project-name>/analysis/YYYY-MM-DD-<주제-슬러그>.md`에 남기도록 했다. `global/models/codex/AGENT.md`의 작업 후 규칙에도 이 기준을 연결했다. 공통 `local/README.md`의 선택 구조 안내는 이미 반영돼 있어 중복 수정하지 않았다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: Claude 전용 hook, 도구명, Workflow·서브에이전트 정책은 Codex에 그대로 이식하지 않았다. 분석 문서만 만드는 작업은 로컬 하네스 전체 생성을 강제하지 않는다.

## 2026-08-25 (Codex 리뷰·문서 점검 에이전트 보강)

- 목표: Claude 쪽에 존재하는 `reviewer`/`doc-lint` 예시 서브에이전트와 비교해 Codex 기본 에이전트 세트의 누락을 보완한다.
- 변경: `.codex/agents/reviewer.toml`과 `.codex/agents/doc-lint.toml`을 추가했다. `global/models/codex/model-routing.md`에 코드 리뷰와 문서 하네스 점검 시 두 에이전트를 우선 고려하는 기준, Claude와 함께 쓰는 프로젝트의 `model-routing-map.md` 안내를 추가했다. `scripts/bootstrap-project-root.mjs`와 `scripts/validate-docs.sh`가 새 Codex 에이전트 파일을 생성·검증하도록 갱신했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: Claude 전용 `Workflow`, hook, `AskUserQuestion`, Plan Mode 도구명은 Codex 문서로 옮기지 않았다. Codex의 리뷰 재기획 확인은 여전히 문서 기준의 수동 Reviewer 체크다.
