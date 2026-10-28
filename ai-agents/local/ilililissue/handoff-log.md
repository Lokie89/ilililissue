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
