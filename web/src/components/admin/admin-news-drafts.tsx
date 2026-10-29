"use client";

import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
import type { NewsDraftRun, NewsDraftStatus } from "@/lib/types";

const POLL_INTERVAL_MS = 5000;

function describe(run: NewsDraftRun): string {
  const when = run.finishedAt.slice(0, 16).replace("T", " ");
  if (run.error) {
    return `${when} 실패: ${run.error}`;
  }
  const skipped = run.skippedDrafts ? `, ${run.skippedDrafts}건 제외` : "";
  const images = run.imageCount ? `, 이미지 ${run.imageCount}장` : "";
  return `${when} 기사 ${run.articleCount}건으로 초안 ${run.createdIssueIds.length}건 등록${images}${skipped}`;
}

/**
 * 뉴스 자동 초안 수동 실행. 백엔드가 백그라운드로 돌리므로 끝날 때까지 상태를 주기적으로 확인한다.
 */
export function AdminNewsDrafts({ onFinished }: { onFinished: () => Promise<void> }) {
  const [status, setStatus] = useState<NewsDraftStatus | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const fetchStatus = useCallback(() => api<NewsDraftStatus>("/api/admin/news-drafts/status"), []);

  useEffect(() => {
    fetchStatus()
      .then(setStatus)
      .catch((e) => setMessage(errorMessage(e)));
  }, [fetchStatus]);

  const running = status?.running ?? false;

  useEffect(() => {
    if (!running) {
      return;
    }
    const timer = setInterval(async () => {
      try {
        const next = await fetchStatus();
        setStatus(next);
        if (!next.running) {
          await onFinished();
        }
      } catch (e) {
        setMessage(errorMessage(e));
      }
    }, POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [running, fetchStatus, onFinished]);

  async function start() {
    setMessage(null);
    try {
      setStatus(await api<NewsDraftStatus>("/api/admin/news-drafts", { method: "POST" }));
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  return (
    <section className="card">
      <h2 className="mb-1 font-bold">뉴스 자동 초안</h2>
      <p className="mb-4 text-sm text-muted">
        지난 24시간 뉴스를 모아 AI가 질문·배경 설명 초안을 &lsquo;작성 중&rsquo; 상태로 등록합니다. 설정하면 매일 오전 7시에
        자동으로 실행됩니다. Codex CLI 방식이면 이슈마다 대표 이미지도 만듭니다(한 장에 1~2분). 사실관계·중립성·출처를 확인한 뒤 검수·게시해 주세요.
      </p>
      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={start}
          disabled={running}
          className="btn btn-sm btn-primary"
        >
          {running ? "만드는 중…" : "지금 초안 만들기"}
        </button>
        {status?.lastRun && <span className="text-xs text-muted">최근 실행: {describe(status.lastRun)}</span>}
      </div>
      {message && <p className="error-text mt-3">{message}</p>}
    </section>
  );
}
