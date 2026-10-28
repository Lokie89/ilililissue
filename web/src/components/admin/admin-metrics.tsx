"use client";

import { useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
import type { Metrics } from "@/lib/types";

function isoDate(date: Date): string {
  return date.toLocaleDateString("sv-SE", { timeZone: "Asia/Seoul" });
}

// 기획서의 초기 실험 목표 (업계 평균이 아닌 가설)
const TARGETS = {
  voteRate: 25,
  opinionRate: 15,
  opposingViewRate: 30,
  revisit7dRate: 15,
};

export function AdminMetrics() {
  const [from, setFrom] = useState(() => isoDate(new Date(Date.now() - 27 * 24 * 60 * 60 * 1000)));
  const [to, setTo] = useState(() => isoDate(new Date()));
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api<Metrics>("/api/admin/metrics", { query: { from, to } })
      .then((m) => {
        setMetrics(m);
        setError(null);
      })
      .catch((e) => setError(errorMessage(e)));
  }, [from, to]);

  const rates: { label: string; value: number; target: number; detail: string }[] = metrics
    ? [
        { label: "투표율", value: metrics.voteRate, target: TARGETS.voteRate, detail: `투표 ${metrics.votes} / 이슈별 고유 방문자 ${metrics.issueVisitors} (전체 조회 ${metrics.issueViews})` },
        { label: "의견 작성률", value: metrics.opinionRate, target: TARGETS.opinionRate, detail: `의견 ${metrics.opinions} / 투표 ${metrics.votes}` },
        { label: "반대 의견 열람률", value: metrics.opposingViewRate, target: TARGETS.opposingViewRate, detail: `다른 입장 열람 ${metrics.opposingViewers} / 투표자 ${metrics.voters}` },
        { label: "7일 재방문율", value: metrics.revisit7dRate, target: TARGETS.revisit7dRate, detail: `대상 회원 ${metrics.revisitCohort}명` },
      ]
    : [];

  return (
    <section>
      <div className="mb-4 flex items-center gap-2 text-sm">
        <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className="rounded border p-1" />
        ~
        <input type="date" value={to} onChange={(e) => setTo(e.target.value)} className="rounded border p-1" />
      </div>
      {error && <p className="text-sm text-rose-600">{error}</p>}
      <div className="grid grid-cols-2 gap-3">
        {rates.map((rate) => (
          <div key={rate.label} className="rounded-lg border border-stone-200 p-4">
            <div className="text-sm text-stone-500">{rate.label}</div>
            <div className="text-2xl font-semibold tabular-nums">{rate.value}%</div>
            <div className={`text-xs ${rate.value >= rate.target ? "text-emerald-700" : "text-stone-400"}`}>
              목표 {rate.target}% {rate.value >= rate.target ? "달성" : "미달"}
            </div>
            <div className="mt-1 text-xs text-stone-400">{rate.detail}</div>
          </div>
        ))}
      </div>
      <p className="mt-3 text-xs text-stone-400">
        이슈 조회 수는 비회원·중복 조회를 포함합니다. 지표 정의는 domain-policy.md의 ParticipationEvent를 참고하세요.
      </p>
    </section>
  );
}
