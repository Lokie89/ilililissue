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
  const [agreed, setAgreed] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      if (mode === "signup") {
        await api("/api/members", { method: "POST", body: { email, nickname, password, agreedToTerms: agreed } });
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
    <form onSubmit={submit} className="card mx-auto flex max-w-sm flex-col gap-3 sm:p-8">
      <h1 className="text-2xl font-bold tracking-tight">{mode === "login" ? "다시 만나서 반가워요" : "1일 1이슈 시작하기"}</h1>
      <p className="mb-3 text-sm text-muted">
        {mode === "login" ? "로그인하고 오늘의 이슈에 참여하세요." : "닉네임은 의견을 남길 때 표시돼요."}
      </p>
      <input
        type="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
        autoComplete="email"
        className="field"
      />
      {mode === "signup" && (
        <input
          required
          minLength={2}
          maxLength={12}
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="닉네임 (2~12자, 의견에 표시돼요)"
          className="field"
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
        className="field"
      />
      {mode === "signup" && (
        <label className="flex items-start gap-2 py-1 text-xs leading-relaxed text-muted">
          <input
            type="checkbox"
            required
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            className="mt-0.5 accent-ink"
          />
          <span>
            <Link href="/terms" target="_blank" className="link">
              이용약관
            </Link>
            과{" "}
            <Link href="/privacy" target="_blank" className="link">
              개인정보 처리방침
            </Link>
            에 동의합니다. (필수)
          </span>
        </label>
      )}
      {error && <p className="error-text">{error}</p>}
      <button type="submit" disabled={submitting} className="btn btn-primary mt-1 w-full py-3">
        {mode === "login" ? "로그인" : "가입하고 시작하기"}
      </button>
      <p className="mt-1 text-center text-sm text-muted">
        {mode === "login" ? (
          <Link href={`/signup?next=${encodeURIComponent(next)}`} className="link">
            처음이신가요? 가입하기
          </Link>
        ) : (
          <Link href={`/login?next=${encodeURIComponent(next)}`} className="link">
            이미 계정이 있어요
          </Link>
        )}
      </p>
    </form>
  );
}
