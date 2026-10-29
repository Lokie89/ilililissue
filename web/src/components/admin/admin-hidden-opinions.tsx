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
      {message && <p className="error-text mb-2">{message}</p>}
      {opinions.length === 0 ? (
        <p className="card py-12 text-center text-muted">숨긴 의견이 없습니다.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {opinions.map((opinion) => (
            <li key={opinion.id} className="card text-sm">
              <div className="mb-2 text-xs text-subtle">
                의견 #{opinion.id} · 이슈 #{opinion.issueId} · {STANCE_LABEL[opinion.stance]} ·{" "}
                {new Date(opinion.writtenAt).toLocaleString("ko-KR")}
              </div>
              <p className="mb-3 rounded-xl bg-ink/[0.03] p-3 leading-relaxed whitespace-pre-wrap">{opinion.content}</p>
              <button type="button" onClick={() => unhide(opinion.id)} className="btn btn-sm btn-secondary">
                숨김 해제
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
