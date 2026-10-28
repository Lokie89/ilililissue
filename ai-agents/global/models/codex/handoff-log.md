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

### 2026-09-21 (낮은 모델 우선 구현과 독립 Reviewer 검수)

- 목표: 구현을 가장 낮은 가용 모델·낮은 추론 강도에서 시작하고, 필요한 경우에만 승격하며, 구현보다 높은 등급의 Reviewer로 결과를 독립 검수하는 기준을 Codex 문서에 반영한다.
- 변경: `model-routing.md`의 기본 원칙·역할 기준·구현/승격/검수 절차를 보강했다. `roles.md`, `validation.md`, `harness/failure-cases.md`에 같은 규칙과 위반 기준을 연결했다. 기존 `.claude/settings.local.json` 변경은 사용자 변경으로 유지했다.
- 검증: `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check` 통과. `bash scripts/validate-docs.sh`는 Windows 줄바꿈(CRLF)으로 인해 `set -\r` 오류가 발생해 실행하지 못했다.
- 남은 작업: 없음.
- 주의 사항: 실제 모델 ID와 지원 추론 강도는 현재 환경에서 확인 가능한 값만 사용한다. 고위험 작업의 사람 승인과 독립 검토는 모델 승격만으로 대체하지 않는다.

### 2026-09-15 (Codex 자동 위임 판단)

- 목표: 사용자가 매번 요청하지 않아도 Codex가 작업 규모, 독립성, 위험도에 따라 서브에이전트 위임과 현재 환경의 모델·추론 강도를 자율 선택하도록 한다.
- 변경: Codex 실행 규칙, 모델 라우팅, 역할, 검증, 실패 케이스, fixture, 산출물 스키마, 문서 검증 스크립트에 자동 위임 기준을 추가했다. 작은·순차 작업은 직접 처리하고, 독립 탐색·전문 검토·고위험 독립 검토는 자동 위임하며, 병렬 쓰기는 파일 소유 범위가 겹치지 않을 때만 허용한다.
- 검증: `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행한다.
- 남은 작업: 없음.
- 주의 사항: 외부 모델명이나 존재하지 않는 모델을 가정하지 않는다. 실제 가용 모델을 확인할 수 없으면 부모 설정을 상속하고, 승인·파괴적 작업·외부 상태 변경의 안전 경계는 위임으로 우회하지 않는다.

### 2026-09-09 (보안 검토 에이전트 추가)

- 목표: 인프라·백엔드·프론트엔드 보안 위험을 전담해 점검하는 Codex 에이전트를 기본 세트에 추가한다.
- 변경: `.codex/agents/security-reviewer.toml`을 읽기 전용 증거 기반 검토 에이전트로 추가했다. 인프라, 백엔드, 프론트엔드 위험과 추가 검증 필요 사항을 분리해 보고하며 자동 수정이나 보안 보증을 하지 않는다. `model-routing.md`, 프로젝트 루트 부트스트랩 스크립트, 문서 검증 스크립트에 이 에이전트를 연결했다.
- 검증: `python3 -c 'import tomllib; ...'`, `bash scripts/validate-docs.sh`, `node scripts/validate-harness.mjs`, `node scripts/test-evaluator.mjs`, `git diff --check`를 실행해 통과했다.
- 남은 작업: 없음.
- 주의 사항: 런타임, 클라우드 계정, 의존성 스캐너, 침투 테스트가 필요한 보안 증거는 코드 검토와 구분해 `verification_needed`에 남긴다.

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

