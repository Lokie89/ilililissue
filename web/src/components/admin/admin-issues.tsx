"use client";

import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
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
    <div className="flex flex-col gap-8">
      {message && <p className="rounded bg-stone-100 p-2 text-sm">{message}</p>}

      <section className="rounded-lg border border-stone-200 p-5">
        <h2 className="mb-1 font-semibold">{editingId ? `이슈 #${editingId} 수정` : "새 이슈 작성"}</h2>
        <p className="mb-4 text-sm text-stone-500">
          기사 본문·사진을 복사하지 말고, 사실관계를 확인해 직접 쓴 중립적인 질문과 설명, 원문 링크만 넣어 주세요.
        </p>
        <div className="flex flex-col gap-3">
          <input
            value={draft.title}
            onChange={(e) => setDraft({ ...draft, title: e.target.value })}
            maxLength={100}
            placeholder="질문 (유도 문구 없이, 100자 이내)"
            className="rounded border border-stone-300 p-2"
          />
          <textarea
            value={draft.summary}
            onChange={(e) => setDraft({ ...draft, summary: e.target.value })}
            maxLength={1000}
            rows={5}
            placeholder="배경 설명과 사실관계 (1000자 이내)"
            className="rounded border border-stone-300 p-2"
          />
          <select
            value={draft.categoryId}
            onChange={(e) => setDraft({ ...draft, categoryId: e.target.value ? Number(e.target.value) : "" })}
            className="rounded border border-stone-300 p-2"
          >
            <option value="">카테고리 선택</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <div className="flex flex-col gap-2">
            <span className="text-sm font-medium">원문 출처</span>
            {draft.sources.map((source, index) => (
              <div key={index} className="grid grid-cols-[2fr_1fr_1fr_auto] gap-2">
                <input
                  value={source.url}
                  onChange={(e) => updateSource(index, { url: e.target.value })}
                  placeholder="https://..."
                  className="rounded border border-stone-300 p-2 text-sm"
                />
                <input
                  value={source.publisher}
                  onChange={(e) => updateSource(index, { publisher: e.target.value })}
                  placeholder="언론사"
                  className="rounded border border-stone-300 p-2 text-sm"
                />
                <input
                  type="date"
                  value={source.publishedOn}
                  onChange={(e) => updateSource(index, { publishedOn: e.target.value })}
                  className="rounded border border-stone-300 p-2 text-sm"
                />
                <button
                  type="button"
                  onClick={() =>
                    setDraft((prev) => ({ ...prev, sources: prev.sources.filter((_, i) => i !== index) }))
                  }
                  className="px-2 text-stone-400"
                >
                  삭제
                </button>
              </div>
            ))}
            <button
              type="button"
              onClick={() => setDraft((prev) => ({ ...prev, sources: [...prev.sources, { ...EMPTY_SOURCE }] }))}
              className="self-start text-sm underline"
            >
              출처 추가
            </button>
          </div>
          <div className="flex gap-2">
            <button type="button" onClick={saveDraft} className="rounded bg-stone-900 px-4 py-2 text-white">
              {editingId ? "수정 저장" : "작성"}
            </button>
            {editingId && (
              <button type="button" onClick={resetDraft} className="rounded px-4 py-2">
                취소
              </button>
            )}
          </div>
        </div>
      </section>

      <section className="rounded-lg border border-stone-200 p-5">
        <h2 className="mb-3 font-semibold">카테고리</h2>
        <div className="mb-3 flex flex-wrap gap-2 text-sm">
          {categories.map((c) => (
            <span key={c.id} className="rounded bg-stone-100 px-2 py-1">
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
            className="rounded border border-stone-300 p-2 text-sm"
          />
          <button
            type="button"
            onClick={() =>
              run(async () => {
                await api("/api/admin/categories", { method: "POST", body: { name: newCategory } });
                setNewCategory("");
              }, "카테고리를 추가했습니다.")
            }
            className="rounded border px-3 text-sm"
          >
            추가
          </button>
        </div>
      </section>

      <section>
        <h2 className="mb-3 font-semibold">전체 이슈</h2>
        <table className="w-full text-sm">
          <thead className="border-b text-left text-stone-500">
            <tr>
              <th className="py-2">#</th>
              <th>질문</th>
              <th>카테고리</th>
              <th>상태</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {issues.map((issue) => (
              <tr key={issue.id} className="border-b border-stone-100">
                <td className="py-2">{issue.id}</td>
                <td>{issue.title}</td>
                <td>{issue.category?.name}</td>
                <td>{ISSUE_STATUS_LABEL[issue.status]}</td>
                <td className="flex flex-wrap justify-end gap-2 py-2">
                  {(issue.status === "DRAFT" || issue.status === "REVIEWED") && (
                    <button type="button" onClick={() => startEdit(issue.id)} className="underline">
                      수정
                    </button>
                  )}
                  {issue.status === "DRAFT" && (
                    <button
                      type="button"
                      onClick={() => run(() => api(`/api/admin/issues/${issue.id}/review`, { method: "POST" }), "검수 완료")}
                      className="underline"
                    >
                      검수 완료
                    </button>
                  )}
                  {issue.status === "REVIEWED" && (
                    <button
                      type="button"
                      onClick={() => run(() => api(`/api/admin/issues/${issue.id}/publish`, { method: "POST" }), "게시했습니다.")}
                      className="font-semibold underline"
                    >
                      게시
                    </button>
                  )}
                  {issue.status === "PUBLISHED" && (
                    <button
                      type="button"
                      onClick={() => run(() => api(`/api/admin/issues/${issue.id}/close`, { method: "POST" }), "마감했습니다.")}
                      className="underline"
                    >
                      마감
                    </button>
                  )}
                  {(issue.status === "PUBLISHED" || issue.status === "CLOSED") && (
                    <button type="button" onClick={() => correct(issue.id)} className="underline">
                      정정
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
