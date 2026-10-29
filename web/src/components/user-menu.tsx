"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMe } from "@/components/me-provider";

export function UserMenu() {
  const { me, loading, logout } = useMe();
  const router = useRouter();

  if (loading) {
    return <span className="h-8 w-24 animate-pulse rounded-lg bg-ink/5" />;
  }

  if (!me) {
    return (
      <nav className="flex items-center gap-1">
        <Link href="/login" className="btn btn-sm btn-ghost">
          로그인
        </Link>
        <Link href="/signup" className="btn btn-sm btn-primary">
          가입
        </Link>
      </nav>
    );
  }

  return (
    <nav className="flex items-center gap-1">
      {me.role === "ADMIN" && (
        <Link href="/admin" className="btn btn-sm btn-ghost">
          관리
        </Link>
      )}
      <Link href="/me" className="btn btn-sm btn-ghost max-w-32">
        <span className="truncate">{me.nickname}</span>
      </Link>
      <button
        type="button"
        onClick={async () => {
          await logout();
          router.refresh();
        }}
        className="btn btn-sm btn-ghost"
      >
        로그아웃
      </button>
    </nav>
  );
}
