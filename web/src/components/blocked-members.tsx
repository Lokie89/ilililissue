"use client";

import { useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";
import type { BlockedMember } from "@/lib/types";

/**
 * 내가 차단한 회원 목록과 해제. 차단은 의견 카드의 '차단' 버튼으로 한다.
 */
export function BlockedMembers() {
  const [blocks, setBlocks] = useState<BlockedMember[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api<BlockedMember[]>("/api/members/me/blocks")
      .then(setBlocks)
      .catch((e) => setError(errorMessage(e)));
  }, []);

  async function unblock(block: BlockedMember) {
    if (!window.confirm(`${block.nickname}님의 차단을 해제할까요?`)) return;
    try {
      await api(`/api/members/me/blocks/${block.id}`, { method: "DELETE" });
      setBlocks((prev) => prev?.filter((b) => b.id !== block.id) ?? null);
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  return (
    <section className="card flex flex-col gap-3">
      <h2 className="font-bold">차단한 회원</h2>
      <p className="text-sm text-muted">차단한 회원의 의견은 보이지 않아요. 상대에게는 알리지 않아요.</p>
      {error && <p className="error-text">{error}</p>}
      {blocks === null ? (
        !error && <p className="text-sm text-subtle">불러오는 중…</p>
      ) : blocks.length === 0 ? (
        <p className="notice">차단한 회원이 없어요.</p>
      ) : (
        <ul className="flex flex-col divide-y divide-line text-sm">
          {blocks.map((block) => (
            <li key={block.id} className="flex items-center justify-between py-2">
              <span>
                {block.nickname}
                <span className="ml-2 text-xs text-subtle">
                  {new Date(block.blockedAt).toLocaleDateString("ko-KR")}
                </span>
              </span>
              <button type="button" onClick={() => unblock(block)} className="btn btn-sm btn-secondary">
                해제
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
