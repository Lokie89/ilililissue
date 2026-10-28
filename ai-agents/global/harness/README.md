# 공통 에이전트 하네스

이 디렉터리는 Codex와 Claude 문서에 공통으로 적용할 수 있는 실행·평가 계약을 정의한다. 모델별 `global/models/<model>/harness/`가 행동 기준을 설명한다면, 이 공통 하네스는 자동 평가와 런타임 구현이 교환할 데이터 형식을 정의한다.

## 현재 범위

현재 구현된 범위는 문서 정책과 로컬 검증기다.

- 에이전트 하네스 계층과 책임
- 숙의형 멀티 에이전트 패턴
- 구조화된 평가 fixture
- 실행 기록(run record) 스키마
- 종료 조건과 승인 상태 표현
- 모델 호출 없이 실행할 수 있는 정적 검증

아직 특정 에이전트 SDK, 모델 API, trace 저장소, queue 또는 배포 런타임은 선택하지 않았다.

## 구성

- [architecture.md](./architecture.md): 일반적인 에이전트 구성과 현재 구현 경계
- [deliberation.md](./deliberation.md): 독립 의견, 비판, 수정, 합성, 검증 라운드 기반 숙의 패턴
- [schemas/fixture.schema.json](./schemas/fixture.schema.json): 평가 fixture 계약
- [schemas/run-record.schema.json](./schemas/run-record.schema.json): 실행 결과와 trace 요약 계약
- [fixtures/core.json](./fixtures/core.json): 모델 공통 대표 fixture
- `runs/*.json`: evaluator 회귀 테스트용 실행 기록
- `../../scripts/validate-harness.mjs`: 공통 계약과 fixture 정적 검증기
- `../../scripts/evaluate-run.mjs`: fixture와 실행 기록 비교

## 검증

```sh
node scripts/validate-harness.mjs
node scripts/test-evaluator.mjs
```

이 명령은 JSON 문법, 필수 필드, ID 중복, 종료 한도, 위험 행동 승인 규칙을 확인한다. 실제 모델의 응답 품질이나 도구 실행은 평가하지 않는다.

개별 실행 기록은 다음 형식으로 판정한다.

```sh
node scripts/evaluate-run.mjs global/harness/fixtures/core.json \
  blueprint_payment_retry \
  global/harness/runs/blueprint_payment_retry.pass.json
```

evaluator는 종료 상태, 쓰기 금지, 금지 행동, 승인 누락, 필수 근거, 실행 예산, 보고된 위반을 결정론적으로 판정한다. 자연어 의미와 실제 코드 정확성은 이후 의미 evaluator나 프로젝트 테스트가 담당한다.

## 다음 구현 단계

1. 선택한 에이전트 런타임에서 `run-record.schema.json` 형식으로 실행 기록을 내보낸다.
2. fixture별 요청을 격리된 환경에서 실행한다.
3. tool call과 파일 변경을 기대값과 비교한다.
4. 규칙 기반 판정으로 부족한 의미 평가만 별도 evaluator에 맡긴다.
5. 대표 fixture를 CI 회귀 평가에 연결한다.

런타임을 도입하기 전에는 모델 공급자, 상태 저장소, trace 보존 기간, 비용 한도, CI 실행 빈도를 사용자가 결정해야 한다.
