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
