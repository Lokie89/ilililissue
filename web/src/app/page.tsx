import { Suspense } from "react";
import Link from "next/link";
import { formatDate } from "@/lib/format";
import { getCategories, getIssues } from "@/lib/server-api";
import { type Category, ISSUE_STATUS_LABEL, type IssueSummary } from "@/lib/types";

export default function Home({ searchParams }: PageProps<"/">) {
  return (
    <div className="flex flex-col gap-8">
      <section>
        <h1 className="mb-3 text-[28px] leading-tight font-bold tracking-tight sm:text-3xl">
          하나의 질문,
          <br />
          서로 다른 생각
        </h1>
        <p className="max-w-xl text-muted">
          찬성·중립·반대 중 하나를 고르고, 다른 입장의 사람들은 왜 그렇게 생각하는지 읽어 보세요. 답글 싸움은 없어요.
        </p>
      </section>
      <Suspense fallback={<div className="h-48 animate-pulse rounded-2xl bg-ink/5" />}>
        <FilteredIssues searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

/** ?category=<id> 로 분야를 고른다. 없거나 잘못된 값이면 전체. */
async function FilteredIssues({ searchParams }: Pick<PageProps<"/">, "searchParams">) {
  const raw = (await searchParams).category;
  const categories = await getCategories();
  const selected = categories.find((category) => String(category.id) === (Array.isArray(raw) ? raw[0] : raw));
  const issues = await getIssues(0, selected?.id);

  return (
    <div className="flex flex-col gap-8">
      <CategoryTabs categories={categories} selectedId={selected?.id} />
      <IssueList issues={issues?.content ?? []} category={selected} />
    </div>
  );
}

function CategoryTabs({ categories, selectedId }: { categories: Category[]; selectedId?: number }) {
  const tabs = [{ id: undefined, name: "전체" }, ...categories];
  return (
    <nav aria-label="분야" className="-mx-4 overflow-x-auto px-4">
      <ul className="flex w-max gap-1.5">
        {tabs.map((tab) => {
          const active = tab.id === selectedId;
          return (
            <li key={tab.id ?? "all"}>
              <Link
                href={tab.id === undefined ? "/" : `/?category=${tab.id}`}
                aria-current={active ? "page" : undefined}
                scroll={false}
                className={`btn btn-sm rounded-full px-4 ${active ? "btn-primary" : "btn-secondary"}`}
              >
                {tab.name}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

function IssueList({ issues, category }: { issues: IssueSummary[]; category?: Category }) {
  if (issues.length === 0) {
    return (
      <p className="card py-16 text-center text-muted">
        {category ? `아직 ${category.name} 분야에 게시된 이슈가 없어요.` : "아직 게시된 이슈가 없어요."}
      </p>
    );
  }
  const [latest, ...rest] = issues;
  return (
    <div className="flex flex-col gap-10">
      <FeaturedIssue issue={latest} category={category} />
      {rest.length > 0 && (
        <section>
          <h2 className="eyebrow mb-3">{category ? `${category.name} 지난 이슈` : "지난 이슈"}</h2>
          <ul className="card divide-y divide-line p-0 sm:p-0">
            {rest.map((issue) => (
              <li key={issue.id}>
                <Link
                  href={`/issues/${issue.id}`}
                  className="group flex items-center justify-between gap-4 px-5 py-4 transition-colors hover:bg-ink/[0.02] sm:px-6"
                >
                  <div className="min-w-0">
                    <IssueMeta issue={issue} />
                    <h3 className="mt-1 font-semibold group-hover:underline group-hover:underline-offset-4">
                      {issue.title}
                    </h3>
                  </div>
                  <span aria-hidden className="text-subtle transition-transform group-hover:translate-x-0.5">
                    →
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}

function FeaturedIssue({ issue, category }: { issue: IssueSummary; category?: Category }) {
  const open = issue.status === "PUBLISHED";
  const label = open ? "오늘의 이슈" : "최근 이슈";
  return (
    <Link
      href={`/issues/${issue.id}`}
      className="group card relative block overflow-hidden transition-shadow hover:shadow-[0_8px_30px_-12px_rgba(0,0,0,0.15)] sm:p-8"
    >
      <span aria-hidden className="absolute inset-x-0 top-0 flex h-1">
        <span className="flex-1 bg-stance-agree" />
        <span className="flex-1 bg-stance-neutral" />
        <span className="flex-1 bg-stance-disagree" />
      </span>
      <p className="eyebrow mb-3">{category ? `${category.name} · ${label}` : label}</p>
      <IssueMeta issue={issue} />
      <h2 className="mt-2 text-2xl leading-snug font-bold tracking-tight sm:text-[28px]">{issue.title}</h2>
      <span className="btn btn-primary mt-6">{open ? "내 입장 고르기" : "결과 보기"} →</span>
    </Link>
  );
}

function IssueMeta({ issue }: { issue: IssueSummary }) {
  return (
    <div className="flex flex-wrap items-center gap-2 text-xs text-subtle">
      {issue.category && <span className="chip">{issue.category.name}</span>}
      {issue.publishedAt && <time dateTime={issue.publishedAt}>{formatDate(issue.publishedAt)}</time>}
      {issue.status === "CLOSED" && <span>· {ISSUE_STATUS_LABEL.CLOSED}</span>}
    </div>
  );
}
