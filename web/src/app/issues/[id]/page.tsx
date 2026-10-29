import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { Participation } from "@/components/participation";
import { ViewTracker } from "@/components/view-tracker";
import { formatDate, formatDateTime } from "@/lib/format";
import { getIssue, getIssueTitle } from "@/lib/server-api";

export async function generateMetadata({ params }: PageProps<"/issues/[id]">): Promise<Metadata> {
  const { id } = await params;
  const title = await getIssueTitle(id);
  return title ? { title } : {};
}

export default function IssuePage({ params }: PageProps<"/issues/[id]">) {
  return (
    <Suspense fallback={<div className="h-64 animate-pulse rounded-2xl bg-ink/5" />}>
      <Issue params={params} />
    </Suspense>
  );
}

async function Issue({ params }: Pick<PageProps<"/issues/[id]">, "params">) {
  const { id } = await params;
  const issue = await getIssue(id);
  if (!issue) {
    notFound();
  }

  return (
    <article className="flex flex-col gap-8">
      <ViewTracker issueId={issue.id} />
      <header>
        <div className="mb-3 flex flex-wrap items-center gap-2 text-xs text-subtle">
          {issue.category && <span className="chip">{issue.category.name}</span>}
          {issue.publishedAt && <time dateTime={issue.publishedAt}>{formatDate(issue.publishedAt)}</time>}
          {issue.status === "CLOSED" && <span className="chip">투표 마감</span>}
        </div>
        <h1 className="text-[26px] leading-snug font-bold tracking-tight sm:text-3xl">{issue.title}</h1>
      </header>

      <section className="card flex flex-col gap-6">
        <div>
          <h2 className="eyebrow mb-2">배경</h2>
          <p className="leading-[1.8] whitespace-pre-wrap text-ink/85">{issue.summary}</p>
          {issue.corrected && issue.correctedAt && (
            <p className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-800">
              {formatDateTime(issue.correctedAt)}에 사실관계가 정정되었습니다.
            </p>
          )}
        </div>
        <div className="border-t border-line pt-5">
          <h2 className="eyebrow mb-2">원문 기사</h2>
          <ul className="flex flex-col gap-1">
            {issue.sources.map((source) => (
              <li key={source.url}>
                <a
                  href={source.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group -mx-2 flex items-center justify-between gap-3 rounded-lg px-2 py-1.5 text-sm hover:bg-ink/5"
                >
                  <span className="font-medium">{source.publisher}</span>
                  <span className="flex items-center gap-2 text-xs text-subtle">
                    {formatDate(source.publishedOn)}
                    <span aria-hidden className="transition-transform group-hover:translate-x-0.5">
                      ↗
                    </span>
                  </span>
                </a>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <Participation issueId={issue.id} open={issue.status === "PUBLISHED"} />
    </article>
  );
}
