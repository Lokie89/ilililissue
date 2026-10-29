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

