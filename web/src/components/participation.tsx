"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError, errorMessage } from "@/lib/api";
import {
  type OpinionItem,
  type OpinionSort,
  type Page,
  type Participation as ParticipationState,
  STANCE_LABEL,
  STANCES,
  type Stance,
  type VoteResult,
} from "@/lib/types";
import { useMe } from "@/components/me-provider";

const STANCE_COLOR: Record<Stance, string> = {
  AGREE: "bg-sky-600",
  NEUTRAL: "bg-stone-500",
  DISAGREE: "bg-rose-600",
};

type Props = {
  issueId: number;
  open: boolean;
};

/**
 * 이슈 상세의 참여 영역: 투표 → 결과 → 입장별 의견.
 * 결과와 의견은 투표한 회원에게만 보인다.
 */
export function Participation({ issueId, open }: Props) {
  const { me, loading: meLoading } = useMe();
  const [state, setState] = useState<ParticipationState | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchState = useCallback(
    () => api<ParticipationState>(`/api/issues/${issueId}/participation`),
    [issueId],
  );

  const reload = useCallback(async () => {
    try {
      setState(await fetchState());
    } catch (e) {
      setError(errorMessage(e));
    }
  }, [fetchState]);

  useEffect(() => {
    if (meLoading) return;
    fetchState()
      .then(setState)
      .catch((e) => setError(errorMessage(e)));
  }, [meLoading, me, fetchState]);

  if (meLoading || state === null) {
    return <p className="text-sm text-stone-500">{error ?? "불러오는 중…"}</p>;
  }

  if (!me) {
    return (
      <section className="rounded-lg border border-stone-200 p-5 text-center">
        <p className="mb-3">로그인하면 투표하고 다른 사람들의 의견을 볼 수 있어요.</p>
        <a href={`/login?next=/issues/${issueId}`} className="inline-block rounded bg-stone-900 px-4 py-2 text-white">
          로그인
        </a>
      </section>
    );
  }

  if (!state.voted) {
    return open ? (
      <VoteForm issueId={issueId} onVoted={reload} />
    ) : (
      <p className="rounded-lg border border-stone-200 p-5 text-center text-stone-500">투표가 마감된 이슈입니다.</p>
    );
  }

  return (
    <div className="flex flex-col gap-8">
      <Results issueId={issueId} myStance={state.stance!} />
      <MyOpinion issueId={issueId} state={state} onChange={reload} />
      <Opinions issueId={issueId} myStance={state.stance!} />
    </div>
  );
}

function VoteForm({ issueId, onVoted }: { issueId: number; onVoted: () => void }) {
  const [selected, setSelected] = useState<Stance | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit() {
    if (!selected) return;
    setSubmitting(true);
    setError(null);
    try {
      await api(`/api/issues/${issueId}/votes`, { method: "POST", body: { stance: selected } });
      onVoted();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="rounded-lg border border-stone-200 p-5">
      <h2 className="mb-1 font-semibold">당신의 입장은?</h2>
      <p className="mb-4 text-sm text-stone-500">투표는 한 번만 할 수 있고 바꿀 수 없어요. 투표하면 결과와 의견을 볼 수 있어요.</p>
      <div className="mb-4 grid grid-cols-3 gap-2">
        {STANCES.map((stance) => (
          <button
            key={stance}
            type="button"
            onClick={() => setSelected(stance)}
            className={`rounded border py-3 font-medium ${
              selected === stance ? `${STANCE_COLOR[stance]} border-transparent text-white` : "border-stone-300"
            }`}
          >
            {STANCE_LABEL[stance]}
          </button>
        ))}
      </div>
      {error && <p className="mb-2 text-sm text-rose-600">{error}</p>}
      <button
        type="button"
        disabled={!selected || submitting}
        onClick={submit}
        className="w-full rounded bg-stone-900 py-2 text-white disabled:opacity-40"
      >
        투표하기
      </button>
    </section>
  );
}

function Results({ issueId, myStance }: { issueId: number; myStance: Stance }) {
  const [result, setResult] = useState<VoteResult | null>(null);

  useEffect(() => {
    api<VoteResult>(`/api/issues/${issueId}/results`).then(setResult).catch(() => setResult(null));
  }, [issueId]);

  if (!result) return null;

  return (
    <section>
      <h2 className="mb-3 font-semibold">
        참여 결과 <span className="text-sm font-normal text-stone-500">· {result.total}명 참여 · 내 입장 {STANCE_LABEL[myStance]}</span>
      </h2>
      <div className="flex h-8 overflow-hidden rounded">
        {result.stances
          .filter((s) => s.count > 0)
          .map((s) => (
            <div
              key={s.stance}
              className={`${STANCE_COLOR[s.stance]} flex items-center justify-center text-xs text-white`}
              style={{ width: `${s.percentage}%` }}
            >
              {s.percentage >= 10 && `${STANCE_LABEL[s.stance]} ${s.percentage}%`}
            </div>
          ))}
      </div>
      <ul className="mt-2 flex gap-4 text-sm text-stone-600">
        {result.stances.map((s) => (
          <li key={s.stance}>
            {STANCE_LABEL[s.stance]} {s.count}명 ({s.percentage}%)
          </li>
        ))}
      </ul>
      <p className="mt-1 text-xs text-stone-400">{result.notice}</p>
    </section>
  );
}

function MyOpinion({
  issueId,
  state,
  onChange,
}: {
  issueId: number;
  state: ParticipationState;
  onChange: () => void;
}) {
  const opinion = state.opinion;
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(opinion?.content ?? "");
  const [error, setError] = useState<string | null>(null);

  async function remove() {
    if (!opinion || !window.confirm("의견을 삭제할까요? 삭제하면 이 이슈에는 다시 의견을 쓸 수 없어요.")) return;
    setError(null);
    try {
      await api(`/api/opinions/${opinion.id}`, { method: "DELETE" });
      onChange();
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  async function submit() {
    setError(null);
    try {
      if (opinion) {
        await api(`/api/opinions/${opinion.id}`, { method: "PUT", body: { content } });
        setEditing(false);
      } else {
        await api(`/api/issues/${issueId}/opinions`, { method: "POST", body: { content } });
      }
      onChange();
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  if (opinion?.deleted) {
    return (
      <section className="rounded-lg bg-stone-50 p-5 text-sm text-stone-500">
        의견을 삭제했어요. 이 이슈에는 다시 의견을 쓸 수 없어요.
      </section>
    );
  }

  if (opinion && !editing) {
    return (
      <section className="rounded-lg bg-stone-50 p-5">
        <h2 className="mb-2 font-semibold">
          내 의견 <span className="text-sm font-normal text-stone-500">({STANCE_LABEL[opinion.stance]})</span>
        </h2>
        {opinion.hidden && <p className="mb-2 text-sm text-rose-600">운영 정책에 따라 숨김 처리된 의견입니다.</p>}
        <p className="whitespace-pre-wrap">{opinion.content}</p>
        <div className="mt-3 flex gap-3 text-sm text-stone-500">
          {opinion.edited ? (
            <span>수정 완료 (더 이상 수정할 수 없어요)</span>
          ) : (
            <button type="button" onClick={() => setEditing(true)} className="underline">
              수정하기 (1회 가능)
            </button>
          )}
          <button type="button" onClick={remove} className="underline">
            삭제
          </button>
        </div>
        {error && <p className="mt-2 text-sm text-rose-600">{error}</p>}
      </section>
    );
  }

  return (
    <section className="rounded-lg border border-stone-200 p-5">
      <h2 className="mb-1 font-semibold">{opinion ? "의견 수정" : "의견 남기기 (선택)"}</h2>
      <p className="mb-3 text-sm text-stone-500">
        {opinion
          ? "수정은 한 번만 할 수 있어요."
          : "이 이슈에 의견은 하나만 남길 수 있고, 한 번 수정할 수 있어요. 다른 의견에 답글을 달 수는 없어요."}
      </p>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        maxLength={500}
        rows={4}
        className="w-full rounded border border-stone-300 p-2"
        placeholder="왜 그렇게 생각하는지 들려주세요."
      />
      <div className="mt-1 flex items-center justify-between text-sm">
        <span className="text-stone-400">{content.length}/500</span>
        <div className="flex gap-2">
          {opinion && (
            <button type="button" onClick={() => setEditing(false)} className="rounded px-3 py-1">
              취소
            </button>
          )}
          <button
            type="button"
            onClick={submit}
            disabled={!content.trim()}
            className="rounded bg-stone-900 px-3 py-1 text-white disabled:opacity-40"
          >
            {opinion ? "수정 완료" : "등록"}
          </button>
        </div>
      </div>
      {error && <p className="mt-2 text-sm text-rose-600">{error}</p>}
    </section>
  );
}

function Opinions({ issueId, myStance }: { issueId: number; myStance: Stance }) {
  // 다른 입장의 의견을 먼저 보여준다.
  const [tab, setTab] = useState<Stance>(STANCES.find((s) => s !== myStance) ?? "AGREE");
  const [sort, setSort] = useState<OpinionSort>("RECOMMENDED");
  const [items, setItems] = useState<OpinionItem[]>([]);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const fetchPage = useCallback(
    (nextPage: number) =>
      api<Page<OpinionItem>>(`/api/issues/${issueId}/opinions`, {
        query: { stance: tab, sort, page: nextPage, size: 20 },
      }),
    [issueId, tab, sort],
  );

  const apply = useCallback((result: Page<OpinionItem>, nextPage: number) => {
    setItems((prev) => (nextPage === 0 ? result.content : [...prev, ...result.content]));
    setTotal(result.totalElements);
    setPage(nextPage);
  }, []);

  const load = useCallback(
    async (nextPage: number) => {
      try {
        apply(await fetchPage(nextPage), nextPage);
      } catch (e) {
        setError(errorMessage(e));
      }
    },
    [fetchPage, apply],
  );

  useEffect(() => {
    fetchPage(0)
      .then((result) => apply(result, 0))
      .catch((e) => setError(errorMessage(e)));
  }, [fetchPage, apply]);

  function updateItem(id: number, change: Partial<OpinionItem>) {
    setItems((prev) => prev.map((item) => (item.id === id ? { ...item, ...change } : item)));
  }

  return (
    <section>
      <div className="mb-3 flex items-end justify-between border-b border-stone-200">
        <div className="flex">
          {STANCES.map((stance) => (
            <button
              key={stance}
              type="button"
              onClick={() => setTab(stance)}
              className={`px-4 py-2 ${tab === stance ? "border-b-2 border-stone-900 font-semibold" : "text-stone-500"}`}
            >
              {STANCE_LABEL[stance]}
              {stance === myStance && <span className="ml-1 text-xs text-stone-400">(내 입장)</span>}
            </button>
          ))}
        </div>
        <select
          value={sort}
          onChange={(e) => setSort(e.target.value as OpinionSort)}
          className="mb-1 rounded border border-stone-300 px-2 py-1 text-sm"
        >
          <option value="RECOMMENDED">추천순</option>
          <option value="LATEST">최신순</option>
        </select>
      </div>
      {error && <p className="text-sm text-rose-600">{error}</p>}
      {items.length === 0 ? (
        <p className="py-8 text-center text-stone-500">아직 {STANCE_LABEL[tab]} 의견이 없어요.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {items.map((item) => (
            <OpinionCard key={item.id} item={item} onChange={(change) => updateItem(item.id, change)} />
          ))}
        </ul>
      )}
      {items.length < total && (
        <button type="button" onClick={() => load(page + 1)} className="mt-3 w-full rounded border py-2 text-sm">
          더 보기
        </button>
      )}
    </section>
  );
}

function OpinionCard({ item, onChange }: { item: OpinionItem; onChange: (change: Partial<OpinionItem>) => void }) {
  const [message, setMessage] = useState<string | null>(null);

  // 추천했으면 취소, 아니면 추천한다.
  async function toggleRecommendation() {
    const path = `/api/opinions/${item.id}/recommendations`;
    try {
      if (item.recommendedByMe) {
        await api(path, { method: "DELETE" });
        onChange({ recommendedByMe: false, recommendationCount: Math.max(0, item.recommendationCount - 1) });
      } else {
        await api(path, { method: "POST" });
        onChange({ recommendedByMe: true, recommendationCount: item.recommendationCount + 1 });
      }
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) {
        onChange({ recommendedByMe: true });
      } else {
        setMessage(errorMessage(e));
      }
    }
  }

  async function report() {
    const reason = window.prompt("신고 사유를 적어 주세요. (300자 이내)");
    if (!reason?.trim()) return;
    try {
      await api(`/api/opinions/${item.id}/reports`, { method: "POST", body: { reason } });
      setMessage("신고가 접수되었어요.");
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  return (
    <li className="rounded-lg border border-stone-200 p-4">
      <div className="mb-2 flex justify-between text-sm text-stone-500">
        <span>
          {item.authorNickname ?? "알 수 없음"}
          {item.mine && " (나)"}
          {item.edited && " · 수정됨"}
        </span>
        <time dateTime={item.writtenAt}>{new Date(item.writtenAt).toLocaleDateString("ko-KR")}</time>
      </div>
      <p className="whitespace-pre-wrap">{item.content}</p>
      <div className="mt-3 flex items-center gap-3 text-sm">
        {item.mine ? (
          <span className="rounded border border-stone-200 px-2 py-0.5 text-stone-500">추천 {item.recommendationCount}</span>
        ) : (
          <button
            type="button"
            onClick={toggleRecommendation}
            aria-pressed={item.recommendedByMe}
            title={item.recommendedByMe ? "추천 취소" : "추천"}
            className={`rounded border px-2 py-0.5 ${item.recommendedByMe ? "border-stone-900 font-semibold" : "border-stone-300"}`}
          >
            추천 {item.recommendationCount}
          </button>
        )}
        {!item.mine && (
          <button type="button" onClick={report} className="text-stone-400 underline">
            신고
          </button>
        )}
        {message && <span className="text-stone-500">{message}</span>}
      </div>
    </li>
  );
}
