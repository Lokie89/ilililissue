# 프로젝트 작업 기록 아카이브

`handoff-log.md`의 `현재 기록`에서 옮긴 오래된 기록이다. 오래된 순서로 쌓는다.

### 2026-10-28

- 목표: ai-agents 하네스 도입, 코드 분석, 기획 기반 개발 계획 수립, Phase 0(빌드·테스트 복구)
- 변경:
  - `D:\_Study\ai-agents`를 `ai-agents/`로 복사(`.git` 제외), 루트 `CLAUDE.md`/`AGENTS.md` 생성
  - 로컬 하네스 생성: `goal.md`, `domain-policy.md`, `architecture.md`, `context-map.md`, `tools.md`, `validation.md`, `docs/planning-2026-10-28.md`, `analysis/2026-10-28-codebase-analysis.md`
  - 사용자 결정 반영: 비회원은 열람만, 닉네임 표시, 투표 후 결과 공개, 입장 변경 불가, 추천 기능 + 추천순/최신순, REST API + Next.js(웹)/Expo(앱), Spring Security + JWT(쿠키)
  - Phase 0: `build.gradle`에 Java 17 toolchain, `IssueJpaEntity` 이미지 연관을 `@JoinColumn(name = "issueId")`로 수정, `Issue` 관리자 검증을 생성·수정 시점으로 한정(재구성 시 제외), `IssueTest`/`UserTest` 컴파일·null id 오류 수정
  - Spring Boot 4.1.2 + Gradle 9.8.2 업그레이드, web(mvc)/security/validation/MySQL 드라이버 추가, Boot 4 테스트 어노테이션 패키지 변경 반영
  - `application.yml` 프로필 분리(`local` 기본, 테스트는 `src/test/resources/application.yml`로 H2), `docker-compose.yml` 비밀값 환경변수화, `.env.example` 추가
  - Phase 1 도메인 재설계: 기존 `comment`, `member`, `issue`(이미지 포함) 코드 삭제 후 `member`, `issue`(Issue, Category, IssueSource), `vote`, `opinion`(Opinion, Recommendation), `report` 도메인과 단위 테스트 작성. 영속성 계층은 아직 없음
  - `domain-policy.md`에 가안 값(닉네임 2~12자, 제목 100자, 설명 1000자, 의견 500자, 신고 사유 300자)과 이슈 수정·정정 규칙 반영
- 검증: `JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew test` → 30개 통과. `bootRun`(MySQL)은 미실행 — `.env` 미작성
- 남은 작업:
  - Phase 2 인증: 회원 영속성, 가입/로그인/로그아웃, Spring Security + JWT 쿠키
  - Phase 3 이슈 API(공개 조회, 관리자 등록·상태 전이), Phase 4 투표·의견·추천(영속성 + 유니크 제약 테스트), Phase 5 신고·참여 이벤트
- 주의 사항:
  - 기본 `JAVA_HOME`이 JDK 8이라 Gradle 실행 시 JDK 17 지정 필요 (Gradle 9도 JDK 17 이상 필요)
  - 모듈 간 참조는 typed id(`MemberId`, `IssueId`, `OpinionId`)로 한다. `Vote.cast`는 `Issue`, `Opinion.write`는 `Vote`에 의존한다
  - 추천 기능은 기획 원문(좋아요·싫어요 MVP 제외)과 다른 사용자 결정이며 세부 정책(취소, 자기 의견 추천)은 미정

### 2026-10-28 (2) — MVP API 구현

- 목표: Phase 2~5 백엔드 MVP API 완성 (사용자 지시: 별도 지시 없으면 다음 Phase로 계속 진행)
- 변경:
  - Phase 2 인증: 회원가입·로그인·재발급·로그아웃, Spring Security OAuth2 Resource Server + Nimbus JWT, HttpOnly 쿠키, 리프레시 토큰 해시 저장·rotation
  - Phase 3 이슈: 공개 조회 API, 관리자 카테고리·이슈 작성·상태 전이·정정 API
  - Phase 4 참여: 투표, 결과(투표자 공개), 의견 작성·수정, 입장별 목록(추천순/최신순), 추천
  - Phase 5 운영: 신고·관리자 숨김/기각, 참여 이벤트 로그, 관리자 지표 API
  - `README.md` 작성 (실행, 인증, API 목록)
- 검증: `./gradlew test` → 59개 통과 (H2). MySQL 실기동(`bootRun`)은 미실행 — Docker Desktop 데몬이 꺼져 있음
- 남은 작업:
  - Docker Desktop 실행 후 MySQL로 `bootRun` 및 핵심 흐름 수동 확인
  - Phase 6 웹 클라이언트(Next.js), 앱(Expo)은 4주 검증 이후
  - 운영 DB 스키마 관리(Flyway) 도입 — 현재 `ddl-auto: update`
  - 미정 정책: 추천 취소·자기 의견 추천, 비회원 방문자 식별, 의견 삭제, 회원 탈퇴
- 주의 사항:
  - 결과·의견 목록·추천은 투표자만 가능. 비로그인 401, 미투표 403
  - 의견 목록 조회는 탭 열람 이벤트를 기록하므로 읽기 전용 트랜잭션이 아니다
  - 지표 정의는 `domain-policy.md`의 ParticipationEvent 참고

### 2026-10-28 (3) — 웹 클라이언트

- 목표: Phase 6 웹 클라이언트로 MVP 핵심 흐름을 화면에서 사용 가능하게 만들기
- 변경:
  - `web/` Next.js 16.4 앱: 이슈 목록·상세, 투표→결과→입장별 의견(다른 입장 탭 먼저), 의견 작성·1회 수정, 추천, 신고, 로그인·가입, 관리자(이슈·카테고리·신고·지표)
  - `/api/*` rewrite 프록시로 쿠키 인증, 401 시 1회 재발급 후 재시도
  - 백엔드 개발용 `./gradlew bootTestRun` (H2 + 예시 데이터, `src/test`에만 존재)
- 검증: `npm run lint` 통과, `npm run build` 통과, `bootTestRun` + `next dev`로 프록시 경유 가입·로그인·투표·의견·결과·재발급·관리자 이슈 작성/게시·지표·로그아웃 curl 확인. 브라우저 화면 확인과 MySQL 실행은 미실행
- 남은 작업:
  - 브라우저로 화면 확인(반응형 포함), MySQL로 `bootRun` 확인
  - 배포 구성(웹·API 같은 도메인 또는 프록시), Flyway
  - 앱(Expo)은 4주 검증 이후
- 주의 사항:
  - 스트리밍 응답이라 없는 이슈도 HTTP 200 + not-found 화면(noindex)으로 응답한다
  - `web/AGENTS.md`는 `next dev`가 다시 만드는 파일이므로 커밋해 둔다

### 2026-10-28 (4) — 미정 정책 반영, MySQL 실행 확인

- 목표: 사용자 결정(추천 취소 허용·자기 추천 금지, 의견 삭제 허용·재작성 불가, 회원 탈퇴 포함·의견은 '탈퇴한 회원'으로 유지) 반영, MySQL 실기동 확인
- 변경:
  - `DELETE /api/opinions/{id}/recommendations`, 자기 의견 추천 403
  - `DELETE /api/opinions/{id}` (삭제 표시, 재작성·수정·추천·신고 불가, 참여 상태에 `deleted`)
  - `POST /api/members/me/withdrawal {password}`: 이메일·닉네임·비밀번호 null + `withdrawnAt`, `MemberWithdrawnEvent`로 리프레시 토큰 폐기, JWT 변환 시 회원 활성 여부 확인
  - 웹: 추천 토글, 내 의견 삭제, `/me` 내 정보·탈퇴
- 검증: `./gradlew test` 66개 통과, `npm run lint`·`npm run build` 통과. Docker MySQL 8.0 + `bootRun`에서 스모크 스크립트 34개 항목(가입·관리자 이슈 게시·투표·의견·추천/취소·자기 추천·삭제·재작성·탈퇴·재가입·지표·로그아웃)을 직접 호출과 `next dev` 프록시 경유 양쪽에서 통과. 브라우저 화면 확인은 미실행
- 남은 작업: Flyway(현재 `ddl-auto: update`), 배포 구성, 브라우저 화면 확인, 비회원 방문자 식별, 숨김 해제
- 주의 사항:
  - 인증된 요청마다 회원 활성 여부를 DB에서 한 번 조회한다(탈퇴 직후 액세스 토큰 차단용)
  - 로컬 `.env`는 임의 값으로 생성되어 있다(커밋 안 됨). MySQL 컨테이너 `mysql-docker`는 `restart: always`라 Docker를 켜면 같이 뜬다

### 2026-10-28 (5) — 숨김 해제, 방문자 쿠키, Flyway, 배포 구성

- 목표: 사용자 결정(비회원 쿠키 식별, 숨긴 의견 되돌리기, 단일 서버 클라우드 배포, Flyway, push만·PR 보류) 반영
- 변경:
  - 관리자 숨긴 의견 목록 `GET /api/admin/opinions/hidden`, `POST /api/admin/opinions/{id}/unhide`, 관리 화면 "숨긴 의견" 탭
  - 조회 기록을 `POST /api/issues/{id}/views`로 분리(브라우저의 `ViewTracker`가 호출), `visitor_id` 쿠키 발급, 투표율 분모 = 이슈별 고유 방문자 수. `GET /api/issues/{id}`는 더 이상 지표에 기록하지 않음(웹 서버 렌더링·메타데이터 조회가 조회수를 부풀리던 문제)
  - Flyway: `V1__init.sql`, 운영·로컬 `ddl-auto: validate`, `FlywayMigrationTest`(H2 MySQL 모드)
  - 배포: `Dockerfile`, `web/Dockerfile`(standalone), `docker-compose.prod.yml`(db·api·web·caddy), `deploy/Caddyfile`, `deploy/backup.sh`, `deploy/README.md`, `prod` 프로필(forward headers)
- 검증: `./gradlew test` 68개 통과, 웹 lint·build 통과. 로컬 MySQL에 Flyway V1 적용 후 `bootRun` 스모크 41개 통과. 운영 구성을 로컬에서 `-p ilililissue-prodtest`로 빌드·기동해 HTTPS(Caddy 내부 인증서) 경유 스모크 41개, SSR 페이지, Secure 쿠키 확인 후 `down -v`로 정리
- 남은 작업: 실제 클라우드 서버·도메인 준비 후 배포, 브라우저 화면 확인, PR(사용자 보류)
- 주의 사항:
  - 기존에 `ddl-auto: update`로 만든 로컬 DB는 Flyway 이력이 없어 실패한다. 로컬 `ilililissue` DB는 이번에 비우고 Flyway로 다시 만들었다
  - 앱 클라이언트도 상세 화면에서 조회 기록 API를 호출해야 투표율이 맞다
  - `next.config.ts`의 rewrite 대상은 빌드 시점에 고정된다(`BACKEND_URL` 빌드 인자)

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

### 2026-10-29 (4) — 초안 작성 AI를 OpenAI API로 교체

- 목표: "코덱스로 할래 코덱스키로 돌아가게해줘". 이 PC의 Codex는 ChatGPT 로그인(API 키 아님)이라 방식을 물었고, 사용자가 OpenAI API 키 방식을 골랐다
- 변경:
  - `ClaudeIssueDraftWriter`와 `anthropic-java` 의존성 삭제 → `OpenAiIssueDraftWriter`(SDK 없이 `RestClient` + Jackson 3 `JsonMapper`, Responses API `text.format` json_schema strict)
  - 설정 `news-draft.openai-api-key`(`OPENAI_API_KEY`), `news-draft.model`(`NEWS_DRAFT_MODEL`, 기본 `gpt-6.1-sol`; 최상위 `gpt-6-astra`는 5배 비쌈). `.env`, `.env*.example`, `docker-compose.prod.yml`, README, 아키텍처 문서 반영
  - 거절(`content[type=refusal]`)이면 0건, `status=incomplete`·HTTP 오류면 실행 실패. 읽기 타임아웃 10분
- 검증: `./gradlew test` 87개 통과(응답 파싱 테스트 3개 추가). 재시작 후 수동 실행 → "OpenAI API 키가 설정되지 않았습니다" 확인. **OpenAI 실제 호출은 키가 없어 미실행**
- 남은 작업: `.env`에 `OPENAI_API_KEY` 입력 후 실제 초안 생성·품질 확인(요청 형식이 실제 API와 맞는지도 이때 확인), 언론사 RSS 이용 조건 확인, 이전 남은 작업
- 주의 사항:
  - 모델 ID와 Responses API 형식은 2026-10-29 OpenAI 문서(developers.openai.com/api/docs)로 확인했다
  - OpenAI 서비스 약관상 API 입력·출력 데이터 처리 조건도 운영 전에 확인한다

### 2026-10-29 (5) — Codex CLI로 실제 초안 생성

- 목표: "지금은 테스트니까 그냥 해보자" — API 키 없이 이 PC의 Codex(ChatGPT 로그인)로 실제 초안 만들기
- 변경:
  - `CodexCliIssueDraftWriter`(`news-draft.writer=codex-cli`), 프롬프트·스키마를 `IssueDraftPrompt`로 분리해 OpenAI API 방식과 공유. 기본값은 `openai-api`
  - 설정 `writer`, `codex-command`, `codex-model`. 로컬 `.env`에 `NEWS_DRAFT_WRITER=codex-cli`
  - 로컬 DB 카테고리 이름이 깨져 있던 것 수정(이전 세션에 `docker exec mysql`로 넣을 때 클라이언트 문자셋 미지정)
- 검증: `./gradlew test` 88개 통과. MySQL `bootRun`에서 관리자 수동 실행 → 기사 200건으로 26초 만에 DRAFT 3건(#1 다문화가족 산후조리 지원 기준, #2 수능 스마트안경 부정행위 응시 제한, #3 수도권 신규 택지·개발제한구역) 생성, 출처는 수집 URL만
- 남은 작업: 초안 3건 사실관계 검수(특히 #3은 후보지가 개발제한구역인지 원문 확인 필요, #1은 출처 2건이 한겨레), 배경 설명이 목표(300~600자)보다 짧음(약 280자) → 프롬프트 조정 검토, 운영용 OpenAI API 키, 언론사 RSS 이용 조건 확인
- 주의 사항:
  - `docker exec ... mysql`로 한글을 넣을 때는 `--default-character-set=utf8mb4`를 붙인다
  - Codex CLI 방식은 서버 PC에 codex 설치·ChatGPT 로그인이 필요하고 요금제 한도를 쓴다. 운영 자동 실행에는 OpenAI API 키 방식을 쓴다

### 2026-10-29 (6) — 투표 자동 마감(06:59)과 연장, 관리자 계정

- 목표: "마감도 수동 할수 있고 전날꺼 6시59분에 자동 마감", "연장하면 열리는데 또 다음날 6시 59분에 마감"
- 변경:
  - `IssueAutoCloseScheduler`(매일 06:59 Asia/Seoul) → `CloseExpiredIssuesUseCase.closePublishedBeforeToday()`: `coalesce(extended_at, published_at) < 오늘 0시`인 `PUBLISHED` 마감
  - `Issue.extend(now)`(`PUBLISHED`·`CLOSED` → `PUBLISHED`, `extendedAt` 기록), `POST /api/admin/issues/{id}/extend`, 응답에 `extendedAt`, Flyway `V4__issue_extended_at.sql`
  - 관리 화면: 게시 중 "연장", 마감 "다시 열기(연장)" 버튼, 연장 시각 표시
  - 로컬 관리자 계정 `admin@local.test`(닉네임 관리자, `admin` 닉네임은 다른 회원이 사용 중) 생성
- 검증: `./gradlew test` 93개 통과(도메인 연장 2개, 자동 마감·연장 통합 3개 추가), 웹 lint·tsc 통과, 로컬 MySQL V4 적용 확인
- 남은 작업: 이전과 같음
- 주의 사항:
  - 스모크 테스트에서 #1이 이미 게시된 줄 모르고 연장 API를 호출해 `extended_at`이 기록됐고, 바로 NULL로 되돌렸다. 공유 DB에서 상태를 바꾸는 호출 전에는 현재 상태를 먼저 조회한다
  - 06:59 마감과 07:00 초안 작성 모두 서버가 켜져 있어야 동작한다
