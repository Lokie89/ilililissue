"use client";

import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
import { formatDateTime } from "@/lib/format";
import { IssueImage } from "@/components/issue-image";
import { AdminNewsDrafts } from "./admin-news-drafts";
import {
  type Category,
  type IssueDetail,
  type IssueSource,
  ISSUE_STATUS_LABEL,
  type IssueSummary,
  type Page,
} from "@/lib/types";

const EMPTY_SOURCE: IssueSource = { url: "", publisher: "", publishedOn: "" };

type Draft = {
  title: string;
  summary: string;
  categoryId: number | "";
  sources: IssueSource[];
};

const EMPTY_DRAFT: Draft = { title: "", summary: "", categoryId: "", sources: [{ ...EMPTY_SOURCE }] };

function fetchAll(): Promise<[Page<IssueSummary>, Category[]]> {
  return Promise.all([
    api<Page<IssueSummary>>("/api/admin/issues", { query: { size: 50 } }),
    api<Category[]>("/api/categories"),
  ]);
}

export function AdminIssues() {
  const [issues, setIssues] = useState<IssueSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [draft, setDraft] = useState<Draft>(EMPTY_DRAFT);
  const [newCategory, setNewCategory] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [imageGenerating, setImageGenerating] = useState<number[]>([]);

  const apply = useCallback(([issuePage, categoryList]: [Page<IssueSummary>, Category[]]) => {
    setIssues(issuePage.content);
    setCategories(categoryList);
  }, []);

  const reload = useCallback(async () => apply(await fetchAll()), [apply]);

  useEffect(() => {
    fetchAll()
      .then(apply)
      .catch((e) => setMessage(errorMessage(e)));
  }, [apply]);

  async function run(action: () => Promise<unknown>, done: string) {
    setMessage(null);
    try {
      await action();
      setMessage(done);
      await reload();
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  async function startEdit(id: number) {
    const issue = await api<IssueDetail>(`/api/admin/issues/${id}`);
    setEditingId(id);
    setDraft({
      title: issue.title,
      summary: issue.summary,
      categoryId: issue.category?.id ?? "",
      sources: issue.sources.length ? issue.sources : [{ ...EMPTY_SOURCE }],
    });
  }

  function resetDraft() {
    setEditingId(null);
    setDraft(EMPTY_DRAFT);
  }

  async function saveDraft() {
    const body = { ...draft, sources: draft.sources.filter((s) => s.url.trim()) };
    await run(async () => {
      if (editingId) {
        await api(`/api/admin/issues/${editingId}`, { method: "PUT", body });
      } else {
        await api("/api/admin/issues", { method: "POST", body });
      }
      resetDraft();
    }, editingId ? "수정했습니다. 다시 검수가 필요합니다." : "작성했습니다.");
  }

  // 이미지는 백그라운드로 1~2분 걸린다. 끝날 때까지 5초마다 확인하고 목록을 새로 읽는다.
  async function generateImage(id: number) {
    setMessage(null);
    try {
      await api(`/api/admin/issues/${id}/image`, { method: "POST" });
    } catch (e) {
      setMessage(errorMessage(e));
      return;
    }
    setImageGenerating((prev) => [...prev, id]);
    setMessage(`#${id} 이미지를 만드는 중이에요. 1~2분 걸려요.`);
    const deadline = Date.now() + 5 * 60 * 1000;
    while (Date.now() < deadline) {
      await new Promise((resolve) => setTimeout(resolve, 5000));
      try {
        const status = await api<{ generating: boolean }>(`/api/admin/issues/${id}/image/status`);
        if (!status.generating) break;
      } catch {
        break;
      }
    }
    setImageGenerating((prev) => prev.filter((value) => value !== id));
    await reload();
    setMessage(`#${id} 이미지 작업이 끝났어요. 이미지가 없으면 다시 시도해 주세요.`);
  }

  async function correct(id: number) {
    const summary = window.prompt("정정된 배경 설명을 입력하세요.");
    if (!summary?.trim()) return;
    await run(() => api(`/api/admin/issues/${id}/correction`, { method: "POST", body: { summary } }), "정정했습니다.");
  }

  function updateSource(index: number, change: Partial<IssueSource>) {
    setDraft((prev) => ({
      ...prev,
      sources: prev.sources.map((source, i) => (i === index ? { ...source, ...change } : source)),
    }));
  }

  return (
    <div className="flex flex-col gap-4">
      {message && <p className="notice">{message}</p>}

      <AdminNewsDrafts onFinished={reload} />

      <section className="card">
        <h2 className="mb-1 font-bold">{editingId ? `이슈 #${editingId} 수정` : "새 이슈 작성"}</h2>
        <p className="mb-4 text-sm text-muted">
          기사 본문·사진을 복사하지 말고, 사실관계를 확인해 직접 쓴 중립적인 질문과 설명, 원문 링크만 넣어 주세요.
        </p>
        <div className="flex flex-col gap-3">
          <input
            value={draft.title}
            onChange={(e) => setDraft({ ...draft, title: e.target.value })}
            maxLength={100}
            placeholder="질문 (유도 문구 없이, 100자 이내)"
            className="field"
          />
          <textarea
            value={draft.summary}
            onChange={(e) => setDraft({ ...draft, summary: e.target.value })}
            maxLength={1000}
            rows={5}
            placeholder="배경 설명과 사실관계 (1000자 이내)"
            className="field"
          />
          <select
            value={draft.categoryId}
            onChange={(e) => setDraft({ ...draft, categoryId: e.target.value ? Number(e.target.value) : "" })}
            className="field"
          >
            <option value="">카테고리 선택</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <div className="flex flex-col gap-2">
            <span className="eyebrow mt-2">원문 출처</span>
            {draft.sources.map((source, index) => (
              <div key={index} className="grid grid-cols-2 gap-2 rounded-xl border border-line p-2 sm:grid-cols-[2fr_1fr_1fr_auto] sm:border-0 sm:p-0">
                <input
                  value={source.url}
                  onChange={(e) => updateSource(index, { url: e.target.value })}
                  placeholder="https://..."
                  className="field col-span-2 text-sm sm:col-span-1"
                />
                <input
                  value={source.publisher}
                  onChange={(e) => updateSource(index, { publisher: e.target.value })}
                  placeholder="언론사"
                  className="field text-sm"
                />
                <input
                  type="date"
                  value={source.publishedOn}
                  onChange={(e) => updateSource(index, { publishedOn: e.target.value })}
                  className="field text-sm"
                />
                <button
                  type="button"
                  onClick={() =>
                    setDraft((prev) => ({ ...prev, sources: prev.sources.filter((_, i) => i !== index) }))
                  }
                  className="btn btn-sm btn-ghost col-span-2 sm:col-span-1"
                >
                  삭제
                </button>
              </div>
            ))}
            <button
              type="button"
              onClick={() => setDraft((prev) => ({ ...prev, sources: [...prev.sources, { ...EMPTY_SOURCE }] }))}
              className="btn btn-sm btn-secondary self-start"
            >
              출처 추가
            </button>
          </div>
          <div className="flex gap-2">
            <button type="button" onClick={saveDraft} className="btn btn-primary">
              {editingId ? "수정 저장" : "작성"}
            </button>
            {editingId && (
              <button type="button" onClick={resetDraft} className="btn btn-ghost">
                취소
              </button>
            )}
          </div>
        </div>
      </section>

      <section className="card">
        <h2 className="mb-3 font-bold">카테고리</h2>
        <div className="mb-3 flex flex-wrap gap-2 text-sm">
          {categories.map((c) => (
            <span key={c.id} className="chip">
              {c.name}
            </span>
          ))}
        </div>
        <div className="flex gap-2">
          <input
            value={newCategory}
            onChange={(e) => setNewCategory(e.target.value)}
            maxLength={20}
            placeholder="새 카테고리"
            className="field text-sm"
          />
          <button
            type="button"
            onClick={() =>
              run(async () => {
                await api("/api/admin/categories", { method: "POST", body: { name: newCategory } });
                setNewCategory("");
              }, "카테고리를 추가했습니다.")
            }
            className="btn btn-sm btn-secondary"
          >
            추가
          </button>
        </div>
      </section>

      <section className="card p-0 sm:p-0">
        <h2 className="px-5 pt-5 font-bold sm:px-6">전체 이슈</h2>
        <ul className="mt-3 divide-y divide-line">
          {issues.map((issue) => (
            <li key={issue.id} className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
              {issue.imageKey ? (
                <IssueImage issueId={issue.id} imageKey={issue.imageKey} admin className="w-full shrink-0 rounded-lg sm:w-28" />
              ) : (
                <div className="hidden aspect-video w-28 shrink-0 items-center justify-center rounded-lg bg-ink/5 text-xs text-subtle sm:flex">
                  이미지 없음
                </div>
              )}
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2 text-xs text-subtle">
                  <span>#{issue.id}</span>
                  <span className={`chip ${issue.status === "PUBLISHED" ? "bg-emerald-50 text-emerald-700" : ""}`}>
                    {ISSUE_STATUS_LABEL[issue.status]}
                  </span>
                  {issue.category && <span>{issue.category.name}</span>}
                  {issue.extendedAt && <span>· {formatDateTime(issue.extendedAt)} 연장</span>}
                </div>
                <p className="mt-1 font-medium">{issue.title}</p>
              </div>
              <div className="flex shrink-0 flex-wrap gap-1.5">
                  {(issue.status === "DRAFT" || issue.status === "REVIEWED") && (
                    <button type="button" onClick={() => startEdit(issue.id)} className="btn btn-sm btn-secondary">
                      수정
                    </button>
                  )}
                  {issue.status === "DRAFT" && (
                    <button
                      type="button"
                      onClick={() => run(() => api(`/api/admin/issues/${issue.id}/review`, { method: "POST" }), "검수 완료")}
                      className="btn btn-sm btn-secondary"
                    >
                      검수 완료
                    </button>
                  )}
                  {issue.status === "REVIEWED" && (
                    <button
                      type="button"
                      onClick={() => run(() => api(`/api/admin/issues/${issue.id}/publish`, { method: "POST" }), "게시했습니다.")}
                      className="btn btn-sm btn-primary"
                    >
                      게시
                    </button>
                  )}
                  {issue.status === "PUBLISHED" && (
                    <button
                      type="button"
                      onClick={() => run(() => api(`/api/admin/issues/${issue.id}/close`, { method: "POST" }), "마감했습니다.")}
                      className="btn btn-sm btn-secondary"
                    >
                      마감
                    </button>
                  )}
                  {(issue.status === "PUBLISHED" || issue.status === "CLOSED") && (
                    <button
                      type="button"
                      onClick={() =>
                        run(
                          () => api(`/api/admin/issues/${issue.id}/extend`, { method: "POST" }),
                          "연장했습니다. 내일 오전 6시 59분에 다시 마감됩니다.",
                        )
                      }
                      className="btn btn-sm btn-secondary"
                    >
                      {issue.status === "CLOSED" ? "다시 열기(연장)" : "연장"}
                    </button>
                  )}
                  {(issue.status === "PUBLISHED" || issue.status === "CLOSED") && (
                    <button type="button" onClick={() => correct(issue.id)} className="btn btn-sm btn-secondary">
                      정정
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={() => generateImage(issue.id)}
                    disabled={imageGenerating.includes(issue.id)}
                    className="btn btn-sm btn-secondary"
                  >
                    {imageGenerating.includes(issue.id) ? "이미지 만드는 중…" : issue.imageKey ? "이미지 다시 만들기" : "이미지 만들기"}
                  </button>
                  {issue.imageKey && (
                    <button
                      type="button"
                      onClick={() => {
                        if (window.confirm("대표 이미지를 지울까요?")) {
                          run(() => api(`/api/admin/issues/${issue.id}/image`, { method: "DELETE" }), "이미지를 지웠습니다.");
                        }
                      }}
                      className="btn btn-sm btn-ghost"
                    >
                      이미지 삭제
                    </button>
                  )}
                </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
