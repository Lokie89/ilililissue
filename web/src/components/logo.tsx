import Link from "next/link";

/** 로고: 세 입장 색 점 + 서비스 이름. 세 점이 같은 크기인 것이 서비스의 태도다. */
export function Logo() {
  return (
    <Link href="/" className="flex items-center gap-2 text-[17px] font-bold tracking-tight">
      <span aria-hidden className="flex gap-0.5">
        <span className="size-2 rounded-full bg-stance-agree" />
        <span className="size-2 rounded-full bg-stance-neutral" />
        <span className="size-2 rounded-full bg-stance-disagree" />
      </span>
      1일 1이슈
    </Link>
  );
}
