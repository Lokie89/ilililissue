# 기록 아카이브

`handoff-log.md`의 최근 기록 보관 한도를 넘긴 이전 세션 기록을 오래된 순서로 보관한다.

## 2026-08-07

- 목표: 저장소에 이미 있는 [Codex 모델](../codex/README.md) 문서 세트와 같은 구조로, Claude Code 하네스(Plan Mode, 서브에이전트, Skill, Workflow, 권한 프롬프트)에 맞춘 Claude 모델 문서 세트를 만든다.
- 변경: `global/models/claude/`에 `README.md`, `AGENT.md`, `goal.md`, `context-map.md`, `project-rules.md`, `domain-rules.md`, `model-routing.md`, `tools.md`, `validation.md`, `handoff-log.md`, `roles.md`와 `harness/`(README, state-machine, failure-cases, output-schema, fixtures, evaluation-rubric)를 추가했다. Codex의 청사진/도메인/보이스카웃 원칙은 그대로 유지하되, 청사진 단계는 `EnterPlanMode`/`ExitPlanMode`(Plan Mode)로, 모델 선택 별칭은 실제 Claude 별칭(`haiku`/`sonnet`/`opus`/`fable`/`inherit`)과 추론 강도(`low`~`max`) 두 축으로, 역할 구성은 Codex의 Planner/Researcher/Developer/Reviewer/Committer에 `Agent`/`Workflow` 위임을 다루는 Orchestrator 역할을 추가해 대응시켰다. `Workflow` 도구는 사용자의 명시적 opt-in이 있을 때만 사용하도록 모델 라우팅과 검증 기준에 명시했다. 루트 `README.md`에 Claude 모델 링크를 추가하고, `scripts/validate-docs.sh`에 Claude 문서 세트에 대한 파일 존재 및 핵심 참조 검사를 추가했다.
- 검증: `bash scripts/validate-docs.sh` 실행 결과를 확인한다.
- 남은 작업: 실제 Claude 서브에이전트 정의(`.claude/agents/*.md`)나 프로젝트별 사용 예시를 `local/`에 추가할 수 있다. 필요하면 Codex와 Claude 별칭 간 로컬 매핑 예시 파일을 별도로 추가할 수 있다. Codex 쪽 `handoff-log.md`에는 아직 아카이브 규칙을 적용하지 않았으므로, 필요하면 동일한 방식으로 옮길 수 있다.
- 주의 사항: 이 문서 세트는 Claude Code에 내장된 권한 프롬프트, 파괴적 작업 확인, hook, Plan Mode 동작을 대체하지 않고 보완한다. 충돌 시 Claude Code 내장 규칙이 항상 우선한다.

같은 세션 후속 작업: 콜드 스타트(다른 PC에서 새 세션 시작) 시 토큰 소비를 줄이기 위해 아래를 추가했다.

- 변경: 루트에 `CLAUDE.md`를 추가해 세션 시작 시 `global/models/claude/AGENT.md`를 바로 가리키도록 했다(매번 구조를 탐색해서 찾지 않도록). `handoff-log.md`(이 파일, `local/_template/handoff-log.md`, `local/sample-project/handoff-log.md`) 맨 위에 "아카이브 안내" 섹션을 추가해 `현재 기록`은 최근 5개만 남기고 나머지는 `handoff-archive.md`로 옮기도록 했으며, 문서를 열자마자 아카이브 파일의 존재를 인지하도록 안내 문구를 최상단에 배치했다. `project-rules.md`와 `validation.md`에 이 아카이브 규칙을 체크 항목으로 추가했다. `context-map.md`에는 넓은 탐색을 `Explore` 서브에이전트에 위임하는 이유(메인 세션 컨텍스트를 작게 유지)를 명시했고, `local/_template/context-map.md`는 디렉터리 대신 구체적 진입점 파일 경로를 적도록 안내 문구를 바꿨다. `scripts/validate-docs.sh`에 이 변경들에 대한 검사를 추가했다.
- 검증: `bash scripts/validate-docs.sh` 통과 확인.
- 남은 작업: `handoff-archive.md`는 실제로 5개를 초과하는 시점에 생성하면 된다(현재는 아직 만들 필요 없음). Codex 쪽 문서에도 같은 아카이브 규칙과 CLAUDE.md 상당 진입점을 적용할지는 별도 논의가 필요하다.
- 주의 사항: `CLAUDE.md`는 세션마다 자동으로 로드되므로 내용을 최소한으로 유지해야 한다. 항목을 추가할 때마다 이 파일이 커지지 않도록 주의한다.

## 2026-08-10

- 목표: Codex 문서가 2026-08-09에 먼저 현행화된 내용(`fae1bb8`, `df5faaf`, `de5fb7b`) 중 Claude 문서에도 반영할 게 있는지 대조하고 반영한다.
- 변경: (1) `EnterPlanMode` 툴이 실제로 Plan Mode에서 `Glob`/`Grep`/`Read` 탐색을 허용한다는 사실에 맞춰, `AGENT.md`/`goal.md`/`harness/evaluation-rubric.md`/`harness/fixtures.md`/`harness/failure-cases.md`/`validation.md`의 청사진 단계 서술을 "명령 실행을 전혀 하지 않는다"에서 "파일·외부 상태 변경은 하지 않되 계획 근거 확보를 위한 읽기 전용 조사는 허용한다"로 정정했다. (2) 루트 `CLAUDE.md`에 Codex `AGENTS.md`와 동일한 "진입점을 간결하게 유지하고 서브트리 예외는 중첩 `CLAUDE.md`로 둔다" 원칙을 추가했다. (3) `model-routing.md`에 병렬 서브에이전트가 같은 파일을 동시에 쓰지 않도록 하는 절과, `Agent`/`Workflow`의 `isolation: "worktree"` 사용 기준을 추가하고 `tools.md`에도 대응 문구를 추가했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs` 모두 통과.
- 남은 작업: Codex 쪽 `model-routing.md`가 실제 모델 ID/`.codex/agents/*.toml` 기준으로 크게 개편됐지만, 이는 Codex 고유의 설정 방식(가상의 별칭 사용 문제)을 고친 것이라 Claude에는 적용하지 않았다(Claude의 별칭 `haiku`/`sonnet`/`opus`/`fable`은 실제 런타임 별칭이라 문제가 없었음).
- 주의 사항: 앞으로 Codex/Claude 중 한쪽 문서를 고칠 때는 반대쪽에도 같은 종류의 부정확성이 있는지 확인하는 습관을 들인다.

## 2026-08-10 (문서 하네스 보강)

- 목표: 문서 리뷰에서 나온 개선 후보(로컬 샘플 누락, CI 미연동, 서브에이전트/모델 매핑 예시 부재, 구조화 fixture 커버리지, 서브트리 오버라이드 예시 부재)를 모두 반영한다.
- 변경: `local/sample-project/tools.md`를 추가해 로컬 하네스 필수 파일 6개를 채웠다. `.github/workflows/validate-docs.yml`을 추가해 `validate-docs.sh`/`validate-harness.mjs`/`test-evaluator.mjs`를 push/PR에서 자동 실행하도록 했다(README에도 명시). `.claude/agents/reviewer.md`, `.claude/agents/doc-lint.md` 예시 서브에이전트를 추가하고 `model-routing.md`/`tools.md`에서 참조했다. `local/_template/model-routing-map.md`(템플릿)와 `local/sample-project/model-routing-map.md`(예시)를 추가해 Codex/Claude를 함께 쓰는 프로젝트의 모델 매핑 방법을 보여주고, 이 과정에서 `model-routing.md`에 남아 있던 stale한 Codex 별칭(`fast/balanced/deep/max`, 2026-08-09 Codex 개편에서 이미 폐기됨) 참조도 함께 고쳤다. `global/harness/fixtures/core.json`에 `review`/`project_bootstrap` 카테고리 fixture 2개를 추가해 스키마가 정의한 카테고리 커버리지를 넓혔다. `local/sample-project/legacy-module/`에 중첩 `CLAUDE.md`/`AGENTS.md` 서브트리 오버라이드 예시를 추가하고 `goal.md`/`context-map.md`/`local/README.md`에서 연결했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`(6 fixture 통과), `node scripts/test-evaluator.mjs` 모두 통과.
- 남은 작업: `.claude/agents/*.md`와 `local/*/model-routing-map.md`는 실제 사용 예시가 쌓이면 내용을 더 구체화할 수 있다. 구조화 fixture는 여전히 prose fixture(Claude 11개)보다 적으므로 필요하면 계속 늘릴 수 있다.
- 주의 사항: `local/sample-project/`는 실행 코드가 없는 문서 전용 샘플이라는 전제를 유지해야 한다. `legacy-module/`의 오버라이드 내용은 가상 예시이며 실제 정책이 아니다.

## 2026-08-11 (Stop hook으로 review→plan 루프 강제)

- 목표: `AGENT.md`/`roles.md`/`state-machine.md`에 문서로만 존재하던 "Reviewer가 재기획 필요 위험을 발견하면 Planner로 되돌아간다" 규칙이 실제로는 아무 것도 강제하지 않는 산문 규범이라는 점을 사용자와 확인하고, 위험 신호가 있는 세션에서만 이를 강제하는 Stop hook을 추가한다.
- 변경: `.claude/hooks/review-replan-check.sh`(git diff/status 기준으로 위험 신호를 저비용 패턴 매칭하고, 감지되면 transcript에서 `REVIEW-CHECK:` 마커 존재를 확인)와 `.claude/settings.json`(Stop hook 등록, 팀 공유 파일로 결정)을 새로 추가했다. 위험 신호가 없으면 즉시 종료해 일반 세션에는 토큰 비용이 들지 않고, 감지되면 응답 종료를 막고 `REVIEW-CHECK: 재기획 불필요 - <근거>` 또는 `REVIEW-CHECK: 재기획 필요 - Planner로 복귀 - <근거>` 형식의 명시적 확인을 요구한다. `stop_hook_active`를 확인해 무한 루프를 방지했다.
- 검증: 위험 없음/위험+마커 없음/위험+마커 있음/`stop_hook_active=true` 4가지 시나리오를 합성 stdin으로 직접 pipe-test했고 모두 기대대로 동작했다(각 테스트 후 임시 파일 정리 확인). `jq -e`로 `settings.json`의 훅 스키마도 확인했다. `Stop` 이벤트 자체는 이번 대화 턴 밖에서 발생하므로 실제 세션에서 훅이 발화하는 것은 증명하지 못했다.
- 남은 작업: 다음 세션에서 실제로 위험 신호가 있는 변경을 만들고 응답을 끝낼 때 훅이 실제로 걸리는지 확인이 필요하다. `.claude/settings.json`이 이번 세션 시작 후 처음 생긴 파일이라 설정 감시가 바로 걸리지 않으면 `/hooks`를 한 번 열거나 재시작해야 할 수 있다. `RISK_PATH_RE`/`RISK_KEYWORD_RE` 패턴은 이 저장소에서 실제 위험 사례가 쌓이면 넓히거나 좁힐 수 있다.
- 주의 사항: 이 훅은 review→plan 전이가 실제로 일어났는지까지 검증하지 못한다 — Claude가 `REVIEW-CHECK:` 마커만 붙이고 실제로는 재기획을 안 해도 훅은 통과시킨다. 판단의 정직성까지는 강제할 수 없는 구조적 한계로 남긴다.

같은 세션 후속 작업: 커밋 후 사용자가 "새 세션이 문서만 읽고 들어와도 이 훅의 존재를 알 수 있는가"를 물어서 확인한 결과, `tools.md`/`validation.md`에는 이 훅에 대한 언급이 전혀 없어 유일한 기록이 `handoff-log.md`뿐이었다(아카이브되면 사라짐). `tools.md`에 "자동 강제 장치" 섹션을 추가해 훅의 존재, 트리거 조건, `REVIEW-CHECK:` 형식, Codex에는 적용되지 않는다는 점을 명시했고, `validation.md` 기본 체크리스트에도 대응 항목을 추가했다. `bash scripts/validate-docs.sh` 통과 확인. 남은 갭: Codex는 이 메커니즘의 영향권 밖이며(Codex에는 Claude Code의 hook 시스템이 없음), `roles.md`/`codex/roles.md` 양쪽에 동일하게 있는 "Reviewer가 재기획 필요 위험을 찾으면 Planner로 돌아간다" 규칙은 Codex 쪽에서는 여전히 순수 산문 규범으로만 남아 있다. 이 비대칭을 Codex 쪽 문서에 알릴지는 사용자가 보류했다.

## 2026-08-11 (숙의형 멀티 에이전트 패턴 연결)

- 목표: 공통 하네스에 추가된 숙의형 멀티 에이전트 패턴을 Claude 라우팅과 검증 기준에도 연결한다.
- 변경: `global/harness/deliberation.md`를 공통 패턴으로 두고, Claude `model-routing.md`의 Workflow 사용 기준과 `validation.md`의 숙의형 멀티 에이전트 검증 기준에서 참조했다. 모델별 실패 케이스에 라운드 미구분, 단순 다수결 판정, 라운드별 요약 로그 누락을 위반으로 추가했다.
- 검증: Codex 세션에서 `bash scripts/validate-docs.sh`, 공통 Node 검증, PowerShell 문구 확인, `git diff --check`가 통과했다. 기존 Windows CRLF 줄끝 문제는 `scripts/validate-docs.sh`를 LF로 정리해 해결했고, 라운드별 요약 로그 문구도 문서 검증 스크립트에 추가했다.
- 남은 작업: 실제 Claude Workflow로 구현할 때는 opt-in 조건과 세션 workflow size guideline을 별도로 확인해야 한다.
- 주의 사항: Claude의 Workflow opt-in 원칙은 유지한다. 숙의형 패턴은 Workflow 사용을 자동 허용하지 않고, 사용자가 명시적으로 오케스트레이션을 원할 때 적용한다.

같은 문서, 다른 세션 후속 작업: 사용자가 "Java Spring Boot 아키텍처" 질문으로 숙의형 패턴을 실제로 시켜봤는데, `Workflow` opt-in 표현을 전혀 쓰지 않았음에도 `Agent` 도구만으로(proposer 3개 병렬 독립분석 → critic 1개 → 직접 synthesis/verification) deliberation.md의 라운드 구조를 그대로 재현해 잘 작동했다. 그런데 `model-routing.md` 133~134행이 "Workflow(멀티 에이전트 오케스트레이션) 사용 기준" 섹션 안, opt-in 규칙 바로 다음에 붙어 있어서 "숙의형 패턴 = Workflow 하위 항목 = opt-in 필요"로 잘못 읽힐 여지가 있었다. `model-routing.md`에 "숙의형 패턴 자체는 Workflow opt-in의 하위 항목이 아니다. 규모가 작으면(proposer/critic/synthesizer) opt-in 없이 `Agent`만으로 라운드를 직접 진행해도 되고, 라운드를 스크립트로 강제해야 하거나 규모가 커지면 그때 opt-in을 확인하고 `Workflow`로 승격한다"는 문장을 추가해 두 실행 경로를 명시했다. `bash scripts/validate-docs.sh` 통과 확인. 이 실행 경로 구분은 `validation.md`의 "숙의형 멀티 에이전트 검증" 절에는 원래도 Workflow 종속 서술이 없어 추가 수정은 하지 않았다.

## 2026-08-12 (로컬 아키텍처 청사진 추가)

- 목표: 프로젝트별 로컬 하네스가 도메인 정책뿐 아니라 아키텍처 청사진도 먼저 확인하도록 기준을 확장한다.
- 변경: `local/_template/architecture.md`와 `local/sample-project/architecture.md`를 추가했다. Codex/Claude 프로젝트 규칙, 목표, AGENT, 컨텍스트 지도, 검증, 하네스 산출물, fixture, 실패 케이스에 `architecture.md` 확인과 청사진 질문 기준을 연결했다. `local/README.md`, 샘플 context-map, 루트 README, 문서 검증 스크립트도 새 필수 문서에 맞췄다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 실제 프로젝트에 적용하면서 아키텍처 질문 항목이 과하거나 부족한지 사례를 보고 조정할 수 있다.
- 주의 사항: 아키텍처는 현재 기준의 청사진이며 모든 항목을 한 번에 확정하지 않는다. 구현 전제에 직접 영향을 주는 항목만 질문으로 승격하고, 나머지는 `미정` 또는 `해당 없음`으로 기록한다.

## 2026-08-12 (Codex 운영 기준 보완 대조)

- 목표: Codex 쪽 `7a00434`(하위 `AGENTS.md`, PowerShell 인코딩, dirty worktree 보호, 최신 정보 확인, hook 부재 시 수동 Reviewer 체크 보완) 커밋을 Claude 문서와 대조해 반영할 게 있는지 확인한다.
- 변경: 대부분은 이미 Claude 쪽에 동등하거나 더 강한 형태로 있었다(중첩 `CLAUDE.md`는 Claude Code가 네이티브로 자동 로드하므로 별도 규칙 불필요, `Read` 툴이 인코딩을 처리, `사용자 변경 보호 위반`/`최신 정보 사용 위반` 실패 케이스 기존 존재, Codex에는 없는 Stop hook을 Claude는 이미 보유). 실제로 빠진 항목 2개만 반영했다: `harness/failure-cases.md`의 "사용자 변경 보호 위반"에 "커밋이나 스테이징을 수행하면서 현재 작업과 무관한 변경을 포함했다"를 추가했고, `validation.md`의 "결과 보고 형식"에 "최신 정보나 외부 사실을 확인했다면 사용한 공식 문서 또는 1차 출처"를 추가했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`(6 fixture 통과), `node scripts/test-evaluator.mjs`(2 케이스 통과), `git diff --check` 모두 통과.
- 남은 작업: 없음.
- 주의 사항: Codex와 Claude는 실행 환경이 달라(네이티브 중첩 CLAUDE.md 로드, Stop hook 존재, Bash 툴이 Git Bash를 문제없이 사용) 모든 항목을 1:1로 옮길 필요는 없다. 대조할 때마다 "Codex에 없는 기능을 보완한 항목"과 "Codex의 샌드박스/환경 제약 때문에 추가된 항목"을 구분해서 판단해야 한다.

## 2026-08-12 (프로젝트 시작 청사진 선행 강화)

- 목표: 새 프로젝트 생성이나 초기 세팅 요청에서 파일 생성보다 청사진 제시가 먼저 일어나도록 하네스 기준을 강화한다.
- 변경: Codex/Claude `AGENT.md`, `goal.md`, `project-rules.md`, `context-map.md`, 상태 머신, 평가 루브릭, fixture, 실패 케이스, 산출물 스키마, 검증 문서에 프로젝트 bootstrap의 blueprint-first 규칙을 추가했다. 구조화 fixture `project_bootstrap_customer_support`도 `writes_allowed: false`로 바꿔 청사진 승인 전 파일 생성이 실패로 판정되게 했다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 실제 새 프로젝트 시작 세션에서 agent가 청사진 후 승인 대기하는지 사례로 확인할 수 있다.
- 주의 사항: 사용자가 "바로 파일 생성해", "청사진 생략하고 구현해"처럼 명시적으로 승인한 경우에는 구현으로 넘어갈 수 있지만, 기본값은 청사진 선행이다.

같은 문서, 다른 세션 후속 작업: 사용자가 Codex 세션에서 만든 이 커밋의 Claude 쪽 내용을 검토해달라고 요청해 확인했다. `AGENT.md`/`goal.md`/`project-rules.md`/`context-map.md`/`state-machine.md`/`evaluation-rubric.md`/`output-schema.md`/`validation.md`/`handoff-archive.md` 변경은 Claude 도구(`EnterPlanMode`/`ExitPlanMode`/`AskUserQuestion`/`TaskCreate`) 기준과 일치했고 상태 전이에도 모순이 없었다. 다만 `harness/fixtures.md`의 Fixture 10 "실패 판정"에 이전 버전(승인 없이도 바로 구현하던 시절) 문구 "프로젝트를 만들었지만 실행 명령이나 동작하는 핵심 흐름을 보고하지 않았다"가 그대로 남아 있었다 — 이 fixture의 새 기대 행동은 승인 전 파일 생성을 금지하므로 "만든 프로젝트"가 있을 수 없어 자기 모순이었다. "청사진에 예상 핵심 흐름(문의 등록, 조회, 상태 변경)이나 실행/검증 명령 후보가 없다"로 고쳤다. `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check` 모두 통과 확인. 같은 leftover 문구가 `global/models/codex/harness/fixtures.md`의 동일 Fixture 10(216행)에도 남아 있어, 사용자 확인 후 같은 문장으로 함께 고쳤다.

## 2026-08-12 (문서 언어 정책 추가)

- 목표: 저장소 운영 문서와 로컬 하네스 문서의 본문 언어 기준을 명시한다.
- 변경: Codex/Claude `AGENT.md`에 문서 언어 섹션을 추가해 본문은 기본적으로 한국어, 파일명·코드 식별자·명령어·스키마 필드명·모델 ID·외부 API 이름은 원문 또는 영어 유지 기준을 정의했다. 양쪽 `project-rules.md`, `validation.md`, `local/README.md`, 문서 검증 스크립트에도 같은 기준을 연결했다.
- 검증: `bash scripts/validate-docs.sh`, `git diff --check`를 실행한다.
- 남은 작업: 실제 프로젝트별 로컬 문서에서 영어 본문이 필요한 경우 예외 이유를 남기는 관례를 적용한다.
- 주의 사항: 루트 `AGENTS.md`/`CLAUDE.md` 같은 짧은 부트스트랩 문서는 영어를 허용하고, 상세 정책은 한국어 문서에 둔다.

## 2026-08-13 (테스트 코드 문서성 명시)

- 목표: 테스트 코드가 기능 구현 시 자주 참조해야 하는 문서 역할도 한다는 기준을 명시한다.
- 변경: Codex/Claude `roles.md`의 Developer 기준에 "테스트 코드도 문서"라는 원칙과 요구사항, 사용 예, 경계 조건 참고 자료로 활용해야 한다는 문장을 추가했다.
- 검증: Codex 세션에서 `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: 테스트를 문서로 보되, 실제 제품 문서나 도메인 정책을 대체하는 것으로 취급하지 않는다.

## 2026-08-13 (기본 토론 에이전트 기준 연결)

- 목표: 하네스와 에이전트 운영 정책 토론 결과를 durable 문서에 남기는 기준을 Claude 문서에도 연결한다.
- 변경: 공통 `global/harness/deliberation.md`에 토론 후 문서화 기준을 추가하고, Codex/Claude 검증·실패 케이스·산출물 스키마에 같은 기준을 연결했다. Codex 쪽에는 하네스 토론 에이전트와 제품 기획자, UX/UI 디자이너, 프론트엔드 개발자, 백엔드 개발자, DB 전문가, 테스터 커스텀 에이전트를 추가했다.
- 검증: Codex 세션에서 `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, TOML 문법 검사, `git diff --check`를 실행한다.
- 남은 작업: Claude 전용 서브에이전트가 필요하면 별도 `.claude/agents/` 정의를 추가할 수 있다.
- 주의 사항: 이번 변경은 Claude Workflow opt-in 규칙을 바꾸지 않는다.

## 2026-08-13 (프로젝트 루트 부트스트랩 확장)

- 목표: 이 문서 세트를 다른 프로젝트에 복사했을 때 루트 진입 문서와 기본 Codex 에이전트 세트를 자동으로 보강할 수 있게 한다.
- 변경: `scripts/bootstrap-project-root.mjs`를 추가해 `AGENTS.md`, `CLAUDE.md`, `.codex/agents/*.toml` 기본 세트를 생성하도록 했다. 기존 파일은 덮어쓰지 않는다. `README.md`, Codex/Claude `project-rules.md`, Codex/Claude `validation.md`, `scripts/validate-docs.sh`에 새 명령과 검증 기준을 연결했다.
- 검증: Codex 세션에서 `node scripts/bootstrap-project-root.mjs`, 임시 디렉터리 생성 검증, `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: Claude 전용 `.claude/agents/` 생성은 포함하지 않았다.

## 2026-08-14 (Claude 기본 토론 에이전트 세트 추가)

- 목표: Codex에만 있던 기본 토론 서브에이전트 7종(`.codex/agents/*.toml`)과 짝을 맞춰 Claude 쪽 모델 병렬 대응 계약을 채운다. 이전 세션 기록의 "남은 작업"이었다.
- 변경: `.claude/agents/harness-deliberator.md`, `product-planner.md`, `ux-ui-designer.md`, `frontend-developer.md`, `backend-developer.md`, `database-specialist.md`, `product-tester.md`를 추가했다. `global/models/claude/model-routing.md`에 각 에이전트의 사용 기준과 모델/추론 강도를 연결하고, Codex 기본 세트와 역할이 대응하며 한쪽만 갱신하지 않는다는 문구를 추가했다. `scripts/bootstrap-project-root.mjs`와 `scripts/validate-docs.sh`가 이 파일들도 생성/검증하도록 갱신했고, `README.md`와 양쪽 `project-rules.md`의 안내 문구도 맞췄다. (커밋 `7a71646`)
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check` 모두 통과.
- 남은 작업: 없음.
- 주의 사항: 이 기록은 커밋 당시 남기지 못해 뒤늦게 추가했다. `doc-lint.md`, `reviewer.md` 서브에이전트는 이보다 앞선 커밋(`5747b32`)에서 이미 존재했으므로 이번 변경 범위에 포함하지 않았다.

## 2026-08-19 (DB 접근 시 auto-commit 해제 원칙 추가)

- 목표: 데이터베이스에 접근하는 코드/스크립트가 기본 auto-commit 모드를 쓰지 않고 명시적 트랜잭션으로 커밋/롤백을 제어하도록 저장소 전역 규칙을 추가한다. 사용자가 "Database 접근 시에는 auto-commit 해제한 상태로 하도록" 문서화를 요청했고, 저장소 전체 글로벌 규칙으로 Claude/Codex 양쪽에 반영하기로 확인했다.
- 변경: `global/models/claude/domain-rules.md`와 `global/models/codex/domain-rules.md`에 "데이터베이스 접근 시 트랜잭션 원칙" 절을 추가해 auto-commit 해제, 트랜잭션 경계 직접 제어, 실패 시 롤백, 예외 시 이유 기록 기준을 명시했다. `.claude/agents/database-specialist.md`와 `.codex/agents/database-specialist.toml`의 책임 목록에도 이 원칙을 확인하는 항목을 추가했고, `scripts/bootstrap-project-root.mjs`의 동일 에이전트 생성 블록도 같은 문구로 맞췄다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`(6 fixture), `node scripts/test-evaluator.mjs`(2 case) 모두 통과.
- 남은 작업: 실제 DB를 쓰는 local 프로젝트가 생기면 해당 `architecture.md`/`domain-policy.md`에 트랜잭션 격리 수준, 커밋 시점, 예외 처리 방식을 구체적으로 기록해야 한다.
- 주의 사항: 이 저장소에는 현재 실제 DB를 쓰는 local 프로젝트가 없어 글로벌 원칙 수준으로만 문서화했다. 코드 구현이나 실제 DB 연결 검증은 하지 않았다.
