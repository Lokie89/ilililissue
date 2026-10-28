# 숙의형 멀티 에이전트 패턴

이 문서는 여러 에이전트가 서로의 의견을 검토하고 더 나은 결론을 만들기 위한 공통 오케스트레이션 패턴을 정의한다.

목표는 에이전트끼리 자유롭게 대화하게 두는 것이 아니라, Orchestrator가 독립 의견, 비판, 수정, 합성, 검증 라운드를 통제해 결론 품질을 높이는 것이다.

## 적용할 때

- 요구사항이 모호하거나 여러 관점의 장단점 비교가 필요하다.
- 설계, 정책, 아키텍처, 리뷰, 의사결정처럼 정답보다 근거 품질이 중요한 작업이다.
- 단일 에이전트가 놓치기 쉬운 반례, 위험, 대안을 찾는 것이 중요하다.
- 최종 결론에 남은 리스크와 반대 근거를 함께 남겨야 한다.

단순 검색, 작은 코드 수정, 명확한 테스트 실패 수정처럼 답이 분명한 작업에는 기본으로 사용하지 않는다.

## 역할

| 역할 | 책임 |
| --- | --- |
| `orchestrator` | 라운드 순서, 입력 공개 범위, 종료 조건, 산출물 형식을 통제한다. |
| `proposer` | 첫 해결안이나 입장을 제시한다. |
| `critic` | 허점, 반례, 위험한 가정, 빠진 조건을 찾는다. |
| `alternative` | 다른 접근 방식이나 경쟁 가설을 제시한다. |
| `synthesizer` | 좋은 주장과 근거를 합쳐 최종안을 만든다. |
| `verifier` | 최종안이 요청에 답하는지, 근거와 리스크가 충분한지 점검한다. |

작업 규모가 작으면 `proposer`, `critic`, `synthesizer`만 사용한다. 역할을 늘리는 것보다 서로 다른 책임을 분명히 하는 것이 우선이다.

## 라운드

```text
received
  -> independent_analysis
  -> critique
  -> revision
  -> synthesis
  -> verification
  -> completed | blocked | budget_exhausted
```

1. `independent_analysis`: 각 에이전트가 다른 에이전트 의견을 보지 않고 독립 초안을 낸다.
2. `critique`: Orchestrator가 초안을 공개하고, 각 에이전트가 다른 의견의 약점과 반례를 지적한다.
3. `revision`: 각 에이전트가 비판을 반영해 자기 의견을 유지, 수정, 철회 중 하나로 갱신한다.
4. `synthesis`: Synthesizer가 다수결이 아니라 근거 품질, 정책 적합성, 리스크를 기준으로 최종안을 작성한다.
5. `verification`: Verifier가 최종안의 누락, 약한 근거, 남은 반대 의견, 검증 필요 항목을 확인한다.

처음부터 다른 의견을 공개하지 않는다. 첫 라운드 독립성이 깨지면 에이전트들이 비슷한 주장으로 수렴하기 쉽다.

## 산출물 형식

각 에이전트 의견은 아래 구조를 따른다.

```json
{
  "agent": "critic",
  "round": "critique",
  "position": "Revise the proposal",
  "confidence": 0.72,
  "key_claims": [
    "The proposal misses rollback behavior."
  ],
  "evidence": [
    "validation.md requires failed validation reasons."
  ],
  "assumptions": [
    "The change affects shared documentation."
  ],
  "risks": [
    "A vague final plan could be implemented inconsistently."
  ],
  "counterpoints": [
    "The current proposal is acceptable if scoped to documentation only."
  ],
  "revision_notes": [
    "Add explicit verification and handoff requirements."
  ]
}
```

최종 합성 결과는 아래 구조를 따른다.

```json
{
  "decision": "Proceed with scoped documentation contract",
  "confidence": 0.81,
  "accepted_claims": [],
  "rejected_claims": [],
  "reasoning_summary": "",
  "remaining_risks": [],
  "required_validation": [],
  "follow_up_questions": []
}
```

## 라운드 요약 로그

Orchestrator는 각 라운드가 끝날 때 사람이 빠르게 읽을 수 있는 짧은 요약을 남긴다. 원문 대화 전체를 보관하지 않고, 결정에 영향을 준 핵심 주장과 변경만 기록한다.

```md
## Deliberation Summary

### Round 1: Independent Analysis
- proposer: 초기안은 A. 핵심 근거는 X.
- critic: 아직 비판 없음.
- alternative: 대안은 B. 장점은 Y.

### Round 2: Critique
- critic: A는 rollback 조건이 약함.
- proposer: 지적 수용. A에 rollback 조건 추가 필요.

### Round 3: Revision
- proposer: A를 A'로 수정.
- alternative: B는 비용 문제로 보조안으로 낮춤.

### Final Synthesis
- decision: A' 채택.
- why: X 근거가 강하고, critic이 지적한 rollback 조건을 보완함.
- remaining risks: Z는 추가 검증 필요.
```

요약 로그는 아래 기준을 지킨다.

- 라운드당 3~5줄을 기본으로 한다.
- 에이전트별 원문 전체를 붙이지 않는다.
- 결론에 영향을 준 주장, 반박, 수정, 남은 리스크만 남긴다.
- 합의되지 않은 쟁점은 삭제하지 않고 `remaining risks`나 `follow_up_questions`에 남긴다.
- 사용자가 상세 기록을 요청하지 않는 한 내부 사고 과정처럼 장황한 대화문을 재현하지 않는다.

## 토론 후 문서화

에이전트 간 토론으로 결정, 정책, 아키텍처 전제, 검증 기준, 실패 케이스, 산출물 형식, 남은 리스크, 후속 질문이 바뀌면 반드시 오래 남을 문서에 반영한다. 라운드 요약 로그는 토론의 중간 기록이고, 최종 기준을 대체하지 않는다.

문서화 대상은 아래 순서로 판단한다.

1. 공통 운영 기준이면 `global/harness/` 문서에 남긴다.
2. 특정 모델 실행 규칙이면 `global/models/<model>/`의 `AGENT.md`, `model-routing.md`, `validation.md`, `harness/failure-cases.md`, `harness/output-schema.md` 중 맞는 문서에 남긴다.
3. 특정 프로젝트 정책이면 `local/<project-name>/`의 `domain-policy.md`, `architecture.md`, `tools.md`, `validation.md`, `handoff-log.md` 중 맞는 문서에 남긴다.
4. 장기 작업, 다음 세션 인계, 결정 배경이 필요한 변경이면 해당 `handoff-log.md`에 남긴다.

원문 토론 전체를 보존하지 않고, 결정에 영향을 준 주장, 채택/기각 이유, 남은 리스크, 갱신한 문서, 실행한 검증만 남긴다. 문서화할 내용이 없다고 판단한 경우에도 그 이유를 최종 보고나 요약 로그에 짧게 남긴다.

## 판정 기준

- 다수결을 최종 판단 기준으로 사용하지 않는다.
- 근거가 구체적이고 현재 컨텍스트와 연결된 주장을 우선한다.
- 반대 의견이 강하면 최종안에 반대 근거와 완화책을 남긴다.
- 정책, 보안, 데이터 손실, 권한, 되돌리기 어려운 변경은 높은 확신만으로 통과시키지 않는다.
- hard gate가 있는 작업은 코드, 테스트, 정책 규칙이 먼저 판정하고 숙의는 애매한 영역에만 사용한다.

## 종료 조건

- 기본 라운드는 `independent_analysis`, `critique`, `revision`, `synthesis`, `verification` 1회씩이다.
- 반복은 최대 2라운드를 기본값으로 한다.
- 같은 주장 반복, 새 근거 없음, 도구 예산 초과, 사용자 결정 필요 상태가 되면 종료한다.
- 결론을 만들 수 없으면 성공으로 포장하지 않고 `blocked` 또는 `budget_exhausted`로 기록한다.

## 실패 케이스

- 독립 분석 전에 다른 에이전트 의견을 공개했다.
- Critic이 반례 없이 선호만 표현했다.
- Synthesizer가 근거 품질 대신 단순 다수결로 결정했다.
- Verifier가 최종안의 남은 리스크를 확인하지 않았다.
- Orchestrator가 라운드, 공개 범위, 종료 조건을 기록하지 않았다.
- Orchestrator가 라운드별 요약 로그 없이 최종 결론만 남겼다.
- 토론으로 바뀐 결정, 정책, 리스크, 후속 질문을 적절한 durable 문서나 handoff 기록에 남기지 않았다.
- 요약 로그가 원문 대화처럼 장황하거나 결론에 영향 없는 발화를 포함했다.
- 에이전트가 서로 같은 역할을 반복해 관점 다양성이 사라졌다.
