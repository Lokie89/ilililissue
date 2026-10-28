"use client";

export default function Error({ retry }: { error: unknown; retry: () => void }) {
  return (
    <div className="py-16 text-center">
      <h1 className="mb-2 text-xl font-semibold">문제가 생겼어요</h1>
      <p className="mb-6 text-stone-500">잠시 후 다시 시도해 주세요.</p>
      <button type="button" onClick={() => retry()} className="rounded bg-stone-900 px-4 py-2 text-white">
        다시 시도
      </button>
    </div>
  );
}
