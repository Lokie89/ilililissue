// 이슈 대표 이미지(AI가 만든 일러스트). 키가 바뀌면 주소도 바뀌어 캐시를 새로 받는다.
// 백엔드가 이미 1200px JPEG로 줄여 주므로 next/image 최적화 없이 그대로 쓴다.

type Props = {
  issueId: number;
  imageKey: string;
  /** 너비 클래스(w-full, w-20 등)는 쓰는 쪽에서 준다. */
  className?: string;
  /** 관리 화면: 게시 전 이슈 이미지도 보는 관리자 주소를 쓴다. */
  admin?: boolean;
  priority?: boolean;
};

export function issueImageUrl(issueId: number, imageKey: string, admin = false): string {
  return `/api/${admin ? "admin/" : ""}issues/${issueId}/image?v=${imageKey}`;
}

export function IssueImage({ issueId, imageKey, className = "", admin = false, priority = false }: Props) {
  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={issueImageUrl(issueId, imageKey, admin)}
      alt=""
      width={1200}
      height={675}
      loading={priority ? "eager" : "lazy"}
      decoding="async"
      className={`aspect-video bg-ink/5 object-cover ${className}`}
    />
  );
}
