"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, errorMessage } from "@/lib/api";
import { useMe } from "@/components/me-provider";

/**
 * 내 정보와 회원 탈퇴. 탈퇴하면 남긴 투표와 의견은 '탈퇴한 회원'으로 남는다.
 */
export function Account() {
  const { me, loading, clear } = useMe();
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (loading) {
    return <p className="text-sm text-stone-500">불러오는 중…</p>;
  }

  if (!me) {
    return (
      <p className="text-center">
        <Link href="/login?next=/me" className="underline">
          로그인
        </Link>
        이 필요합니다.
      </p>
    );
  }

  async function withdraw(event: React.FormEvent) {
    event.preventDefault();
    if (!window.confirm("정말 탈퇴할까요? 되돌릴 수 없어요.")) return;
    setSubmitting(true);
    setError(null);
    try {
      await api("/api/members/me/withdrawal", { method: "POST", body: { password } });
      clear();
      router.push("/");
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto flex max-w-sm flex-col gap-8">
      <section>
        <h1 className="mb-3 text-xl font-bold">내 정보</h1>
        <dl className="grid grid-cols-[5rem_1fr] gap-y-1 text-sm">
          <dt className="text-stone-500">닉네임</dt>
          <dd>{me.nickname}</dd>
          <dt className="text-stone-500">이메일</dt>
          <dd className="break-all">{me.email}</dd>
        </dl>
      </section>

      <form onSubmit={withdraw} className="flex flex-col gap-3 rounded-lg border border-stone-200 p-5">
        <h2 className="font-semibold">회원 탈퇴</h2>
        <ul className="list-disc pl-5 text-sm text-stone-500">
          <li>이메일, 닉네임, 비밀번호가 삭제되고 다시 로그인할 수 없어요.</li>
          <li>남긴 투표와 의견은 지워지지 않고 작성자가 &lsquo;탈퇴한 회원&rsquo;으로 표시돼요. 지우고 싶은 의견은 탈퇴 전에 삭제해 주세요.</li>
          <li>같은 이메일로 다시 가입할 수 있어요.</li>
        </ul>
        <input
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="비밀번호 확인"
          autoComplete="current-password"
          className="rounded border border-stone-300 p-2"
        />
        {error && <p className="text-sm text-rose-600">{error}</p>}
        <button
          type="submit"
          disabled={submitting || !password}
          className="rounded bg-rose-600 py-2 text-white disabled:opacity-40"
        >
          탈퇴하기
        </button>
      </form>
    </div>
  );
}
