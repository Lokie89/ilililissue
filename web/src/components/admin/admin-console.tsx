"use client";

import { useState } from "react";
import { useMe } from "@/components/me-provider";
import { AdminHiddenOpinions } from "./admin-hidden-opinions";
import { AdminIssues } from "./admin-issues";
import { AdminMetrics } from "./admin-metrics";
import { AdminReports } from "./admin-reports";

const TABS = {
  issues: "이슈",
  reports: "신고",
  hidden: "숨긴 의견",
  metrics: "지표",
} as const;

type Tab = keyof typeof TABS;

/**
 * 관리 화면. 실제 권한 검사는 백엔드(/api/admin/**)가 하고, 여기서는 화면 노출만 막는다.
 */
export function AdminConsole() {
  const { me, loading } = useMe();
  const [tab, setTab] = useState<Tab>("issues");

  if (loading) {
    return <div className="h-40 animate-pulse rounded-2xl bg-ink/5" />;
  }
  if (me?.role !== "ADMIN") {
    return <p className="card py-16 text-center text-muted">관리자만 볼 수 있는 페이지예요.</p>;
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold tracking-tight">관리</h1>
      <div role="tablist" className="mb-6 flex gap-1 overflow-x-auto rounded-xl bg-ink/5 p-1">
        {(Object.keys(TABS) as Tab[]).map((key) => (
          <button
            key={key}
            type="button"
            onClick={() => setTab(key)}
            role="tab"
            aria-selected={tab === key}
            className={`flex-1 rounded-lg px-3 py-2 text-sm whitespace-nowrap transition-colors ${
              tab === key ? "bg-surface font-semibold shadow-sm" : "text-muted hover:text-ink"
            }`}
          >
            {TABS[key]}
          </button>
        ))}
      </div>
      {tab === "issues" && <AdminIssues />}
      {tab === "reports" && <AdminReports />}
      {tab === "hidden" && <AdminHiddenOpinions />}
      {tab === "metrics" && <AdminMetrics />}
    </div>
  );
}
