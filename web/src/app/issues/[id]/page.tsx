import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { IssueImage } from "@/components/issue-image";
import { Participation } from "@/components/participation";
import { ViewTracker } from "@/components/view-tracker";
import { formatDate, formatDateTime } from "@/lib/format";
import { getIssue, getIssueTitle } from "@/lib/server-api";
import { STANCE_STYLE } from "@/lib/stance";
import type { Stance } from "@/lib/types";

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
          {issue.category && (
            <Link href={`/?category=${issue.category.id}`} className="chip hover:bg-ink/10">
              {issue.category.name}
            </Link>
          )}
          {issue.publishedAt && <time dateTime={issue.publishedAt}>{formatDate(issue.publishedAt)}</time>}
          {issue.status === "CLOSED" && <span className="chip">투표 마감</span>}
        </div>
        <h1 className="text-[26px] leading-snug font-bold tracking-tight sm:text-3xl">{issue.title}</h1>
      </header>

      {issue.imageKey && (
        <figure>
          <IssueImage issueId={issue.id} imageKey={issue.imageKey} className="w-full rounded-2xl" priority />
          <figcaption className="mt-2 text-right text-xs text-subtle">AI로 만든 이미지입니다. 실제 장면이 아닙니다.</figcaption>
        </figure>
      )}

      {issue.keyPoint && (
        <section className="rounded-2xl bg-ink px-5 py-5 text-white sm:px-7 sm:py-6">
          <h2 className="mb-2 text-xs font-semibold tracking-wide text-white/60">핵심 쟁점</h2>
          <p className="text-lg leading-snug font-bold sm:text-xl">{issue.keyPoint}</p>
        </section>
      )}

      <section className="card">
        <h2 className="eyebrow mb-2">배경</h2>
        <p className="leading-[1.8] whitespace-pre-wrap text-ink/85">{issue.summary}</p>
        {issue.corrected && issue.correctedAt && (
          <p className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-800">
            {formatDateTime(issue.correctedAt)}에 사실관계가 정정되었습니다.
          </p>
        )}
      </section>

      {issue.agreeReason && issue.disagreeReason && (
        <section>
          <h2 className="eyebrow mb-3">이렇게 생각해요</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            <ReasonCard stance="AGREE" label="찬성하는 쪽" reason={issue.agreeReason} />
            <ReasonCard stance="DISAGREE" label="반대·신중한 쪽" reason={issue.disagreeReason} />
          </div>
        </section>
      )}

      <section className="card py-4 sm:py-5">
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
      </section>

      <Participation issueId={issue.id} open={issue.status === "PUBLISHED"} />
    </article>
  );
}

function ReasonCard({ stance, label, reason }: { stance: Stance; label: string; reason: string }) {
  const style = STANCE_STYLE[stance];
  return (
    <div className={`rounded-2xl p-5 ${style.soft}`}>
      <p className={`mb-2 flex items-center gap-1.5 text-sm font-semibold ${style.text}`}>
        <span aria-hidden className={`size-2 rounded-full ${style.solid}`} />
        {label}
      </p>
      <p className="leading-relaxed text-ink/85">{reason}</p>
    </div>
  );
}
