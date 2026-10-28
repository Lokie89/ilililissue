import { Suspense } from "react";
import Link from "next/link";
import { getIssues } from "@/lib/server-api";
import { ISSUE_STATUS_LABEL } from "@/lib/types";

export default function Home() {
  return (
    <div>
      <section className="mb-8">
        <h1 className="mb-2 text-2xl font-bold">오늘의 이슈</h1>
        <p className="text-stone-600">
          찬성·중립·반대 중 하나를 고르고, 다른 입장의 사람들은 왜 그렇게 생각하는지 읽어 보세요. 답글 싸움은 없어요.
        </p>
      </section>
      <Suspense fallback={<p className="text-stone-500">이슈를 불러오는 중…</p>}>
        <IssueList />
      </Suspense>
    </div>
  );
}

async function IssueList() {
  const issues = await getIssues();
  if (!issues || issues.content.length === 0) {
    return <p className="py-12 text-center text-stone-500">아직 게시된 이슈가 없어요.</p>;
  }
  return (
    <ul className="flex flex-col gap-3">
      {issues.content.map((issue) => (
        <li key={issue.id}>
          <Link
            href={`/issues/${issue.id}`}
            className="block rounded-lg border border-stone-200 p-4 hover:border-stone-400"
          >
            <div className="mb-1 flex gap-2 text-xs text-stone-500">
              {issue.category && <span>{issue.category.name}</span>}
              {issue.publishedAt && <time>{new Date(issue.publishedAt).toLocaleDateString("ko-KR")}</time>}
              {issue.status === "CLOSED" && <span>· {ISSUE_STATUS_LABEL.CLOSED}</span>}
            </div>
            <h2 className="font-semibold">{issue.title}</h2>
          </Link>
        </li>
      ))}
    </ul>
  );
}
