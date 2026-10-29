"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError, errorMessage } from "@/lib/api";
import { formatDate } from "@/lib/format";
import { STANCE_STYLE } from "@/lib/stance";
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

function StanceDot({ stance, className = "size-2" }: { stance: Stance; className?: string }) {
  return <span aria-hidden className={`inline-block shrink-0 rounded-full ${className} ${STANCE_STYLE[stance].solid}`} />;
}

// 의견 목록에서 먼저 여는 탭: 나와 반대되는 입장. 중립 투표자는 찬성부터 본다.
const WHY: Record<Stance, string> = { AGREE: "왜 찬성했나요?", NEUTRAL: "왜 중립을 골랐나요?", DISAGREE: "왜 반대했나요?" };

const OPPOSITE: Record<Stance, Stance> = { AGREE: "DISAGREE", DISAGREE: "AGREE", NEUTRAL: "AGREE" };

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
    return error ? <p className="error-text">{error}</p> : <div className="h-40 animate-pulse rounded-2xl bg-ink/5" />;
  }

  if (!me) {
    return (
      <>
        <section id="vote" className="card scroll-mt-20">
          <h2 className="mb-1 text-lg font-bold">당신의 입장은?</h2>
          <p className="mb-5 text-sm text-muted">입장을 고르고 가입하면 투표하고, 사람들이 왜 다르게 생각하는지 볼 수 있어요.</p>
          <div className="mb-4 grid grid-cols-3 gap-2 sm:gap-3">
            {STANCES.map((stance) => (
              <a
                key={stance}
                href={`/signup?next=/issues/${issueId}`}
                className="flex flex-col items-center gap-2 rounded-xl border-2 border-line py-4 font-semibold transition-colors hover:border-subtle sm:py-5"
              >
                <StanceDot stance={stance} className="size-3" />
                {STANCE_LABEL[stance]}
              </a>
            ))}
          </div>
          <p className="text-center text-sm text-muted">
            이미 계정이 있어요 ·{" "}
            <a href={`/login?next=/issues/${issueId}`} className="link font-semibold text-ink">
              로그인
            </a>
          </p>
        </section>
        {open && <VoteBar />}
      </>
    );
  }

  if (!state.voted) {
    return open ? (
      <>
        <VoteForm issueId={issueId} onVoted={reload} />
        <VoteBar />
      </>
    ) : (
      <p className="card text-center text-muted">투표가 마감된 이슈입니다. 아래에서 다른 이슈에 참여해 보세요.</p>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <Results issueId={issueId} myStance={state.stance!} />
      <MyOpinion issueId={issueId} state={state} onChange={reload} />
      <Opinions issueId={issueId} myStance={state.stance!} />
    </div>
  );
}

/**
 * 모바일에서 투표 카드가 화면 아래에 있을 때만 보이는 하단 고정 버튼. 투표 카드가 보이거나 지나가면 숨긴다.
 */
function VoteBar() {
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    const target = document.getElementById("vote");
    if (!target) return;
    const observer = new IntersectionObserver(([entry]) => {
      setHidden(entry.isIntersecting || entry.boundingClientRect.top < 0);
    });
    observer.observe(target);
    return () => observer.disconnect();
  }, []);

  if (hidden) return null;
  return (
    <div className="fixed inset-x-0 bottom-0 z-20 border-t border-line bg-surface/95 px-4 pt-3 pb-[calc(env(safe-area-inset-bottom)+12px)] backdrop-blur sm:hidden">
      <a href="#vote" className="btn btn-primary w-full py-3">
        내 입장 고르기
      </a>
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
    <section id="vote" className="card scroll-mt-20">
      <h2 className="mb-1 text-lg font-bold">당신의 입장은?</h2>
      <p className="mb-5 text-sm text-muted">투표는 한 번만 할 수 있고 바꿀 수 없어요. 투표하면 결과와 의견을 볼 수 있어요.</p>
      <div role="radiogroup" aria-label="입장" className="mb-5 grid grid-cols-3 gap-2 sm:gap-3">
        {STANCES.map((stance) => {
          const active = selected === stance;
          const style = STANCE_STYLE[stance];
          return (
            <button
              key={stance}
              type="button"
              role="radio"
              aria-checked={active}
              onClick={() => setSelected(stance)}
              className={`flex flex-col items-center gap-2 rounded-xl border-2 py-4 font-semibold transition-colors sm:py-5 ${
                active ? `${style.border} ${style.soft} ${style.text}` : "border-line hover:border-subtle"
              }`}
            >
              <StanceDot stance={stance} className="size-3" />
              {STANCE_LABEL[stance]}
            </button>
          );
        })}
      </div>
      {error && <p className="error-text mb-3">{error}</p>}
      <button type="button" disabled={!selected || submitting} onClick={submit} className="btn btn-primary w-full py-3">
        {selected ? `${STANCE_LABEL[selected]}에 투표하기` : "입장을 골라 주세요"}
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
    <section className="card">
      <div className="mb-4 flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="text-lg font-bold">참여 결과</h2>
        <p className="text-sm text-muted">
          {result.total.toLocaleString("ko-KR")}명 참여 · 내 입장{" "}
          <span className={`font-semibold ${STANCE_STYLE[myStance].text}`}>{STANCE_LABEL[myStance]}</span>
        </p>
      </div>
      <div className="flex h-3 gap-0.5 overflow-hidden rounded-full bg-ink/5">
        {result.stances
          .filter((s) => s.count > 0)
          .map((s) => (
            <div key={s.stance} className={STANCE_STYLE[s.stance].solid} style={{ width: `${s.percentage}%` }} />
          ))}
      </div>
      <ul className="mt-4 grid grid-cols-3 gap-2">
        {result.stances.map((s) => (
          <li
            key={s.stance}
            className={`rounded-xl px-3 py-2.5 ${s.stance === myStance ? STANCE_STYLE[s.stance].soft : "bg-ink/[0.03]"}`}
          >
            <div className="flex items-center gap-1.5 text-xs font-medium text-muted">
              <StanceDot stance={s.stance} />
              {STANCE_LABEL[s.stance]}
            </div>
            <div className="mt-1 text-xl font-bold tabular-nums">{s.percentage}%</div>
            <div className="text-xs text-subtle tabular-nums">{s.count.toLocaleString("ko-KR")}명</div>
          </li>
        ))}
      </ul>
      <p className="mt-3 text-xs text-subtle">{result.notice}</p>
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
  // 의견은 선택이라 처음에는 한 줄로 접어 두고, 다른 입장 의견이 결과 바로 아래에 보이게 한다.
  const [writing, setWriting] = useState(false);
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
    return <section className="notice">의견을 삭제했어요. 이 이슈에는 다시 의견을 쓸 수 없어요.</section>;
  }

  if (opinion && !editing) {
    return (
      <section className={`rounded-2xl p-5 sm:p-6 ${STANCE_STYLE[opinion.stance].soft}`}>
        <h2 className="mb-2 flex items-center gap-2 text-sm font-semibold">
          <StanceDot stance={opinion.stance} />내 의견 · {STANCE_LABEL[opinion.stance]}
        </h2>
        {opinion.hidden && <p className="error-text mb-2">운영 정책에 따라 숨김 처리된 의견입니다.</p>}
        <p className="leading-relaxed whitespace-pre-wrap">{opinion.content}</p>
        <div className="mt-4 flex flex-wrap items-center gap-1">
          {opinion.edited ? (
            <span className="mr-2 text-xs text-muted">수정 완료 (더 이상 수정할 수 없어요)</span>
          ) : (
            <button type="button" onClick={() => setEditing(true)} className="btn btn-sm btn-ghost -ml-3">
              수정하기 (1회 가능)
            </button>
          )}
          <button type="button" onClick={remove} className="btn btn-sm btn-ghost">
            삭제
          </button>
        </div>
        {error && <p className="error-text mt-2">{error}</p>}
      </section>
    );
  }

  if (!opinion && !writing) {
    return (
      <button
        type="button"
        onClick={() => setWriting(true)}
        className={`flex w-full items-center justify-between gap-3 rounded-2xl px-5 py-4 text-left transition-colors hover:brightness-[0.98] ${STANCE_STYLE[state.stance!].soft}`}
      >
        <span className="flex items-center gap-2 font-semibold">
          <StanceDot stance={state.stance!} />
          {WHY[state.stance!]}
          <span className="text-sm font-normal text-muted">이유 남기기 (선택)</span>
        </span>
        <span aria-hidden className="text-subtle">
          ＋
        </span>
      </button>
    );
  }

  return (
    <section className="card">
      <h2 className="mb-1 text-lg font-bold">
        {opinion ? "의견 수정" : "왜 그렇게 생각하나요?"}
        {!opinion && <span className="ml-2 text-sm font-normal text-subtle">선택</span>}
      </h2>
      <p className="mb-4 text-sm text-muted">
        {opinion
          ? "수정은 한 번만 할 수 있어요."
          : "이 이슈에 의견은 하나만 남길 수 있고, 한 번 수정할 수 있어요. 다른 의견에 답글을 달 수는 없어요."}
      </p>
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        maxLength={500}
        rows={4}
        className="field resize-y leading-relaxed"
        placeholder="내 입장의 이유를 들려주세요. 다른 입장의 사람도 읽게 돼요."
      />
      <div className="mt-3 flex items-center justify-between">
        <span className="text-xs text-subtle tabular-nums">{content.length}/500</span>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => (opinion ? setEditing(false) : setWriting(false))}
            className="btn btn-sm btn-ghost"
          >
            취소
          </button>
          <button type="button" onClick={submit} disabled={!content.trim()} className="btn btn-sm btn-primary">
            {opinion ? "수정 완료" : "등록"}
          </button>
        </div>
      </div>
      {error && <p className="error-text mt-2">{error}</p>}
    </section>
  );
}

function Opinions({ issueId, myStance }: { issueId: number; myStance: Stance }) {
  // 다른 입장의 의견을 먼저 보여준다. 찬성↔반대, 중립은 찬성부터.
  const [tab, setTab] = useState<Stance>(OPPOSITE[myStance]);
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

  // 열린 '⋯' 메뉴는 바깥을 누르면 닫는다.
  useEffect(() => {
    function closeMenus(event: MouseEvent) {
      document.querySelectorAll("details[data-opinion-menu][open]").forEach((menu) => {
        if (!menu.contains(event.target as Node)) menu.removeAttribute("open");
      });
    }
    document.addEventListener("click", closeMenus);
    return () => document.removeEventListener("click", closeMenus);
  }, []);

  // 의견은 이슈당 회원 1개이므로 차단한 작성자의 의견은 이 카드 하나뿐이다.
  function removeItem(id: number) {
    setItems((prev) => prev.filter((item) => item.id !== id));
    setTotal((prev) => Math.max(0, prev - 1));
  }

  return (
    <section className="mt-2">
      <div className="mb-1 flex items-baseline justify-between gap-2">
        <h2 className="text-lg font-bold">입장별 의견</h2>
        <select
          value={sort}
          onChange={(e) => setSort(e.target.value as OpinionSort)}
          aria-label="정렬"
          className="rounded-lg bg-transparent py-1 text-sm text-muted focus:outline-none"
        >
          <option value="RECOMMENDED">추천순</option>
          <option value="LATEST">최신순</option>
        </select>
      </div>
      <p className="mb-4 text-sm text-muted">나와 다른 입장의 의견부터 보여드려요.</p>
      <div role="tablist" className="mb-4 grid grid-cols-3 gap-1 rounded-xl bg-ink/5 p-1">
        {STANCES.map((stance) => {
          const active = tab === stance;
          return (
            <button
              key={stance}
              type="button"
              role="tab"
              aria-selected={active}
              onClick={() => setTab(stance)}
              className={`flex items-center justify-center gap-1.5 rounded-lg py-2 text-sm transition-colors ${
                active ? "bg-surface font-semibold shadow-sm" : "text-muted hover:text-ink"
              }`}
            >
              <StanceDot stance={stance} />
              {STANCE_LABEL[stance]}
              {stance === myStance && <span className="text-[11px] font-normal text-subtle">나</span>}
            </button>
          );
        })}
      </div>
      {error && <p className="error-text">{error}</p>}
      {items.length === 0 ? (
        <p className="card py-12 text-center text-muted">아직 {STANCE_LABEL[tab]} 의견이 없어요.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {items.map((item) => (
            <OpinionCard
              key={item.id}
              item={item}
              onChange={(change) => updateItem(item.id, change)}
              onBlocked={() => removeItem(item.id)}
            />
          ))}
        </ul>
      )}
      {items.length < total && (
        <button type="button" onClick={() => load(page + 1)} className="btn btn-secondary mt-3 w-full">
          더 보기
        </button>
      )}
    </section>
  );
}

function OpinionCard({
  item,
  onChange,
  onBlocked,
}: {
  item: OpinionItem;
  onChange: (change: Partial<OpinionItem>) => void;
  onBlocked: () => void;
}) {
  const [message, setMessage] = useState<string | null>(null);
  const [reporting, setReporting] = useState(false);
  const [reason, setReason] = useState("");

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
    if (!reason.trim()) return;
    try {
      await api(`/api/opinions/${item.id}/reports`, { method: "POST", body: { reason } });
      setReporting(false);
      setReason("");
      setMessage("신고가 접수되었어요.");
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  async function blockAuthor() {
    if (
      !window.confirm(
        `${item.authorNickname}님을 차단할까요? 이 회원의 의견이 더 이상 보이지 않아요. 상대에게는 알리지 않고, 내 정보에서 해제할 수 있어요.`,
      )
    )
      return;
    try {
      await api(`/api/opinions/${item.id}/author-block`, { method: "POST" });
      onBlocked();
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  // 입장 색은 왼쪽 테두리로 낸다. overflow-hidden을 쓰면 '⋯' 메뉴가 카드 안에서 잘린다.
  return (
    <li className={`card border-l-4 py-4 sm:py-5 ${STANCE_STYLE[item.stance].borderLeft}`}>
      <div className="mb-2 flex items-center justify-between gap-2 text-sm">
        <span className="flex min-w-0 items-center gap-2">
          <span
            aria-hidden
            className="flex size-7 shrink-0 items-center justify-center rounded-full bg-ink/5 text-xs font-semibold text-muted"
          >
            {(item.authorNickname ?? "?").slice(0, 1)}
          </span>
          <span className="truncate font-medium">{item.authorNickname ?? "알 수 없음"}</span>
          {item.mine && <span className="chip">나</span>}
        </span>
        <span className="shrink-0 text-xs text-subtle">
          {item.edited && "수정됨 · "}
          <time dateTime={item.writtenAt}>{formatDate(item.writtenAt)}</time>
        </span>
      </div>
      <p className="leading-relaxed whitespace-pre-wrap">{item.content}</p>
      <div className="mt-3 flex flex-wrap items-center gap-1">
        {item.mine ? (
          <span className="chip">추천 {item.recommendationCount}</span>
        ) : (
          <button
            type="button"
            onClick={toggleRecommendation}
            aria-pressed={item.recommendedByMe}
            title={item.recommendedByMe ? "추천 취소" : "추천"}
            className={`btn btn-sm ${item.recommendedByMe ? "btn-primary" : "btn-secondary"}`}
          >
            추천 {item.recommendationCount}
          </button>
        )}
        {!item.mine && (
          <details data-opinion-menu className="group relative ml-auto">
            <summary
              aria-label="더 보기"
              className="flex size-9 cursor-pointer list-none items-center justify-center rounded-lg text-subtle hover:bg-ink/5 [&::-webkit-details-marker]:hidden"
            >
              ⋯
            </summary>
            <div className="absolute right-0 z-10 mt-1 flex w-32 flex-col rounded-xl border border-line bg-surface p-1 shadow-lg">
              <button
                type="button"
                onClick={(e) => {
                  setReporting(true);
                  e.currentTarget.closest("details")?.removeAttribute("open");
                }}
                className="rounded-lg px-3 py-2.5 text-left text-sm hover:bg-ink/5"
              >
                신고
              </button>
              <button
                type="button"
                onClick={(e) => {
                  e.currentTarget.closest("details")?.removeAttribute("open");
                  blockAuthor();
                }}
                className="rounded-lg px-3 py-2.5 text-left text-sm hover:bg-ink/5"
              >
                이 회원 차단
              </button>
            </div>
          </details>
        )}
        {message && <span className="w-full text-xs text-muted">{message}</span>}
      </div>
      {reporting && (
        <div className="mt-3 flex flex-col gap-2 rounded-xl bg-ink/[0.03] p-3">
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            maxLength={300}
            rows={2}
            placeholder="신고 사유를 적어 주세요. (300자 이내)"
            className="field text-sm"
          />
          <div className="flex justify-end gap-1">
            <button type="button" onClick={() => setReporting(false)} className="btn btn-sm btn-ghost">
              취소
            </button>
            <button type="button" onClick={report} disabled={!reason.trim()} className="btn btn-sm btn-primary">
              신고하기
            </button>
          </div>
        </div>
      )}
    </li>
  );
}
