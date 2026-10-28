#!/usr/bin/env bash
# Stop hook: risk 신호가 있는 세션에서만, review 발견에 대해
# "재기획(Planner 복귀) 필요 여부"를 명시했는지 검사하고 없으면 Stop을 막는다.
# 위험 신호가 없으면 git 상태 확인만 하고 즉시 종료해 토큰 비용을 들이지 않는다.

input="$(cat)"

# 이미 이 Stop 사이클에서 한 번 막혔다면 무한 루프를 막기 위해 그냥 통과시킨다.
stop_hook_active="$(printf '%s' "$input" | jq -r '.stop_hook_active // false' 2>/dev/null)"
if [ "$stop_hook_active" = "true" ]; then
  exit 0
fi

transcript_path="$(printf '%s' "$input" | jq -r '.transcript_path // empty' 2>/dev/null)"

RISK_PATH_RE='domain-policy|domain-rules|security|auth|payment|(^|/)\.env(\.|$)|credential|secret|settings\.json|settings\.local\.json|migration|schema'
RISK_KEYWORD_RE='password|secret|api[_-]?key|DROP TABLE|TRUNCATE TABLE|rm -rf|reset --hard|push --force|--no-verify|--no-gpg-sign|도메인 정책|보안 정책|인증 정책|migration'

changed_files="$( { git diff --name-only HEAD 2>/dev/null; git diff --name-only --cached 2>/dev/null; git status --porcelain 2>/dev/null | awk '{print $2}'; } | sort -u )"

risk=0
if [ -n "$changed_files" ] && printf '%s\n' "$changed_files" | grep -Eiq "$RISK_PATH_RE"; then
  risk=1
fi

if [ "$risk" -eq 0 ]; then
  diff_content="$( { git diff HEAD 2>/dev/null; git diff --cached 2>/dev/null; } )"
  if [ -n "$diff_content" ] && printf '%s' "$diff_content" | grep -Eiq "$RISK_KEYWORD_RE"; then
    risk=1
  fi
fi

if [ "$risk" -eq 0 ]; then
  exit 0
fi

marker_found=0
if [ -n "$transcript_path" ] && [ -f "$transcript_path" ]; then
  if tail -c 20000 "$transcript_path" 2>/dev/null | grep -q 'REVIEW-CHECK:'; then
    marker_found=1
  fi
fi

if [ "$marker_found" -eq 1 ]; then
  exit 0
fi

cat <<'JSON'
{
  "decision": "block",
  "reason": "이 세션에서 위험 신호(보안/도메인 정책/되돌리기 어려운 변경으로 보이는 파일 또는 키워드)가 감지되었습니다. 응답을 끝내기 전에 다음 형식으로 재기획 필요 여부를 한 줄로 명시하세요: 'REVIEW-CHECK: 재기획 불필요 - <근거>' 또는 'REVIEW-CHECK: 재기획 필요 - Planner로 복귀 - <근거>'. 재기획이 필요하면 실제로 Planner 단계로 돌아가 범위를 다시 정리하세요."
}
JSON
exit 0
