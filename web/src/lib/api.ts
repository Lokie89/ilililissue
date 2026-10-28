// 브라우저에서 쓰는 API 클라이언트.
// 같은 출처의 /api/* 로 요청하고(Next.js rewrite로 백엔드에 전달), 인증은 HttpOnly 쿠키로 한다.
// 액세스 토큰이 만료되어 401이 오면 한 번 재발급(/api/auth/refresh)한 뒤 다시 요청한다.

import type { ApiErrorBody } from "./types";

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  query?: Record<string, string | number | undefined>;
};

let refreshing: Promise<boolean> | null = null;

async function refreshTokens(): Promise<boolean> {
  // 동시에 여러 요청이 401을 받아도 재발급은 한 번만 한다(리프레시 토큰은 1회용).
  refreshing ??= fetch("/api/auth/refresh", { method: "POST", credentials: "same-origin" })
    .then((response) => response.ok)
    .catch(() => false)
    .finally(() => {
      refreshing = null;
    });
  return refreshing;
}

function buildUrl(path: string, query?: RequestOptions["query"]): string {
  if (!query) {
    return path;
  }
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined) {
      params.set(key, String(value));
    }
  }
  const search = params.toString();
  return search ? `${path}?${search}` : path;
}

async function send(path: string, options: RequestOptions): Promise<Response> {
  return fetch(buildUrl(path, options.query), {
    method: options.method ?? "GET",
    credentials: "same-origin",
    headers: options.body === undefined ? undefined : { "Content-Type": "application/json" },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });
}

async function toError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as ApiErrorBody;
    return new ApiError(response.status, body.code, body.message);
  } catch {
    return new ApiError(response.status, "UNKNOWN", "요청을 처리하지 못했습니다.");
  }
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  let response = await send(path, options);
  if (response.status === 401 && !path.startsWith("/api/auth/") && (await refreshTokens())) {
    response = await send(path, options);
  }
  if (!response.ok) {
    throw await toError(response);
  }
  if (response.status === 204 || response.headers.get("Content-Length") === "0") {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  return "잠시 후 다시 시도해 주세요.";
}
