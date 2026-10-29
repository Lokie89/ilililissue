"use client";

export default function Error({ retry }: { error: unknown; retry: () => void }) {
  return (
    <div className="card py-16 text-center">
      <h1 className="mb-2 text-xl font-semibold">문제가 생겼어요</h1>
      <p className="mb-6 text-muted">잠시 후 다시 시도해 주세요.</p>
      <button type="button" onClick={() => retry()} className="btn btn-primary">
        다시 시도
      </button>
    </div>
  );
}
