# 모델 라우팅

이 문서는 Claude 에이전트가 역할에 맞는 모델 별칭, 추론 강도(reasoning effort), 서브에이전트를 선택하는 기준입니다.

## 기본 원칙

- 모델과 추론 강도는 별개 축이다. 모델은 어떤 가중치를 쓸지, 추론 강도는 얼마나 깊게 생각할지를 결정한다.
- 모델 지정은 별칭(`haiku`, `sonnet`, `opus`, `fable`), 풀 모델 ID, `inherit` 중 하나를 쓴다.
- 모델을 생략하면 `inherit`로 간주하고 부모 세션 또는 메인 루프 모델을 따른다. `Agent`/`Workflow`의 `model` 옵션도 동일하게 생략을 기본값으로 둔다.
- 계정, 제품 표면, 시점에 따라 실제 사용 가능한 모델이 달라질 수 있으므로 풀 모델 ID는 필요할 때만 고정한다.
- 서브에이전트 frontmatter에는 `type` 필드를 사용하지 않는다.

## 모델 지정 방식

| 방식 | 의미 | 사용 기준 |
| --- | --- | --- |
| 별칭 | 역할별 의도를 표현하는 추상 이름 | 글로벌 규칙과 템플릿 |
| 풀 모델 ID | 실제 실행할 모델 ID | 로컬 프로젝트나 확정된 설정 |
| `inherit` | 부모 세션 또는 메인 루프 모델 상속 | 기본값, 모델 고정이 필요 없는 작업 |

## 별칭

| 별칭 | 용도 |
| --- | --- |
| `haiku` | 빠른 탐색, 문서 스캔, 단순 정리, 반복 작업 |
| `sonnet` | 일반 구현, 문서 수정, 보통 수준의 판단(대부분 세션의 기본 모델) |
| `opus` | 설계, 도메인 정책, 복잡한 디버깅, 리뷰, 높은 위험도 검토 |
| `fable` | 서술형 콘텐츠, 시나리오 기반 대화형 작업 |
| `inherit` | 부모 세션 또는 메인 루프 모델 상속 |

실제 모델 ID와 별칭 매핑은 계정, 제품 표면, 시점에 따라 달라질 수 있으므로 로컬 설정에서 확정한다.

## 추론 강도(reasoning effort)

| 강도 | 용도 |
| --- | --- |
| `low` | 기계적 변환, 단순 집계, 반복 작업 |
| `medium` | 일반 구현, 표준 리뷰, 대부분의 세션 기본값 |
| `high` | 설계 판단, 도메인 정책, 복잡한 디버깅, 리뷰 |
| `xhigh` | 보안, 데이터 손실 위험, 되돌리기 어려운 변경 전 검토 |
| `max` | 가장 어려운 검증/판단 단계, 여러 결과를 종합하는 최종 판단 |

## 선택 루브릭

모델은 먼저 역할로 기본값을 정하고, 작업 조건에 따라 승격하거나 강등한다.

### `haiku`

다음 조건을 대부분 만족하면 `haiku`를 선택한다.

- 파일 탐색, 구조 요약, 단순 검색처럼 읽기 중심 작업이다.
- 코드나 문서를 수정하지 않는다.
- 도메인 정책, 보안, 데이터 손실 판단이 없다.
- 실패해도 사이드이펙트가 거의 없다.
- 결과를 상위 에이전트가 다시 검토한다.

### `sonnet`

다음 조건을 대부분 만족하면 `sonnet`을 선택한다.

- 승인된 구현이나 문서 수정처럼 범위가 명확하다.
- 변경 파일 수가 적거나 영향 범위가 제한적이다.
- 기존 패턴을 따르면 충분하고 새 설계 판단이 작다.
- 테스트, 빌드, 린트 등으로 결과를 검증할 수 있다.
- 보안, 권한, 결제, 데이터 삭제, 마이그레이션 위험이 낮다.

### `opus`

다음 조건 중 하나라도 중요하면 `opus`로 승격한다.

- 청사진, 아키텍처, 변경 범위, 사이드이펙트 판단이 필요하다.
- 새 도메인 개념, 엔티티, 상태, 정책이 추가된다.
- 여러 파일이나 모듈에 걸친 변경이다.
- 실패 경로, 경계 조건, 회귀 가능성을 따져야 한다.
- 테스트 전략 자체를 설계해야 한다.
- 리뷰, 유지보수성 평가, 복잡한 디버깅이 필요하다.

### `opus` + `xhigh`/`max`

다음 조건 중 하나라도 있으면 추론 강도를 `xhigh` 또는 `max`로 올리거나 사람 승인 전 검토 단계로 둔다.

- 보안, 인증, 권한, 개인정보, 비밀값, 데이터 노출과 관련된다.
- 결제, 정산, 재무, 법무, 의료 등 고위험 도메인이다.
- 데이터 삭제, 마이그레이션, 스키마 변경처럼 되돌리기 어려운 작업이다.
- 운영 장애, 성능 저하, 대규모 배포 영향이 예상된다.
- `Workflow`로 장시간 다단계 작업을 오케스트레이션하거나 여러 서브에이전트 결과를 합쳐야 한다.
- 중요한 의사결정 전에 독립적인 검토가 필요하다.

## 승격과 강등

- 낮은 별칭에서 시작했더라도 도메인 불명확성, 보안 위험, 되돌리기 어려운 변경이 발견되면 즉시 상위 별칭이나 추론 강도로 승격한다.
- 상위 별칭으로 시작했더라도 작업이 단순 검색, 정리, 반복 작업으로 분해되면 `haiku` 서브에이전트에 위임할 수 있다.
- `xhigh`/`max`가 필요한 작업은 가능하면 실행 서브에이전트와 검토 서브에이전트를 분리한다(예: `Agent`로 구현 후 별도 검토 호출, `ReportFindings`로 결과 구조화).
- 모델 선택이 애매하면 `inherit`를 쓰지 말고 선택 이유를 남긴 뒤 보수적으로 한 단계 높은 별칭을 선택한다.

## 비용, 속도, 위험 기준

| 기준 | 낮음 | 중간 | 높음 | 매우 높음 |
| --- | --- | --- | --- | --- |
| 사이드이펙트 | `haiku` | `sonnet` | `opus` | `opus`+`max` |
| 도메인 판단 | `sonnet` | `opus` | `opus` | `opus`+`max` |
| 보안 영향 | `opus` | `opus`+`xhigh` | `opus`+`max` | `opus`+`max` |
| 변경 범위 | `sonnet` | `sonnet` | `opus` | `opus`+`max` |
| 검증 난이도 | `sonnet` | `opus` | `opus` | `opus`+`max` |
| 반복/대량 처리 | `haiku` | `haiku` | `sonnet` | `opus` |

## 서브에이전트 라우팅

| 상황 | 권장 서브에이전트 | 모델/강도 |
| --- | --- | --- |
| 파일 위치, 심볼, 참조를 빠르게 찾는다 | `Explore` | `haiku`~`sonnet`, low~medium |
| 구현 전 설계/청사진이 필요하다 | Plan Mode 또는 `Plan` 서브에이전트 | `opus`, high |
| 범용 다단계 조사/구현 | `general-purpose` | `sonnet`~`opus`, medium~high |
| Claude Code 자체 사용법 질문 | `claude-code-guide` | `sonnet`, medium |
| 상태줄 설정 | `statusline-setup` | `sonnet`, low |
| 코드/보안 리뷰 | 전용 리뷰 관점, `.claude/agents/reviewer.md`(이 저장소 예시) 또는 `security-review`/`simplify` Skill | `opus`, high |
| 이 저장소 문서 하네스 자체 점검 | `.claude/agents/doc-lint.md`(이 저장소 예시) | `sonnet`, low~medium |
| 하네스, 에이전트 운영 정책, 검증 기준, 실패 케이스, 산출물 스키마처럼 문서화 판단이 핵심인 토론 | `.claude/agents/harness-deliberator.md` | `opus`, high |
| 프로젝트 목적, 주 사용자, 연령대, 기능 범위 판단 | `.claude/agents/product-planner.md` | `opus`, high |
| UX/UI, 사용성, 표현 방식, 참여 유도 판단 | `.claude/agents/ux-ui-designer.md` | `opus`, high |
| 컴포넌트 구조, 재사용, 프론트엔드/앱 구현 전략 | `.claude/agents/frontend-developer.md` | `sonnet`~`opus`, medium~high |
| 도메인 우선 백엔드 설계, 아키텍처 정합성, 테스트 우선 개발 | `.claude/agents/backend-developer.md` | `opus`, high |
| Entity와 DBMS 적합성, 스키마, 인덱스, 제약, 마이그레이션 | `.claude/agents/database-specialist.md` | `opus`, high |
| 타겟 사용자 관점의 사용성, UX/UI, 개선 제안 검토 | `.claude/agents/product-tester.md` | `sonnet`~`opus`, medium~high |

역할별 기본값보다 작업 조건 루브릭이 우선한다. 예를 들어 `general-purpose`에 위임했더라도 마이그레이션이나 보안 변경을 다루면 `opus`와 `xhigh`로 승격한다.

하네스, 에이전트 운영 정책, 검증 기준, 실패 케이스, 산출물 스키마처럼 문서화 판단이 핵심인 토론에는 기본 서브에이전트 `.claude/agents/harness-deliberator.md`를 우선 고려한다. 제품·기능 토론에는 기본 서브에이전트 세트인 `.claude/agents/product-planner.md`, `.claude/agents/ux-ui-designer.md`, `.claude/agents/frontend-developer.md`, `.claude/agents/backend-developer.md`, `.claude/agents/database-specialist.md`, `.claude/agents/product-tester.md`를 목적에 맞게 조합한다. 이 기본 세트는 [Codex의 `.codex/agents/*.toml` 기본 세트](../codex/model-routing.md)와 역할이 대응하며, 두 모델 중 하나에서만 역할·문서 참조를 갱신하지 않도록 함께 갱신한다.

## 병렬 서브에이전트 쓰기 충돌 회피

- 코드 탐색, 리뷰, 조사처럼 경계가 분명한 읽기 중심 작업은 병렬 `Agent` 호출로 위임해도 된다.
- 같은 파일을 동시에 수정하는 write-heavy 병렬 작업은 충돌과 조정 비용이 크므로 피한다.
- 병렬 쓰기가 꼭 필요하면 대상 파일을 겹치지 않게 나누거나, `Agent`/`Workflow`의 `isolation: "worktree"` 옵션으로 각 에이전트를 별도 워크트리에서 실행한다.
- `isolation: "worktree"`는 설정 비용이 있으므로 에이전트들이 실제로 같은 파일을 건드릴 가능성이 있을 때만 사용한다.

## Workflow(멀티 에이전트 오케스트레이션) 사용 기준

- `Workflow` 도구는 사용자가 명시적으로 오케스트레이션을 opt-in했을 때만 사용한다: "ultracode" 키워드, 세션에 ultracode가 켜져 있다는 안내, "워크플로우 사용해줘"처럼 사용자 자신의 표현, 특정 저장된 workflow 실행 요청, 또는 opt-in을 지시하는 Skill/슬래시 커맨드.
- 위 조건이 없으면 작업이 아무리 커도 `Agent` 도구로 서브에이전트를 개별 위임하거나, 오케스트레이션이 도움이 될 것이라는 점과 예상 비용만 안내하고 사용자 결정을 기다린다.
- Workflow 실행 규모는 세션 설정(예: `/config`의 workflow size guideline)을 따르며, opt-in이 있어도 규모는 요청에 맞춰 과도하게 키우지 않는다.
- 더 나은 의견 형성이 목표인 숙의형 작업은 공통 하네스의 `global/harness/deliberation.md`를 따라 독립 의견, 비판, 수정, 합성, 검증 라운드를 구분한다.
- 숙의형 작업의 최종 판단은 단순 다수결이 아니라 근거 품질, 정책 적합성, 반대 근거, 남은 리스크를 기준으로 한다.
- 숙의형 패턴 자체는 `Workflow` opt-in의 하위 항목이 아니다. 역할을 `proposer`/`critic`/`synthesizer` 정도로 줄일 수 있는 작은 규모라면, opt-in 없이 `Agent`를 여러 번(1라운드는 서로의 결과를 안 보게 병렬로) 호출해 라운드를 직접 진행해도 된다. 라운드를 스크립트로 강제해야 하거나 규모가 커지면 그때 opt-in을 확인하고 `Workflow`로 승격한다.

## frontmatter 규칙

서브에이전트 파일이 frontmatter를 사용한다면 아래 필드만 기본으로 사용한다.

```yaml
---
name: reviewer
description: 코드 리뷰 관점으로 diff, 회귀, 테스트 갭을 찾는다
model: opus
---
```

실제 정의 예시는 `.claude/agents/reviewer.md`와 `.claude/agents/doc-lint.md`를 참조한다.

- `model`은 별칭, 풀 모델 ID, `inherit` 중 하나다.
- `model`을 생략하면 `inherit`로 처리한다.
- `type` 필드는 사용하지 않는다.

## 풀 모델 ID 사용 기준

- 특정 프로젝트에서 모델 가용성과 비용 기준이 확정되어 있다.
- 재현 가능한 실행 환경이 필요하다.
- 역할별 품질, 속도, 비용 차이를 검증했다.
- 글로벌 규칙보다 로컬 설정으로 고정하는 것이 안전하다.

## 로컬 매핑

글로벌 문서는 별칭만 정의한다. 실제 모델 ID 매핑은 로컬 프로젝트나 개인 설정에서 관리한다.

```yaml
model_aliases:
  haiku: <full-model-id>
  sonnet: <full-model-id>
  opus: <full-model-id>
  fable: <full-model-id>
```

같은 저장소에서 [Codex](../codex/model-routing.md)도 함께 쓰는 환경이라면, Codex가 문서에서 쓰는 단순/일반/복잡/고위험 등급과 이 문서의 별칭을 로컬 설정에서 상호 매핑해 둔다(예: 단순 ↔ `haiku`, 일반 ↔ `sonnet`, 복잡 ↔ `opus`, 고위험 ↔ `opus`+`xhigh`/`max`). 작성 예시는 `local/_template/model-routing-map.md`(템플릿)와 `local/sample-project/model-routing-map.md`(작성 예시)를 참조한다.
