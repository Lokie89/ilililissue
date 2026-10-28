"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Member } from "@/lib/types";

type MeContext = {
  me: Member | null;
  loading: boolean;
  reload: () => Promise<void>;
  logout: () => Promise<void>;
};

const Context = createContext<MeContext | null>(null);

async function fetchMe(): Promise<Member | null> {
  try {
    return await api<Member>("/api/members/me");
  } catch (e) {
    if (!(e instanceof ApiError) || e.status !== 401) {
      console.error(e);
    }
    return null;
  }
}

/**
 * 로그인 상태. 인증 쿠키는 HttpOnly라 브라우저 스크립트가 읽을 수 없으므로 /api/members/me로 확인한다.
 */
export function MeProvider({ children }: { children: React.ReactNode }) {
  const [me, setMe] = useState<Member | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setMe(await fetchMe());
    setLoading(false);
  }, []);

  const logout = useCallback(async () => {
    await api("/api/auth/logout", { method: "POST" }).catch(() => undefined);
    setMe(null);
  }, []);

  useEffect(() => {
    let active = true;
    fetchMe().then((member) => {
      if (active) {
        setMe(member);
        setLoading(false);
      }
    });
    return () => {
      active = false;
    };
  }, []);

  const value = useMemo(() => ({ me, loading, reload, logout }), [me, loading, reload, logout]);
  return <Context.Provider value={value}>{children}</Context.Provider>;
}

export function useMe(): MeContext {
  const value = useContext(Context);
  if (!value) {
    throw new Error("useMe must be used inside MeProvider");
  }
  return value;
}
