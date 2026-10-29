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

