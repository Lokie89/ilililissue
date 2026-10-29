// 백엔드 API 응답 타입. 서버 정의는 루트 README.md의 API 표를 따른다.

export type Stance = "AGREE" | "NEUTRAL" | "DISAGREE";

export const STANCES: Stance[] = ["AGREE", "NEUTRAL", "DISAGREE"];

export const STANCE_LABEL: Record<Stance, string> = {
  AGREE: "찬성",
  NEUTRAL: "중립",
  DISAGREE: "반대",
};

export type IssueStatus = "DRAFT" | "REVIEWED" | "PUBLISHED" | "CLOSED";

export const ISSUE_STATUS_LABEL: Record<IssueStatus, string> = {
  DRAFT: "작성 중",
  REVIEWED: "검수 완료",
  PUBLISHED: "게시",
  CLOSED: "마감",
};

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
};

export type Member = {
  id: number;
  email: string;
  nickname: string;
  role: "USER" | "ADMIN";
};

export type Category = {
  id: number;
  name: string;
};

export type IssueSummary = {
  id: number;
  title: string;
  category: Category | null;
  status: IssueStatus;
  publishedAt: string | null;
};

export type IssueSource = {
  url: string;
  publisher: string;
  publishedOn: string;
};

export type IssueDetail = {
  id: number;
  title: string;
  summary: string;
  category: Category | null;
  sources: IssueSource[];
  status: IssueStatus;
  publishedAt: string | null;
  corrected: boolean;
  correctedAt: string | null;
};

export type Opinion = {
  id: number;
  issueId: number;
  stance: Stance;
  /** 삭제한 의견은 null */
  content: string | null;
  edited: boolean;
  hidden: boolean;
  deleted: boolean;
  writtenAt: string;
};

export type Participation = {
  voted: boolean;
  stance: Stance | null;
  opinion: Opinion | null;
};

export type VoteResult = {
  total: number;
  stances: { stance: Stance; count: number; percentage: number }[];
  notice: string;
};

export type OpinionItem = {
  id: number;
  authorNickname: string | null;
  stance: Stance;
  content: string;
  edited: boolean;
  recommendationCount: number;
  recommendedByMe: boolean;
  mine: boolean;
  writtenAt: string;
};

export type OpinionSort = "RECOMMENDED" | "LATEST";

export type ReportStatus = "RECEIVED" | "RESOLVED_HIDDEN" | "RESOLVED_DISMISSED";

export type AdminReport = {
  id: number;
  opinionId: number;
  issueId: number;
  opinionContent: string;
  opinionHidden: boolean;
  reporterId: number;
  reason: string;
  status: ReportStatus;
  reportedAt: string;
  resolvedAt: string | null;
};

export type BlockedMember = {
  id: number;
  nickname: string;
  blockedAt: string;
};

export type HiddenOpinion = {
  id: number;
  issueId: number;
  stance: Stance;
  content: string;
  writtenAt: string;
};

export type Metrics = {
  from: string;
  to: string;
  issueViews: number;
  issueVisitors: number;
  votes: number;
  opinions: number;
  voters: number;
  opposingViewers: number;
  voteRate: number;
  opinionRate: number;
  opposingViewRate: number;
  revisitCohort: number;
  revisit7dRate: number;
};

export type ApiErrorBody = {
  code: string;
  message: string;
};

// 뉴스 자동 초안 실행 결과. 시각은 서버 기준 LocalDateTime 문자열
export type NewsDraftRun = {
  startedAt: string;
  finishedAt: string;
  articleCount: number;
  createdIssueIds: number[];
  skippedDrafts: number;
  error: string | null;
};

export type NewsDraftStatus = {
  running: boolean;
  lastRun: NewsDraftRun | null;
};
