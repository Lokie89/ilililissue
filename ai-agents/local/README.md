# Local 프로젝트 문서

이 디렉터리는 프로젝트별로 달라지는 에이전트 문서와 데이터를 둡니다.

## 사용 원칙

- 모든 프로젝트에 공통으로 적용할 규칙은 `global/`에 둡니다.
- 특정 프로젝트에만 필요한 목표, 컨텍스트, 도메인 정책, 도구 제약, 검증 방법, 작업 기록은 `local/`에 둡니다.
- 로컬 문서는 글로벌 규칙을 덮어쓸 수 있지만, 충돌이 있으면 어떤 이유로 예외가 필요한지 기록합니다.
- 로컬 하네스 문서 본문은 기본적으로 한국어로 작성하고, 파일명, 코드 식별자, 명령어, 스키마 필드명, 외부 API 이름은 원문 또는 영어를 유지합니다.
- 외부 연동 어댑터, API, 메시지, 파일 교환이 있으면 `architecture.md`, `README.md`, `docs/` 중 프로젝트 구조에 맞는 곳에 인터페이스 문서를 남기고, 로컬 하네스가 있으면 `architecture.md`에서 그 위치를 참조합니다.

## 권장 구조

```text
local/
  README.md
  <project-name>/
    goal.md
    context-map.md
    domain-policy.md
    architecture.md
    tools.md
    validation.md
    handoff-log.md
    model-routing-map.md  # 선택: Codex와 Claude를 함께 쓰는 프로젝트만
    analysis/             # 선택: 코드/아키텍처 분석 결과 문서 (YYYY-MM-DD-주제.md)
    docs/                 # 선택: 외부 연동 인터페이스 문서 등 프로젝트 문서
```

## 현재 상태

샘플 프로젝트 문서가 등록되어 있습니다.

- [`sample-project`](./sample-project/): 글로벌 하네스와 로컬 도메인 정책 적용 예시, Codex/Claude 모델 라우팅 매핑 예시, 서브트리 오버라이드 예시(`legacy-module/`) 포함
