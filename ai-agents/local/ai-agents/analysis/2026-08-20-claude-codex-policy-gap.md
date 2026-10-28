# Claude/Codex 정책 차이 분석

## 분석 범위

- `global/models/claude/`와 `global/models/codex/`의 대응 문서
- 공통 로컬 문서 안내인 `local/README.md`
- 최근 Claude 작업 기록의 Codex 미반영 항목

## 확인한 파일과 경로

- `global/models/claude/AGENT.md`
- `global/models/claude/project-rules.md`
- `global/models/claude/handoff-log.md`
- `global/models/codex/AGENT.md`
- `global/models/codex/project-rules.md`
- `global/models/codex/handoff-log.md`
- `local/README.md`

## 핵심 결론

- Claude의 2026-08-20 작업에서 추가된 `코드 분석 산출물` 규칙이 Codex 문서에는 반영되지 않았다.
- 공통 `local/README.md`에는 선택 구조인 `analysis/` 안내가 이미 반영되어 있었다.
- Claude의 hook, Plan Mode 도구명, Workflow, 서브에이전트 세부 규칙은 런타임 전용 차이이므로 Codex 동기화 대상이 아니다.
- Codex에는 분석 결과 저장 위치, 파일명, 필수 내용, 기존 분석 우선 확인, handoff와의 관계만 동일하게 반영하면 된다.

## 후속 조치와 미정 항목

- Codex의 `AGENT.md`와 `project-rules.md`에 분석 산출물 규칙을 반영했다.
- Codex `handoff-log.md`에 동기화 내역을 남기고 보관 한도를 맞췄다.
- 미정 항목은 없다.
