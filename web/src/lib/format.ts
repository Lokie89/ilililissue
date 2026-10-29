// 날짜 표시. 서버(SSR)와 브라우저의 시간대가 달라도 같은 날짜가 나오도록 서울 기준으로 맞춘다.
const TIME_ZONE = "Asia/Seoul";

/** 10월 28일 (올해가 아니면 2025년 10월 28일) */
export function formatDate(value: string): string {
  const date = new Date(value);
  const sameYear =
    date.toLocaleDateString("ko-KR", { year: "numeric", timeZone: TIME_ZONE }) ===
    new Date().toLocaleDateString("ko-KR", { year: "numeric", timeZone: TIME_ZONE });
  return date.toLocaleDateString("ko-KR", {
    year: sameYear ? undefined : "numeric",
    month: "long",
    day: "numeric",
    timeZone: TIME_ZONE,
  });
}

/** 2026. 10. 28. 오후 4:50 */
export function formatDateTime(value: string): string {
  return new Date(value).toLocaleString("ko-KR", { dateStyle: "medium", timeStyle: "short", timeZone: TIME_ZONE });
}
