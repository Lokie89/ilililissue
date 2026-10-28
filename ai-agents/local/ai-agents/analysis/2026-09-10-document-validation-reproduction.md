# 원본 문서 검사 재현 결과

## 분석 범위

- 원본 `ai-agents` 작업 트리의 `scripts/validate-docs.sh`
- 동일 스크립트가 선언한 파일 존재 검사와 문구 검사
- `scripts/validate-harness.mjs`, `scripts/test-evaluator.mjs`
- Windows 작업 트리의 줄바꿈 상태와 Git 인덱스 상태

## 확인한 파일과 경로

- `scripts/validate-docs.sh`
- `scripts/validate-harness.mjs`
- `scripts/test-evaluator.mjs`
- `.github/workflows/validate-docs.yml`
- `local/ai-agents/analysis/`

## 핵심 결론

- `validate-docs.sh`에는 파일 존재 검사 79건과 문구 검사 185건이 선언되어 있다. 185는 실패 건수가 아니라 검사 항목 수다.
- PowerShell에서 동일한 검사 조건을 재현한 결과 파일 검사 79건, 문구 검사 185건 모두 통과했다.
- Node 기반 검사도 `harness validation passed: 6 fixture(s)`, `evaluator tests passed: 2 case(s)`로 통과했다.
- Windows 작업 트리에서는 `scripts/validate-docs.sh`가 CRLF로 체크아웃되어 `bash scripts/validate-docs.sh`가 검사 로직에 진입하기 전에 `set: invalid option`, `syntax error near unexpected token`으로 종료된다.
- `git ls-files --eol` 기준 해당 스크립트는 인덱스가 LF이고 작업 트리가 CRLF이며, 현재 diff에는 표시되지 않는다. 따라서 이는 저장소 내용 변경이라기보다 Git의 `core.autocrlf=true`와 로컬 Bash 실행 환경의 문제다.
- GitHub Actions는 Ubuntu에서 실행되므로 저장소 인덱스의 LF 줄바꿈을 사용해 이 CRLF 문제와는 실행 환경이 다르다.

## 후속 조치와 미정 항목

- 이번 확인에서는 파일을 수정하지 않았다.
- Windows에서도 직접 실행해야 한다면 Git Bash/WSL 같은 POSIX Bash 환경을 사용하거나, 저장소에 `.gitattributes`로 셸 스크립트의 LF를 명시하는 방안을 별도로 검토할 수 있다.
- 외부에서 보고한 “185건 실패”의 원문 로그가 없으므로, 그 보고가 185개 검사 항목의 집계인지 실제 실패 목록인지까지는 추가 로그 대조가 필요하다.
