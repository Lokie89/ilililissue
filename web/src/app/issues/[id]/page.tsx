import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { Participation } from "@/components/participation";
import { ViewTracker } from "@/components/view-tracker";
import { getIssue, getIssueTitle } from "@/lib/server-api";

export async function generateMetadata({ params }: PageProps<"/issues/[id]">): Promise<Metadata> {
  const { id } = await params;
  const title = await getIssueTitle(id);
  return title ? { title } : {};
}

export default function IssuePage({ params }: PageProps<"/issues/[id]">) {
  return (
    <Suspense fallback={<p className="text-stone-500">이슈를 불러오는 중…</p>}>
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
        <div className="mb-2 flex gap-2 text-sm text-stone-500">
          {issue.category && <span>{issue.category.name}</span>}
          {issue.publishedAt && <time>{new Date(issue.publishedAt).toLocaleDateString("ko-KR")}</time>}
          {issue.status === "CLOSED" && <span>· 마감</span>}
        </div>
        <h1 className="text-2xl font-bold">{issue.title}</h1>
      </header>

      <section>
        <h2 className="mb-2 font-semibold">배경</h2>
        <p className="whitespace-pre-wrap leading-relaxed text-stone-700">{issue.summary}</p>
        {issue.corrected && issue.correctedAt && (
          <p className="mt-2 text-xs text-amber-700">
            {new Date(issue.correctedAt).toLocaleString("ko-KR")}에 사실관계가 정정되었습니다.
          </p>
        )}
      </section>

      <section>
        <h2 className="mb-2 font-semibold">원문 기사</h2>
        <ul className="flex flex-col gap-1 text-sm">
          {issue.sources.map((source) => (
            <li key={source.url}>
              <a href={source.url} target="_blank" rel="noopener noreferrer" className="underline">
                {source.publisher}
              </a>{" "}
              <span className="text-stone-400">{source.publishedOn}</span>
            </li>
          ))}
        </ul>
      </section>

      <Participation issueId={issue.id} open={issue.status === "PUBLISHED"} />
    </article>
  );
}
