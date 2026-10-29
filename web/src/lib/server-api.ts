import "server-only";

import { cacheLife } from "next/cache";
import type { Category, IssueDetail, IssueSummary, Page } from "./types";

// 서버 컴포넌트는 rewrite를 거치지 않고 백엔드를 직접 호출한다.
const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

async function get<T>(path: string): Promise<T | null> {
  const response = await fetch(`${BACKEND_URL}${path}`);
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    throw new Error(`GET ${path} failed: ${response.status}`);
  }
  return (await response.json()) as T;
}

/** 공개 이슈 목록. categoryId를 주면 그 분야만. 요청마다 조회한다(Suspense 안에서 호출). */
export function getIssues(page = 0, categoryId?: number): Promise<Page<IssueSummary> | null> {
  const category = categoryId === undefined ? "" : `&categoryId=${categoryId}`;
  return get<Page<IssueSummary>>(`/api/issues?page=${page}&size=20${category}`);
}

/** 분야(카테고리) 목록. 요청마다 조회한다(Suspense 안에서 호출). */
export async function getCategories(): Promise<Category[]> {
  return (await get<Category[]>("/api/categories")) ?? [];
}

/** 공개 이슈 상세. 없거나 게시 전이면 null. 요청마다 조회한다(Suspense 안에서 호출). */
export function getIssue(id: string): Promise<IssueDetail | null> {
  return get<IssueDetail>(`/api/issues/${encodeURIComponent(id)}`);
}

/** 메타데이터용 이슈 제목. 몇 분간 캐시한다. */
export async function getIssueTitle(id: string): Promise<string | null> {
  "use cache";
  cacheLife("minutes");
  try {
    const response = await fetch(`${BACKEND_URL}/api/issues/${encodeURIComponent(id)}`);
    if (!response.ok) {
      return null;
    }
    return ((await response.json()) as IssueDetail).title;
  } catch {
    return null;
  }
}
