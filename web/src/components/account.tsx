"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, errorMessage } from "@/lib/api";
import { BlockedMembers } from "@/components/blocked-members";
import { useMe } from "@/components/me-provider";

/**
 * 내 정보, 차단한 회원, 회원 탈퇴. 탈퇴하면 남긴 투표와 의견은 '탈퇴한 회원'으로 남는다.
 */
export function Account() {
  const { me, loading, clear } = useMe();
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (loading) {
    return <div className="mx-auto h-40 max-w-md animate-pulse rounded-2xl bg-ink/5" />;
  }

  if (!me) {
    return (
      <p className="card mx-auto max-w-md text-center">
        <Link href="/login?next=/me" className="link font-semibold">
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
    <div className="mx-auto flex max-w-md flex-col gap-4">
      <h1 className="mb-2 text-2xl font-bold tracking-tight">내 정보</h1>
      <section className="card">
        <dl className="grid grid-cols-[5rem_1fr] gap-y-3 text-sm">
          <dt className="text-muted">닉네임</dt>
          <dd className="font-medium">{me.nickname}</dd>
          <dt className="text-muted">이메일</dt>
          <dd className="break-all">{me.email}</dd>
        </dl>
      </section>

      <BlockedMembers />

      <form onSubmit={withdraw} className="card flex flex-col gap-3">
        <h2 className="font-bold">회원 탈퇴</h2>
        <ul className="list-disc space-y-1 pl-5 text-sm text-muted">
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
          className="field"
        />
        {error && <p className="error-text">{error}</p>}
        <button
          type="submit"
          disabled={submitting || !password}
          className="btn btn-danger"
        >
          탈퇴하기
        </button>
      </form>
    </div>
  );
}
