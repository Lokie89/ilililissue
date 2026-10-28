"use client";

import { useEffect, useRef } from "react";

/**
 * 이슈 상세를 연 브라우저에서 조회를 한 번 기록한다.
 * 백엔드가 방문자 쿠키(visitor_id)를 발급하고, 지표에서 같은 방문자의 같은 이슈 조회는 한 번으로 센다.
 * 화면을 그리지 않으며, 실패해도 사용자에게 알리지 않는다.
 */
export function ViewTracker({ issueId }: { issueId: number }) {
  // 개발 모드(Strict Mode)에서 effect가 두 번 실행되어도 한 번만 보낸다.
  const sent = useRef<number | null>(null);

  useEffect(() => {
    if (sent.current === issueId) return;
    sent.current = issueId;
    fetch(`/api/issues/${issueId}/views`, { method: "POST", credentials: "same-origin" }).catch(() => undefined);
  }, [issueId]);

  return null;
}
