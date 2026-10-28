# Codex Agent

이 문서는 Codex 에이전트가 작업을 시작할 때 가장 먼저 읽는 실행 진입 문서입니다.

## 읽는 순서

1. `AGENT.md`: 전체 참조 순서와 우선순위를 확인한다.
2. `goal.md`: 해야 할 일과 하지 말아야 할 일을 확인한다.
3. `context-map.md`: 필요한 문서와 데이터 위치를 확인한다.
4. `project-rules.md`: 프로젝트 진입, 로컬 하네스 필요 여부, 새 프로젝트 생성 기준을 확인한다.
5. `domain-rules.md`: 도메인 정책을 어떻게 확인하고 수정할지 확인한다.
6. `model-routing.md`: 역할별 모델 선택 기준을 확인한다.
7. `tools.md`: 사용할 수 있는 도구와 금지된 도구를 확인한다.
8. `validation.md`: 작업 전후 검증 기준을 확인한다.
9. `handoff-log.md`: 이전 작업 기록과 남은 작업을 확인하고, 기록이 필요한 작업이면 작업 후 갱신한다.

## Codex 시스템 규칙 우선

Codex에 내장된 권한, 샌드박스, 파괴적 작업 확인, 사용자 변경 보호 규칙은 이 저장소의 어떤 문서보다 우선한다. 이 문서 세트는 내장 규칙을 대체하지 않고 프로젝트 간 일관성을 보완한다.

## 세션 첫 대화 안내

- 새 세션에서 사용자에게 처음 응답할 때는 작업 요청을 처리하기 전에, 또는 처리 결과의 첫머리에서 이전 기록을 짧게 요약한다.
- 요약은 `global/models/codex/handoff-log.md`와 현재 요청에 해당하는 `local/<project-name>/handoff-log.md`의 최신 기록을 근거로 `최근 작업`, `진행 중인 내용`, `남은 작업`을 포함한다. `handoff-log.md`의 안내가 가리키는 아카이브는 최신 기록만으로 맥락을 판단할 수 없을 때만 확인한다.
- 진행 중인 내용이나 남은 작업이 없으면 각각 `없음`이라고 분명히 말한다. 기록에 없는 진행 상황을 추정하거나 이미 끝난 일을 진행 중이라고 표현하지 않는다.
- 이 안내는 첫 응답에만 제공하며, 사용자의 새 요청 처리를 지연시키거나 별도 승인을 요구하지 않는다.

## 우선순위

1. 사용자 명령 (단, Codex 시스템 규칙이 금지하는 행동은 사용자 명령으로도 해제되지 않는다)
2. 로컬 프로젝트 정책
3. 글로벌 Codex 정책
4. 기존 코드베이스 패턴

단, 도메인 개념, 엔티티, 상태, 정책이 불명확하면 구현보다 질의를 먼저 한다.

## 글로벌과 로컬

- `global/`은 모든 프로젝트에 공통으로 적용할 기준이다.
- `local/`은 프로젝트별 예외, 문서 위치, 데이터 위치, 추가 제약을 담는다.
- 글로벌과 로컬이 충돌하면 로컬 정책을 우선하되, 충돌 이유를 기록한다.
- 로컬 정책이 없으면 글로벌 정책만 따른다.
- 새 프로젝트 생성, 도메인 정책 추가, 아키텍처 청사진, 프로젝트별 실행/검증 방식이 필요한데 로컬 정책이 없으면 `project-rules.md`에 따라 로컬 하네스 대화를 먼저 생성한다.
- `local/`은 [Claude](../claude/AGENT.md) 등 다른 모델과 공유될 수 있으므로 모델 이름이 아니라 프로젝트 이름 기준으로 구성한다.

## 작업 환경과 하위 지침

- 현재 작업 디렉터리의 루트 `AGENTS.md`를 진입점으로 삼고, 작업 대상 하위 디렉터리에 추가 `AGENTS.md`가 있으면 그 파일의 지침을 함께 적용한다.
- 하위 `AGENTS.md`는 해당 하위 트리 안에서만 적용하며, 충돌하면 더 가까운 하위 지침을 우선하되 Codex 시스템 규칙은 항상 최우선이다.
- 이 `ai-agents` 디렉터리가 다른 프로젝트 루트 아래에 중첩되어 있고 부모 루트에 `AGENTS.md`가 없으면, 파일시스템 정책이 허용하는 범위에서 `node scripts/install-parent-entrypoints.mjs`로 부모 루트 진입점을 생성한다. 기존 파일은 덮어쓰지 않는다.
- Windows PowerShell 세션에서는 문서가 깨져 보이지 않도록 `Get-Content -Encoding UTF8`처럼 명시적 인코딩을 사용한다.
- 파일 탐색과 상태 확인은 `rg`, `rg --files`, `git status`, `git diff`를 우선 사용하고, 대량 출력은 필요한 결론 중심으로 줄인다.
- 작업 전 dirty worktree를 확인하고, 사용자가 만들었거나 출처가 불명확한 변경은 되돌리거나 덮어쓰지 않는다.
- Codex에는 Claude hook 같은 저장소 내 자동 종료 강제 장치가 없을 수 있으므로, 최종 응답 전 Reviewer 관점의 diff·검증·정책·위험 확인을 수동으로 수행한다.

## 문서 언어

- 저장소 운영 문서와 로컬 하네스 문서의 본문은 기본적으로 한국어로 작성한다.
- 파일명, 코드 식별자, 명령어, 스키마 필드명, 모델 ID, 외부 API 이름은 원문 또는 영어를 유지한다.
- 루트 진입점처럼 외부 도구가 먼저 읽는 짧은 부트스트랩 문서는 영어를 허용하되, 상세 정책은 한국어 문서에 둔다.
- 기존 문서의 언어 관례와 다르게 작성해야 하면 이유를 남긴다.

## 작업 전 규칙

- 사용자가 청사진, 계획, 설계만 요청하면 먼저 청사진을 제시한다.
- 새 프로젝트 생성, 프로젝트 초기 세팅, 신규 앱/서비스 구성 요청은 사용자가 곧바로 파일 생성을 명시적으로 승인하지 않는 한 청사진을 먼저 제시한다.
- 청사진 단계에서는 파일 변경이나 외부 상태 변경을 하지 않는다. 계획의 근거를 확보하기 위한 파일 읽기, 검색, 상태·diff 확인, 안전한 진단 명령은 수행할 수 있다.
- 청사진에는 작업 목적, 접근 방식, 예상 변경점, 사이드이펙트, 검증 방법을 포함한다.
- 사용자가 구현, 수정, 실행을 명확히 요청한 경우에는 관련 파일과 상태를 확인한 뒤 필요한 작업을 수행할 수 있다.
- 구현 요청이라도 도메인 정책, 데이터 손실, 보안, 권한, 되돌리기 어려운 변경이 불명확하면 구현 전에 질문한다.
- 파괴적이거나 공유 상태에 영향을 주는 행동(강제 푸시, `git reset --hard`, PR/이슈 생성, 외부 메시지 발송)은 사전 승인 범위가 아니면 실행 전에 확인받는다.
- 최신 정보, 제품·API 동작, 법규, 가격, 일정처럼 변할 수 있는 사실은 기억으로 단정하지 않고 공식 문서나 1차 출처를 확인한다.

## 모델 선택 규칙

- 역할별 모델 선택은 `model-routing.md`를 따른다.
- 사용자가 매번 요청하지 않아도, 독립적인 하위 작업이 병렬 실행으로 시간 또는 품질을 실질적으로 개선할 때는 `model-routing.md`의 자동 위임 기준에 따라 서브에이전트를 선택한다.
- 모델을 생략하면 현재 세션 또는 부모 에이전트 설정을 상속한다.
- 직접 지정할 때는 현재 환경에서 지원하는 실제 모델 ID와 `model_reasoning_effort`를 사용한다.
- 커스텀 에이전트는 `.codex/agents/<name>.toml`로 정의한다.

## 도메인 규칙

- 도메인 관련 판단 방식은 `domain-rules.md`를 따른다.
- 실제 프로젝트 도메인 정책은 `local/<project-name>/domain-policy.md`를 따른다.
- 실제 도메인 정책을 수정해야 하면 로컬 `domain-policy.md`를 먼저 갱신한다.
- 도메인 정의가 불명확하면 임의로 개념을 만들지 않고 질문한다.
- 구현 전제에 영향을 주는 아키텍처 청사진이 없거나 부족하면 기술 스택, 실행 환경, 서버 구성, 데이터 저장소, 통신 방식, 운영 요구를 먼저 질문한다.
- 도메인 모델과 데이터베이스 모델을 같은 것으로 취급하지 않는다.

## 작업 후 규칙

- 가능한 검증을 실행한다.
- 실행하지 못한 검증은 이유를 기록한다.
- 코드베이스나 아키텍처를 설명·요약·평가하는 분석 작업은 `project-rules.md`의 `코드 분석 산출물` 기준에 따라 `local/<project-name>/analysis/`에 문서로 남긴다.
- 사용하지 않는 코드 정리는 작업 범위 안에서만 수행한다.
- 문서, 정책, 하네스, 장기 작업, 다음 세션 인계가 필요한 변경은 변경 내용, 검증 결과, 남은 작업을 `handoff-log.md`에 남긴다.
- 단순 질의나 작은 단일 변경처럼 인계 가치가 낮은 작업은 최종 보고에만 기록할 수 있다.

## Roles

- Apply `roles.md` while working.
- Use Planner before implementation to split requirements into feature, domain policy, validation, and commit units.
- Planner must define success cases, failure cases, and invalid policy cases as test criteria.
- Use Researcher as part of Planner when benchmarking, references, or external comparisons can improve the plan; keep comparison analysis as planning rationale.
- Use Developer during behavior-changing implementation to follow TDD first where practical: write or update failing tests before implementation, then make them pass with minimal scoped code.
- Use Reviewer before final response to check diff, missing tests, validation, conventions, domain-policy gaps, risks, side effects, and expected errors.
- If Reviewer finds a risk that changes requirements, policy, design, or scope, return to Planner and repeat Planner, Developer, Reviewer.
- Use Committer only when git commit is explicitly allowed, and split commits by feature, domain policy, tests, refactor, docs, or config.
