# 도구 목록

## 사용할 수 있는 도구

- 파일 검색: `Glob`(경로 패턴), `Grep`(내용 검색)
- 파일 확인: `Read`
- 코드 변경: `Edit`(기존 파일 수정 우선), `Write`(신규 파일 또는 전체 재작성)
- 셸 실행: `Bash`(POSIX 셸 문법), `PowerShell`(Windows 네이티브 문법) — 플랫폼과 스크립트 종류에 맞는 도구를 선택한다
- 서브에이전트 위임: `Agent`(`Explore`, `Plan`, `general-purpose`, `claude-code-guide`, `statusline-setup`, 이 저장소에 정의된 `.claude/agents/reviewer.md`/`doc-lint.md` 등)
- 명시적 오케스트레이션: `Workflow`(opt-in 조건을 충족했을 때만, `model-routing.md`의 Workflow 사용 기준 참조)
- 작업 추적: `TaskCreate`, `TaskUpdate`, `TaskList`, `TaskGet`, `TaskOutput`, `TaskStop`
- 사용자 확인/질의: `AskUserQuestion`, `EnterPlanMode`/`ExitPlanMode`
- 최신 정보 확인: `WebSearch`, `WebFetch`
- 정형 리뷰 결과 보고: `ReportFindings`
- 산출물 공유: `Artifact`(퍼블리시 전 원문 파일 전체 확인 필수), `ShareOnboardingGuide`
- 반복/예약 실행: `ScheduleWakeup`, `CronCreate`/`CronList`/`CronDelete`
- 스킬 호출: `Skill`(저장소에 등록된 스킬, 예: `harness-lab`, `security-review`, `simplify`, `update-config`)
- 버전 확인: `git status`, `git diff`, `git log`(`Bash`/`PowerShell` 경유)
- 도구 스키마 확인: `ToolSearch`(지연 로딩된 도구를 호출하기 전)

## 주의해서 사용할 도구

- 패키지 설치 명령: 네트워크나 권한 상승이 필요하면 이유를 남기고 승인 프롬프트를 통과한 뒤 실행한다.
- 장시간 실행되는 개발 서버: 사용자가 확인할 URL이 필요할 때 `run_in_background` 옵션으로 실행한다.
- 자동 포맷터: 변경 범위가 넓어질 수 있으므로 대상 파일을 확인한다.
- `Workflow`: opt-in 없이 호출하지 않는다. 토큰 비용이 크므로 규모를 세션 가이드라인에 맞춘다.
- 병렬 `Agent`/`Workflow` 호출이 같은 파일을 동시에 수정할 수 있으면 `isolation: "worktree"`로 충돌을 피한다(`model-routing.md`의 병렬 서브에이전트 쓰기 충돌 회피 기준 참조).
- `Artifact`: 사용자가 작성하지 않은 파일은 반드시 전체를 읽은 뒤 퍼블리시한다. 실제 인물, 조직, 금융/신원 정보를 흉내 내는 페이지는 배포하지 않는다.
- `EndConversation`: 지속적인 남용이나 사용자의 명시적 요청 등 정의된 조건에서만 사용한다.

## Git 원격 동기화 경계

- 사용자가 `git push`를 요청했거나 로컬 설정에 자동 push가 명시되어 있으면, 승인된 범위 안에서 push를 시도할 수 있다.
- push가 원격 변경, non-fast-forward, branch protection, 권한, 네트워크, hook, 서명 문제 등으로 실패하면 원인을 보고하고 멈춘다.
- push 실패를 해결하기 위해 `git pull`, `git merge`, `git rebase`, conflict resolution, force push를 임의로 실행하지 않는다.
- 원격과 로컬 이력이 달라 충돌이 난 경우에는 사용자가 직접 동기화와 충돌 해결을 완료한 뒤 다시 push를 요청할 수 있게 안내한다.
- 사용자가 merge/rebase까지 명시적으로 요청하더라도, 원격 이력 변경이나 충돌 해결이 포함되면 별도 승인과 현재 변경 보호 확인 없이는 진행하지 않는다.

## 금지된 도구와 행동

- 사용자 승인 없는 파괴적 명령: `git reset --hard`, `git push --force`(특히 main/master), `git clean -f`, 대량 삭제/이동
- push 실패 해결을 위한 임의 `git pull`, `git merge`, `git rebase`, conflict resolution, force push
- hook 우회(`--no-verify`), 서명 우회(`--no-gpg-sign`) — 사용자가 명시적으로 요청하지 않는 한 금지
- 관련 없는 파일을 대상으로 한 자동 포맷팅
- 비밀값, 토큰, 인증 정보 출력 또는 저장(메모리 파일 포함)
- 검증 우회를 위한 임시 코드
- 실패한 명령을 성공한 것처럼 기록하는 행위
- opt-in 없는 `Workflow` 호출
- 실제 인물, 조직을 사칭하거나 신원 확인 절차를 우회하는 산출물 작성

## 자동 강제 장치

- 이 저장소의 `.claude/settings.json`에 `Stop` hook(`.claude/hooks/review-replan-check.sh`)이 등록되어 있다.
- git diff/status에서 도메인 정책, 보안, 인증, 시크릿, migration 관련 파일이나 키워드(위험 신호)가 감지되면, 응답을 끝내기 전에 `REVIEW-CHECK: 재기획 불필요 - <근거>` 또는 `REVIEW-CHECK: 재기획 필요 - Planner로 복귀 - <근거>` 형식으로 재기획 필요 여부를 명시해야 훅이 응답 종료를 허용한다(`roles.md`의 Reviewer→Planner 전이 규칙을 기계적으로 보강).
- 위험 신호가 없으면 훅은 아무 것도 하지 않는다(토큰 비용 없음).
- 이 훅은 Claude Code의 hook 시스템에만 존재하며 Codex에는 적용되지 않는다.

## 도구 선택 기준

- 전용 도구가 있으면 `Bash`/`PowerShell` 대신 전용 도구(`Read`/`Edit`/`Write`/`Glob`/`Grep`)를 우선한다.
- 저장소에 이미 있는 명령과 스크립트를 우선 사용한다.
- 같은 결과를 낼 수 있다면 변경 범위가 작고 추적이 쉬운 도구를 선택한다.
- 네트워크나 권한 상승이 필요한 작업은 필요성과 영향을 명확히 하고 승인을 기다린다.
- 열린 탐색이 3회 이상의 검색으로 이어질 것 같으면 직접 반복하는 대신 `Explore` 서브에이전트에 위임한다.
