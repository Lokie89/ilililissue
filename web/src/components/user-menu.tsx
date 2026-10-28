"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMe } from "@/components/me-provider";

export function UserMenu() {
  const { me, loading, logout } = useMe();
  const router = useRouter();

  if (loading) {
    return <span className="text-sm text-stone-400">…</span>;
  }

  if (!me) {
    return (
      <nav className="flex gap-3 text-sm">
        <Link href="/login">로그인</Link>
        <Link href="/signup" className="rounded bg-stone-900 px-3 py-1 text-white">
          가입
        </Link>
      </nav>
    );
  }

  return (
    <nav className="flex items-center gap-3 text-sm">
      {me.role === "ADMIN" && <Link href="/admin">관리</Link>}
      <Link href="/me" className="text-stone-500">
        {me.nickname}
      </Link>
      <button
        type="button"
        onClick={async () => {
          await logout();
          router.refresh();
        }}
        className="text-stone-500 underline"
      >
        로그아웃
      </button>
    </nav>
  );
}
