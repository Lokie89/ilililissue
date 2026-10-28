"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { api, errorMessage } from "@/lib/api";
import { useMe } from "@/components/me-provider";

type Mode = "login" | "signup";

/** 로그인 후 돌아갈 경로. 외부 주소로 이동하지 않도록 내부 경로만 허용한다. */
function safeNext(next: string | null): string {
  return next && next.startsWith("/") && !next.startsWith("//") ? next : "/";
}

export function AuthForm({ mode }: { mode: Mode }) {
  const router = useRouter();
  const next = safeNext(useSearchParams().get("next"));
  const { reload } = useMe();
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      if (mode === "signup") {
        await api("/api/members", { method: "POST", body: { email, nickname, password } });
      }
      await api("/api/auth/login", { method: "POST", body: { email, password } });
      await reload();
      router.push(next);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={submit} className="mx-auto flex max-w-sm flex-col gap-3">
      <h1 className="mb-2 text-xl font-bold">{mode === "login" ? "로그인" : "가입"}</h1>
      <input
        type="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
        autoComplete="email"
        className="rounded border border-stone-300 p-2"
      />
      {mode === "signup" && (
        <input
          required
          minLength={2}
          maxLength={12}
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="닉네임 (2~12자, 의견에 표시돼요)"
          className="rounded border border-stone-300 p-2"
        />
      )}
      <input
        type="password"
        required
        minLength={mode === "signup" ? 8 : undefined}
        maxLength={64}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder={mode === "signup" ? "비밀번호 (8자 이상)" : "비밀번호"}
        autoComplete={mode === "signup" ? "new-password" : "current-password"}
        className="rounded border border-stone-300 p-2"
      />
      {error && <p className="text-sm text-rose-600">{error}</p>}
      <button type="submit" disabled={submitting} className="rounded bg-stone-900 py-2 text-white disabled:opacity-40">
        {mode === "login" ? "로그인" : "가입하고 시작하기"}
      </button>
      <p className="text-center text-sm text-stone-500">
        {mode === "login" ? (
          <Link href={`/signup?next=${encodeURIComponent(next)}`} className="underline">
            처음이신가요? 가입하기
          </Link>
        ) : (
          <Link href={`/login?next=${encodeURIComponent(next)}`} className="underline">
            이미 계정이 있어요
          </Link>
        )}
      </p>
    </form>
  );
}
