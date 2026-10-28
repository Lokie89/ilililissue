# 컨텍스트 지도

## 먼저 확인할 위치

- `README.md`: 저장소 목적과 모델 목록
- `global/models/claude/README.md`: Claude 모델 개요
- `global/models/claude/AGENT.md`: 에이전트 실행 진입 문서
- `global/models/claude/goal.md`: Claude가 수행할 일과 금지 사항
- `global/models/claude/project-rules.md`: 프로젝트 진입과 로컬 하네스 생성 기준
- `global/models/claude/domain-rules.md`: 도메인 정책을 확인하고 수정하는 글로벌 규칙
- `global/models/claude/model-routing.md`: 역할별 모델 별칭, 추론 강도, 서브에이전트 선택 기준
- `global/models/claude/tools.md`: 허용 도구와 금지 도구
- `global/models/claude/validation.md`: 검증 절차
- `global/models/claude/handoff-log.md`: 공통 작업 기록과 다음 세션 인계
- `global/models/claude/harness/`: 상태 전이, 실패 케이스, 산출물 스키마, fixture, 판정 루브릭
- `local/README.md`: 프로젝트별 문서 위치와 로컬 운영 규칙
- `global/models/codex/README.md`: 같은 저장소를 공유할 수 있는 다른 모델(Codex) 문서

## 코드 작업 시 확인 순서

1. 사용자 요청의 목표와 완료 기준을 파악한다.
2. 새 프로젝트 생성인지, 기존 프로젝트 수정인지, 단순 분석인지 분류한다.
3. 새 프로젝트 생성이나 초기 세팅이면 청사진 선행 요청으로 취급하고, 명시적 구현 승인 전에는 파일을 만들지 않는다.
4. 새 개념, 엔티티, 상태, 정책이 추가되는지 확인한다.
5. 기술 스택, 배포 구조, 서버 구성, 통신 방식, 데이터 저장소 같은 아키텍처 전제가 필요한지 확인한다.
6. 프로젝트별 로컬 하네스가 필요한지 `project-rules.md` 기준으로 판단한다.
7. 로컬 하네스가 없거나 부족하면 `AskUserQuestion`으로 필요한 질문을 먼저 제시한다.
8. 개념 추가가 있으면 도메인 정의와 정책이 명확한지 확인한다.
9. 아키텍처 전제가 구현에 영향을 주는데 `local/<project-name>/architecture.md`가 없거나 부족하면 청사진 질문을 먼저 제시한다.
10. 도메인 정책이 불명확하면 구현 전에 질의를 통해 정책을 먼저 세운다.
11. 청사진만 필요한 요청인지 확인한다.
12. 청사진, 계획, 설계만 요청받았다면 `EnterPlanMode`로 전환해 실행 없이 계획을 먼저 제시한다.
13. 사용자가 실행을 승인했거나 명확한 구현 요청이면 저장소 구조를 확인한다.
14. `Glob`/`Grep`으로 관련 파일을 검색한다.
15. 기존 구현 패턴과 테스트 방식을 `Read`로 확인한다.
16. 필요한 변경만 `Edit`/`Write`로 적용한다.
17. 검증을 실행하고 결과를 기록한다.

## 청사진 작성 순서

1. 요청을 한 문장으로 재정의한다.
2. 작업 범위와 제외 범위를 구분한다.
3. 도메인 개념, 용어, 정책, 경계가 명확한지 확인한다.
4. 도메인이 불명확하면 필요한 질의를 먼저 적는다.
5. 어떤 파일, 문서, 데이터가 영향을 받을지 예상한다.
6. 단계별 접근 방식을 설명한다.
7. 변경 후 달라지는 동작이나 문서 구조를 설명한다.
8. 사이드이펙트, 위험 요소, 되돌리기 어려운 지점을 적는다.
9. 검증 방법과 완료 기준을 제시한다.
10. `ExitPlanMode`로 사용자 승인을 받은 뒤에만 실행 단계로 넘어간다.

## 도메인 정책 확인 기준

- 도메인 관련 판단 방식은 먼저 `global/models/claude/domain-rules.md`를 참조한다.
- 실제 도메인 정책은 `local/<project-name>/domain-policy.md`를 참조한다.
- 실제 도메인 정책을 수정해야 하면 로컬 `domain-policy.md`를 먼저 갱신한다.
- 새 개념이 기존 도메인 용어와 충돌하지 않는지 확인한다.
- 엔티티, 값 객체, 상태, 권한, 생명주기, 불변 조건이 필요한지 판단한다.
- 정책이 코드 구조보다 먼저 설명 가능한지 확인한다.
- 정의가 모호하면 선택지를 임의로 고정하지 않고 `AskUserQuestion`으로 질문한다.

## 도메인 모델과 데이터베이스 모델 분리 기준

- 도메인 모델은 개발자가 사용하는 업무 개념, 규칙, 정책, 생명주기를 표현한다.
- 데이터베이스 모델은 사용하는 DBMS에서 저장, 조회, 인덱싱, 무결성 제약을 효율적으로 다루기 위한 구조로 설계한다.
- 도메인 개념을 먼저 정의한 뒤, 필요한 경우 DBMS 특성에 맞게 테이블, 컬럼, 인덱스, 제약 조건으로 변환한다.
- 테이블 이름이나 컬럼 구조가 곧 도메인 언어라고 가정하지 않는다.
- 도메인과 DB 구조가 다르면 매핑 의도와 트레이드오프를 기록한다.

## 아키텍처 청사진 확인 기준

- 실제 프로젝트 아키텍처 청사진은 `local/<project-name>/architecture.md`를 참조한다.
- 구현 전제에 영향을 주는 기술 스택, 런타임, PC/서버 스펙, 배포 단위, 단일 서버/클러스터 여부, BFF/API Gateway, 통신 방식, 데이터 저장소, 캐시, 메시지 큐, 이벤트 브로커, 인증/권한, 운영 요구가 불명확하면 `AskUserQuestion`으로 질문한다.
- 모든 항목을 한 번에 확정하지 않아도 되지만, 현재 구현 결정에 직접 영향을 주는 항목은 `미정`으로 방치하지 않는다.
- 단일 서버에서 시작하더라도 나중에 분리할 수 있는 경계와 바꾸기 어려운 결정을 구분한다.
- 도메인 모델, 데이터베이스 모델, 배포 구조, 통신 방식을 같은 결정으로 취급하지 않는다.

## 컨텍스트 수집 규칙

- 공통 규칙은 `global/`에서 먼저 확인한다.
- 프로젝트별 예외, 데이터 위치, 추가 제약은 `local/`에서 확인한다.
- 로컬 규칙이 필요한데 없으면 `project-rules.md`의 로컬 하네스 대화 기준을 따른다.
- 파일 검색은 `Glob`(경로 패턴) 또는 `Grep`(내용 검색)을 우선 사용하고, 열린 탐색이 넓으면 `Explore` 서브에이전트에 위임한다(메인 세션 컨텍스트를 작게 유지해 이후 턴의 토큰 비용을 줄이기 위함).
- 관련 파일을 읽기 전에는 구현을 추정하지 않는다.
- 문서, 테스트, 설정 파일을 함께 확인해 작업의 영향을 판단한다.
- 외부 최신 정보가 필요한 경우 `WebSearch`/`WebFetch`로 공식 문서나 1차 출처를 우선 확인한다.

## Role Documents

- `global/models/claude/roles.md`: Planner, Researcher, Developer, Reviewer, Committer, Orchestrator 역할과 대응하는 Claude Code 기능.
- `global/models/claude/project-rules.md`: 프로젝트 진입, 새 프로젝트 생성, 로컬 하네스 대화 기준.

## Harness Documents

- `global/models/claude/harness/README.md`: 하네스 목적과 사용 방법
- `global/models/claude/harness/state-machine.md`: 작업 상태와 전이 조건
- `global/models/claude/harness/failure-cases.md`: 실패 판정 기준
- `global/models/claude/harness/output-schema.md`: 산출물 필수 구조
- `global/models/claude/harness/fixtures.md`: 대표 요청과 기대 행동
- `global/models/claude/harness/evaluation-rubric.md`: 요청 유형 분류와 판정 절차
