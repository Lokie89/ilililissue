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
      <div className="mb-3 flex gap-2">
        {(Object.keys(STATUS_LABEL) as ReportStatus[]).map((s) => (
          <button
            key={s}
            type="button"
            onClick={() => setStatus(s)}
            className={`rounded px-3 py-1 text-sm ${status === s ? "bg-stone-900 text-white" : "bg-stone-100"}`}
          >
            {STATUS_LABEL[s]}
          </button>
        ))}
      </div>
      {message && <p className="mb-2 text-sm text-rose-600">{message}</p>}
      {reports.length === 0 ? (
        <p className="py-6 text-center text-stone-500">신고가 없습니다.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {reports.map((report) => (
            <li key={report.id} className="rounded-lg border border-stone-200 p-4 text-sm">
              <div className="mb-1 text-stone-500">
                신고 #{report.id} · 이슈 #{report.issueId} · 의견 #{report.opinionId} ·{" "}
                {new Date(report.reportedAt).toLocaleString("ko-KR")}
              </div>
              <p className="mb-2 whitespace-pre-wrap rounded bg-stone-50 p-2">{report.opinionContent}</p>
              <p className="mb-2">사유: {report.reason}</p>
              {report.status === "RECEIVED" && (
                <div className="flex gap-2">
                  <button type="button" onClick={() => resolve(report.id, "hide")} className="rounded bg-rose-600 px-3 py-1 text-white">
                    의견 숨기기
                  </button>
                  <button type="button" onClick={() => resolve(report.id, "dismiss")} className="rounded border px-3 py-1">
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
