"use client";

import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
import type { AdminReport, Page, ReportStatus } from "@/lib/types";

const STATUS_LABEL: Record<ReportStatus, string> = {
  RECEIVED: "접수",
  RESOLVED_HIDDEN: "숨김 처리",
  RESOLVED_DISMISSED: "기각",
};

export function AdminReports() {
  const [status, setStatus] = useState<ReportStatus>("RECEIVED");
  const [reports, setReports] = useState<AdminReport[]>([]);
  const [message, setMessage] = useState<string | null>(null);

  const fetchReports = useCallback(
    () => api<Page<AdminReport>>("/api/admin/reports", { query: { status, size: 50 } }),
    [status],
  );

  const reload = useCallback(async () => setReports((await fetchReports()).content), [fetchReports]);

  useEffect(() => {
    fetchReports()
      .then((page) => setReports(page.content))
      .catch((e) => setMessage(errorMessage(e)));
  }, [fetchReports]);

  async function resolve(id: number, action: "hide" | "dismiss") {
    setMessage(null);
    try {
      await api(`/api/admin/reports/${id}/${action}`, { method: "POST" });
      await reload();
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  return (
    <section>
      <div className="mb-4 flex flex-wrap gap-1.5">
        {(Object.keys(STATUS_LABEL) as ReportStatus[]).map((s) => (
          <button
            key={s}
            type="button"
            onClick={() => setStatus(s)}
            className={`btn btn-sm ${status === s ? "btn-primary" : "btn-secondary"}`}
          >
            {STATUS_LABEL[s]}
          </button>
        ))}
      </div>
      {message && <p className="error-text mb-2">{message}</p>}
      {reports.length === 0 ? (
        <p className="card py-12 text-center text-muted">신고가 없습니다.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {reports.map((report) => (
            <li key={report.id} className="card text-sm">
              <div className="mb-2 text-xs text-subtle">
                신고 #{report.id} · 이슈 #{report.issueId} · 의견 #{report.opinionId} ·{" "}
                {new Date(report.reportedAt).toLocaleString("ko-KR")}
              </div>
              <p className="mb-3 rounded-xl bg-ink/[0.03] p-3 leading-relaxed whitespace-pre-wrap">{report.opinionContent}</p>
              <p className="mb-3 text-muted">사유: {report.reason}</p>
              {report.status === "RECEIVED" && (
                <div className="flex gap-2">
                  <button type="button" onClick={() => resolve(report.id, "hide")} className="btn btn-sm btn-danger">
                    의견 숨기기
                  </button>
                  <button type="button" onClick={() => resolve(report.id, "dismiss")} className="btn btn-sm btn-secondary">
                    기각
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
