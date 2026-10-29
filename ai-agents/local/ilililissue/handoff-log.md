# 프로젝트 작업 기록

## 아카이브 안내

- `현재 기록`에는 최근 5개 세션 기록만 남긴다.
- 그보다 오래된 기록은 오래된 순서대로 `./handoff-archive.md`로 옮긴다.
- 여기서 찾는 내용이 보이지 않으면 `./handoff-archive.md`(있다면)를 먼저 확인한다.

## 세션 기록 템플릿

### YYYY-MM-DD

- 목표:
- 변경:
- 검증:
- 남은 작업:
- 주의 사항:

## 현재 기록

### 2026-10-29 (3) — 뉴스 수집원을 언론사 RSS로 교체, 더미 데이터 정리

- 목표: 실제 이슈 가져오기(사용자: "실제 이슈들좀 가져와바", 네이버 개발자센터에 '검색' API가 없다는 제보)
- 확인: 네이버 검색 API는 2026-07-30부터 개발자센터 신규 신청이 막혔고(NAVER API HUB로 이관), 2026-09-07 시행 약관이 검색 결과를 AI에 입력하는 것을 금지한다(뉴스 보도·블로그 근거, 공식 공지 원문은 미확인). 사용자 선택: 언론사 RSS로 교체
- 변경:
  - `NaverNewsSearchAdapter` 삭제 → `RssNewsFeedAdapter` + `RssFeedParser`(RSS 2.0·1.0·Atom, DOCTYPE 거부, 요약문 앞 200자, utm 제거, 항목 시각 없으면 `lastBuildDate`). 포트 `NewsFeedPort`
  - 피드 8곳(`news-draft.feeds`): 연합뉴스, 한겨레, 경향신문, 동아일보, 조선일보, SBS, 한국경제, 뉴시스. 매일경제는 Cloudflare 봇 차단이라 제외
  - 언론사 이름·발행일은 피드 값을 쓰고, AI 응답은 `sourceUrls`만 받는다. 프롬프트에 논조가 다른 언론사 기사를 함께 고르라고 지시
  - 네이버 환경변수 제거. 필요한 키는 `ANTHROPIC_API_KEY` 하나
  - 로컬 MySQL: 이슈·카테고리·의견·투표·추천·신고·이용 기록을 비우고(회원은 유지) 카테고리 정치·경제·사회·생활·국제 생성. `.env`에 `NEWS_DRAFT_ENABLED`, `ANTHROPIC_API_KEY` 빈 줄 추가
- 검증: `./gradlew test` 84개 통과. 실제 피드 8곳 파싱 확인(총 529건, 임시 테스트로 확인 후 삭제). 재시작 후 수동 실행 → "Anthropic API 키가 설정되지 않았습니다" 확인. **Claude 호출은 키가 없어 미실행**
- 남은 작업: `ANTHROPIC_API_KEY` 입력 후 실제 초안 생성·품질 확인, 언론사별 RSS 이용 조건 확인 또는 이용 허락(운영 전 필수), 이전 남은 작업
- 주의 사항:
  - 신문사 공동 「디지털 뉴스콘텐츠 이용규칙」은 RSS를 개인 열람용으로 보고 봇 대량 수집·영리 목적 링크 모음을 문제 삼는다. 본문 저장·게시는 하지 않지만 법적 확인이 필요하다
  - 한겨레 피드는 항목 시각이 없어 피드 갱신 시각을 발행일로 쓴다(하루 이내 오차)
  - 로컬 `.env`는 UTF-8이 아니다(CP949로 보임). 스크립트로 고칠 때 바이트 단위로 다룬다

### 2026-10-29 (2) — 웹 디자인 정리

- 목표: 웹 화면 디자인 손보기(사용자: "디자인 손좀봐줘")
- 변경:
  - 디자인 토큰(`web/src/app/globals.css` `@theme`): 바탕 `canvas`, 글자 `ink`/`muted`/`subtle`, 선 `line`, 입장 색 `stance-agree`·`stance-neutral`·`stance-disagree`(+`-soft`). 공통 클래스 `card`, `btn`(`btn-primary`·`btn-secondary`·`btn-ghost`·`btn-danger`·`btn-sm`), `field`, `chip`, `eyebrow`, `link`, `notice`, `error-text`
  - 글꼴 Pretendard(npm `pretendard`, 동적 서브셋), 세 입장 점 로고(`components/logo.tsx`), 날짜 표시 `lib/format.ts`(서울 기준 "10월 28일"), 입장 색 `lib/stance.ts`
  - 홈: 최신 이슈 강조 카드 + 지난 이슈 목록. 상세: 배경·원문 카드, 입장 선택 카드형 버튼, 결과 막대 + 입장별 수치 카드, 입장 색 탭과 의견 카드. 로그인·가입·내 정보·관리 화면을 같은 컴포넌트로 통일. 관리 화면 모바일 가로 스크롤(출처 입력 4열, 이슈 표) 해결
  - 동작·문구는 그대로(추천 용어 유지), 스타일만 변경
- 검증: 웹 lint·build 통과. Chrome(playwright-core) 1280px·390px로 홈·상세(비로그인·투표 전·투표 후)·로그인·가입·내 정보·관리 캡처 확인, 가로 스크롤 0, 페이지 오류 0
- 남은 작업: 이전과 같음(키 발급 후 뉴스 초안 실제 실행, 약관·처리방침 기입, 보관 기간, 회원 정지, 배포, PR)
- 주의 사항:
  - `globals.css`에 `@import "tailwindcss"` 뒤로 다른 `@import`를 넣으면 이후 `@theme`·`@layer components`가 통째로 빠졌다(Next 16 Turbopack, 오류 없음). 외부 CSS는 `layout.tsx`에서 JS로 import한다
  - 새 화면은 `stone-*` 같은 원색 클래스 대신 토큰 클래스를 쓴다. 입장 색은 `STANCE_STYLE`로만 쓴다

### 2026-10-29 — 뉴스 자동 초안(네이버 뉴스 검색 + Claude)

- 목표: 매일 아침 7시에 여러 언론사 기사를 모아 내용을 추린 이슈 초안을 자동 등록(사용자: "응 진행해줘" — 네이버 뉴스 검색 API + Claude 요약 + 초안으로만 등록 안 승인). 세션 시작 시 서버 기동 요청은 Docker MySQL + `bootRun` + `next dev`로 처리
- 변경:
  - 모듈 `newsdraft`: `NewsDraftService`(지난 24시간 수집·URL 중복 제거·최대 200건, 초안 검증 후 `ManageIssueUseCase.create`로 `DRAFT` 등록), `NaverNewsSearchAdapter`(RestClient), `ClaudeIssueDraftWriter`(Java SDK `anthropic-java:3.2.0`, 구조화 출력, `claude-opus-5-5`), `NewsDraftScheduler`(07:00, `news-draft.enabled=true`일 때만), `POST /api/admin/news-drafts`(백그라운드, 202/409), `GET /api/admin/news-drafts/status`
  - 설정 `news-draft.*`, 환경변수 `NEWS_DRAFT_ENABLED`, `NEWS_DRAFT_KEYWORDS`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `ANTHROPIC_API_KEY`(`.env.example`, `.env.prod.example`, `docker-compose.prod.yml`)
  - 웹 관리 화면 이슈 탭에 "뉴스 자동 초안" 섹션(지금 초안 만들기, 실행 중 5초마다 상태 확인, 끝나면 목록 새로고침)
  - 문서: `goal.md` 범위 변경, `architecture.md` 외부 연동 인터페이스, `domain-policy.md` 자동 초안, `README.md`, `deploy/README.md`
- 검증: `./gradlew test` 82개 통과(신규 8개: 서비스 단위·네이버 응답 변환·관리자 권한), 웹 lint·build 통과. MySQL `bootRun` + `next dev` 프록시 경유로 관리자 POST 202 → status에 "네이버 검색 API 키가 설정되지 않았습니다" 확인. **실제 네이버·Claude 호출은 키가 없어 미실행**
- 남은 작업: 네이버 검색 API·Anthropic API 키 발급 후 실제 실행으로 초안 품질(중립성·원문 표현 유사도·언론사 이름) 확인과 프롬프트 조정, 운영에서 `NEWS_DRAFT_ENABLED=true`. 이전 남은 작업(약관·처리방침 기입, 보관 기간 확정, 회원 정지 여부, 배포, PR)은 그대로
- 주의 사항:
  - 스모크 테스트용 관리자 계정 `newsdraft-*@example.com`을 로컬 MySQL에 만들었다
  - Claude 서버 측 거절 대체 모델(`fallbacks`)은 쓰지 않았다. 거절되면 그날 초안 0건으로 끝나고 마지막 실행 결과에 남는다
  - 실행 중 플래그와 마지막 결과는 메모리에만 있다. 서버를 여러 대로 늘리면 중복 실행 방지(DB 잠금 등)가 필요하다
  - 네이버 검색 API는 검색어가 필수라 분야 단어 10개로 넓게 모은다. 주제가 치우치면 `NEWS_DRAFT_KEYWORDS`를 조정한다

### 2026-10-28 (7) — 이용약관 동의, 이용 기록 보관, 브라우저 확인

- 목표: 앱 제출 전 남은 항목 이어서 진행(사용자: "이어서 진행해줘")
- 변경:
  - 이용약관 초안 `/terms`, 가입 시 필수 동의(`agreedToTerms`, 서버 검증 400), 동의 시각 `member.terms_agreed_at`(Flyway V3)
  - 이용 기록 보관 1년(가안): `metrics.event-retention`, `EventRetentionScheduler` 매일 04:00 삭제, `@EnableScheduling`
  - 처리방침에 동의 시각·보관 기간 반영, 결과 막대의 좁은 칸 줄바꿈 수정
- 검증: `./gradlew test` 74개 통과, 웹 lint·build 통과, MySQL V3 적용 후 스모크 50개 통과. Chrome(playwright-core, 스크래치패드 설치)으로 데스크톱 1280px·모바일 390px에서 가입(약관 미동의 차단)·투표·의견 탭·차단·내 정보 해제·약관/처리방침 페이지 18개 항목 확인, 가로 스크롤·페이지 오류 없음
- 남은 작업: 약관·처리방침 대괄호 항목 기입과 법률 검토, 보관 기간 확정, 회원 이용 제한(정지) 기능 여부, 실제 배포, PR(사용자 보류)
- 주의 사항:
  - 기존 회원(동의 절차 이전 가입)은 `terms_agreed_at`이 null이다. 운영 데이터가 없으므로 별도 재동의 절차는 만들지 않았다
  - 검증 오류 메시지는 `필드: 메시지` 형식이다(`GlobalExceptionHandler`). 앱이 그대로 보여주면 필드명이 보인다
  - `.gitignore`의 `db/` 규칙이 `src/main/resources/db/migration`까지 무시해 V1·V2가 커밋되지 않았었다. `/db/`로 고치고 V1~V3을 커밋했다(`b8cad2b`). 새 파일이 `git status`에 안 보이면 `git check-ignore -v <경로>`로 확인한다

### 2026-10-28 (6) — 앱 대비: 앱 모드 인증, 회원 차단, 개인정보 처리방침 초안

- 목표: 앱 출시 전 막히는 부분 해결(사용자: "응 수정해줘", "응 진행해줘")
- 변경:
  - 앱 모드 인증 `X-Client: app`(토큰을 본문으로, 재발급·로그아웃은 본문 `refreshToken`), 앱 방문자 ID `X-Visitor-Id` 헤더
  - 회원 차단: `POST /api/opinions/{id}/author-block`, `GET /api/members/me/blocks`, `DELETE /api/members/me/blocks/{blockId}`. 차단한 회원의 의견은 차단한 사람의 의견 목록에서만 빠진다. Flyway `V2__member_block.sql`
  - 웹: 의견 카드 "차단" 버튼, `/me`에 차단 목록·해제, `/privacy` 개인정보 처리방침 초안(대괄호 항목 미기입), 푸터·가입 화면 링크
- 검증: `./gradlew test` 73개 통과(Flyway V1+V2 검증 포함), 웹 lint·build 통과. 로컬 MySQL에 V2 적용 후 `bootRun` 스모크 50개 통과(차단·해제 포함)
- 남은 작업: 처리방침 대괄호 항목 기입·법률 검토, 이용약관(앱 심사 1.2), 이용 기록 보관 기간 결정, 브라우저 화면 확인, 실제 배포, PR(사용자 보류)
- 주의 사항:
  - 처리방침(`web/src/app/privacy/page.tsx`)은 실제 수집 항목·쿠키·보관 기간을 그대로 적었다. 수집 항목이나 보관 기간을 바꾸면 함께 고친다
  - 로컬 `ilililissue` DB는 V2까지 적용된 상태다

