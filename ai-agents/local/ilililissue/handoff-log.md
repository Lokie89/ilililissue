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

### 2026-10-29 (9) — 분야마다 1건 초안, 로컬 이슈 정리 후 오늘 이슈 5건 게시

- 목표: "지금 있는거 정리해주고 각 분야별로 오늘 뉴스중에 인기많았던 내용 토대로 작성". 사용자 선택: 기존 9건 모두 삭제, 바로 게시, 매일 자동 초안도 분야마다 1건
- 변경:
  - `news-draft.max-drafts` → `drafts-per-category`(기본 1). 프롬프트: 카테고리마다 보도량이 가장 많은 쟁점 1건, '생활'은 사회·경제 면의 일상 주제에서, 한 주제는 한 카테고리에만. 서비스가 분야별 상한을 넘는 초안은 건너뜀
  - 로컬 DB: 이슈 9건·출처·이용 기록 삭제(투표·의견·신고 0건), `issue` AUTO_INCREMENT 초기화
- 검증: `./gradlew test` 96개 통과(분야별 상한 1개 추가). Codex CLI로 기사 378건 → 43초, 5건 생성 후 검수·게시: #1 정치 전작권 전환 시기, #2 경제 퇴직급여 사외적립 의무화, #3 사회 수능 AI 안경 응시 제한 3년, #4 생활 경기 그린벨트 해제 주택 공급, #5 국제 이스라엘 진영 넘는 연정. 분야별 공개 이슈 각 1건, 웹 홈·분야 탭·상세 200
- 남은 작업: #4 수치(6곳, 약 7만6700가구) 원문 대조, #5는 국내 독자 관련성이 약함(필요하면 교체), `npm run build` 확인, 운영용 OpenAI API 키, 언론사 RSS 이용 조건
- 주의 사항: 관리자 액세스 토큰은 30분이라 긴 스크립트에서는 재로그인하고, 상태 폴링은 401을 실패로 처리한다(이번에 10분 낭비)

### 2026-10-29 (8) — 분야별 이슈 목록, 이슈 화면 오류(웹 서버 워커 손상) 복구

- 목표: "분야별로 보고 들어갈수있게", "내 입장 고르기 누르면 pending만 반복"
- 변경:
  - `GET /api/issues?categoryId=`(공개 이슈 분야 필터, `IssuePort.findByStatusInAndCategory`)
  - 웹 홈: 분야 탭(전체·정치·경제·사회·생활·국제, `/?category=<id>`), 분야별 강조 카드·지난 이슈, 빈 분야 안내. 이슈 상세의 분야 칩을 그 분야 목록 링크로
- 검증: `./gradlew test` 95개 통과(분야 필터 API 1개 추가), 웹 lint·tsc 통과, Chrome 1280px·390px로 분야 탭 이동·빈 분야 안내 확인(가로 스크롤 0, 오류 0). **`npm run build`는 여유 메모리 0.9GB라 dev 서버가 다시 죽을 수 있어 미실행**
- 남은 작업: 메모리 여유가 있을 때 `cd web && npm run build` 확인, 이전 남은 작업
- 주의 사항:
  - 메모리 부족으로 Claude Code가 백그라운드 셸을 정리할 때 `next dev`의 렌더링 워커가 함께 죽어 이슈 상세가 500(`Jest worker encountered 2 child process exceptions`)이 났다. 홈은 200이라 정상으로 오판했다. 서버 상태는 홈 하나가 아니라 상세 페이지까지 확인하고, 이 오류면 `next dev`를 다시 띄운다

### 2026-10-29 (7) — 뉴스 수집을 면별 피드로, 보도량 기준

- 목표: 기사 거르는 기준 정리(사용자 선택 A: 면별 피드), "최신순 말고 인기 많은 것"
- 확인: 언론사 RSS에 조회수·"많이 본 뉴스" 피드가 없다(연합·동아·한겨레·경향·SBS·한경 후보 주소 모두 404/빈 응답). 인기 대신 보도량(다룬 언론사 수)을 기준으로 삼았다
- 변경:
  - 피드 32개: 8곳 × 정치·경제·사회·국제(`Feed`에 `section`, `NewsArticle`에 `section`). 동아 사회는 `national.xml`, 조선 사회는 `category/national`, 경향 국제는 `kh_world.xml`, SBS 국제는 `sectionId=07`
  - 수집: 피드마다 24시간 내 기사를 시간순으로 놓고 최대 12건을 같은 간격으로 뽑음(`evenlySpaced`), 전체 최대 400건. 최신순 자르기 제거
  - 프롬프트: 보도량(가능하면 3곳 이상) 우선, 분야가 한쪽으로 몰리지 않게, 기사마다 `section` 표시
- 검증: `./gradlew test` 94개 통과, 실제 피드 32개 파싱 확인. Codex CLI 실행 → 기사 376건, 36초, 초안 3건(#7 전작권 전환 시기[정치, 경향·동아·한겨레], #8 의무공개매수제[경제, 경향·SBS·한경], #9 민간 정보시설 보안점검 확대[사회, 동아·경향·한겨레])
- 남은 작업: 초안 검수, 운영용 OpenAI API 키, 언론사 RSS 이용 조건 확인
- 주의 사항: 동아 사회·국제 피드에 같은 기사가 겹쳐 나온다(URL 중복 제거로 처리)

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

