"use client";

import { useState } from "react";
import { useMe } from "@/components/me-provider";
import { AdminIssues } from "./admin-issues";
import { AdminMetrics } from "./admin-metrics";
import { AdminReports } from "./admin-reports";

const TABS = {
  issues: "이슈",
  reports: "신고",
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
    return <p className="text-stone-500">확인 중…</p>;
  }
  if (me?.role !== "ADMIN") {
    return <p className="py-16 text-center text-stone-500">관리자만 볼 수 있는 페이지예요.</p>;
  }

  return (
    <div>
      <h1 className="mb-4 text-xl font-bold">관리</h1>
      <div className="mb-6 flex gap-4 border-b border-stone-200">
        {(Object.keys(TABS) as Tab[]).map((key) => (
          <button
            key={key}
            type="button"
            onClick={() => setTab(key)}
            className={`pb-2 ${tab === key ? "border-b-2 border-stone-900 font-semibold" : "text-stone-500"}`}
          >
            {TABS[key]}
          </button>
        ))}
      </div>
      {tab === "issues" && <AdminIssues />}
      {tab === "reports" && <AdminReports />}
      {tab === "metrics" && <AdminMetrics />}
    </div>
  );
}
