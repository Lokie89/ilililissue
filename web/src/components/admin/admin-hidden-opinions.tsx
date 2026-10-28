"use client";

import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
import { type HiddenOpinion, type Page, STANCE_LABEL } from "@/lib/types";

/**
 * 숨긴 의견 목록. 잘못 숨긴 의견은 되돌릴 수 있다.
 */
export function AdminHiddenOpinions() {
  const [opinions, setOpinions] = useState<HiddenOpinion[]>([]);
  const [message, setMessage] = useState<string | null>(null);

  const fetchHidden = useCallback(
    () => api<Page<HiddenOpinion>>("/api/admin/opinions/hidden", { query: { size: 50 } }),
    [],
  );

  useEffect(() => {
    fetchHidden()
      .then((page) => setOpinions(page.content))
      .catch((e) => setMessage(errorMessage(e)));
  }, [fetchHidden]);

  async function unhide(id: number) {
    if (!window.confirm("이 의견을 다시 보이게 할까요?")) return;
    setMessage(null);
    try {
      await api(`/api/admin/opinions/${id}/unhide`, { method: "POST" });
      setOpinions((await fetchHidden()).content);
    } catch (e) {
      setMessage(errorMessage(e));
    }
  }

  return (
    <section>
      {message && <p className="mb-2 text-sm text-rose-600">{message}</p>}
      {opinions.length === 0 ? (
        <p className="py-6 text-center text-stone-500">숨긴 의견이 없습니다.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {opinions.map((opinion) => (
            <li key={opinion.id} className="rounded-lg border border-stone-200 p-4 text-sm">
              <div className="mb-1 text-stone-500">
                의견 #{opinion.id} · 이슈 #{opinion.issueId} · {STANCE_LABEL[opinion.stance]} ·{" "}
                {new Date(opinion.writtenAt).toLocaleString("ko-KR")}
              </div>
              <p className="mb-2 whitespace-pre-wrap rounded bg-stone-50 p-2">{opinion.content}</p>
              <button type="button" onClick={() => unhide(opinion.id)} className="rounded border px-3 py-1">
                숨김 해제
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
