import Link from "next/link";

export default function NotFound() {
  return (
    <div className="card py-16 text-center">
      <h1 className="mb-2 text-xl font-semibold">찾을 수 없는 페이지예요</h1>
      <p className="mb-6 text-muted">게시 전이거나 없는 이슈일 수 있어요.</p>
      <Link href="/" className="btn btn-secondary">
        이슈 목록으로
      </Link>
    </div>
  );
}
