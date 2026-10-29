# 1일 1이슈 (ilililissue)

매일 선정한 사회·경제·생활 이슈에 찬성·중립·반대로 투표하고, 서로 다른 입장의 의견을 비교하는 서비스입니다. 이 저장소에는 API 서버(루트)와 웹 클라이언트([`web/`](./web/))가 있습니다.

> '누가 이겼는지 보여주는 서비스'가 아니라 '사람들이 왜 다르게 생각하는지 보여주는 서비스'.

- 기획·도메인 정책: [`ai-agents/local/ilililissue/`](./ai-agents/local/ilililissue/) (`goal.md`, `domain-policy.md`, `architecture.md`)
- 에이전트 작업 기준: [`CLAUDE.md`](./CLAUDE.md), [`AGENTS.md`](./AGENTS.md)

## 기술 스택

Java 17, Spring Boot 4.1, Spring Security(JWT, OAuth2 Resource Server), Spring Data JPA, MySQL 8, Gradle 9. 헥사고날 구조의 모듈러 모놀리스(`member`, `auth`, `issue`, `vote`, `opinion`, `report`, `metrics`)입니다.

## 실행

DB 스키마는 Flyway 마이그레이션(`src/main/resources/db/migration`)으로 만들어지며, 앱이 시작할 때 자동 적용됩니다. JDK 17 이상이 필요합니다. 기본 `JAVA_HOME`이 JDK 8이면 아래처럼 지정합니다.

```sh
cp .env.example .env            # 값 채우기 (DB_ROOT_PASSWORD, DB_PASSWORD, JWT_SECRET)
docker compose up -d            # MySQL 8

set -a; . ./.env; set +a        # 환경변수 로드 (bash)
JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew bootRun
```

MySQL 없이 H2와 예시 데이터(관리자 `admin@local.test`, 회원 `user@local.test`, 비밀번호 `password1234`)로 띄우려면 `./gradlew bootTestRun`을 씁니다. 개발 전용이며 운영 코드에 포함되지 않습니다(`src/test/java/com/ilililissue/TestIlililissueApplication.java`).

웹 클라이언트 실행은 [`web/README.md`](./web/README.md), 운영 배포(단일 서버, Docker Compose + Caddy)는 [`deploy/README.md`](./deploy/README.md)를 참고하세요.

기본 프로필은 `local`입니다. 쿠키 `Secure`가 꺼져 있고 CORS 허용 Origin은 `http://localhost:3000`입니다.

### 관리자 지정

가입 API는 항상 일반 회원(`USER`)을 만듭니다. 관리자는 가입 후 DB에서 지정합니다.

```sql
UPDATE member SET role = 'ADMIN' WHERE email = '<관리자 이메일>';
```

## 테스트

```sh
JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew test
```

H2 인메모리 DB를 쓰므로 MySQL 없이 실행됩니다.

## 인증

- `POST /api/auth/login` 성공 시 `access_token`(30분), `refresh_token`(14일, 경로 `/api/auth`) 쿠키를 `HttpOnly`, `SameSite=Lax`로 발급합니다.
- 웹은 쿠키로, 앱은 `Authorization: Bearer <access token>` 헤더로 인증합니다.
- 앱은 로그인·재발급·로그아웃 요청에 `X-Client: app` 헤더를 붙입니다. 그러면 토큰을 쿠키 대신 응답 본문 `{member, accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn}`(만료는 초)으로 주고, 재발급·로그아웃은 본문 `{refreshToken}`을 받습니다.
- 액세스 토큰이 만료되면 `POST /api/auth/refresh`로 재발급합니다. 리프레시 토큰은 1회용(rotation)입니다.
- 오류 응답 형식: `{"code": "UNAUTHORIZED", "message": "로그인이 필요합니다."}`

## API

| 메서드 | 경로 | 권한 | 설명 |
| --- | --- | --- | --- |
| POST | `/api/members` | 누구나 | 회원가입 `{email, nickname, password, agreedToTerms}` (`agreedToTerms`가 true가 아니면 400) |
| GET | `/api/members/me` | 회원 | 내 정보 |
| POST | `/api/members/me/withdrawal` | 회원 | 탈퇴 `{password}` (인증 쿠키 삭제, 의견은 '탈퇴한 회원'으로 남음) |
| GET | `/api/members/me/blocks` | 회원 | 내가 차단한 회원 목록 `[{id, nickname, blockedAt}]` |
| DELETE | `/api/members/me/blocks/{blockId}` | 회원 | 차단 해제 |
| POST | `/api/auth/login` | 누구나 | 로그인 `{email, password}` |
| POST | `/api/auth/refresh` | 리프레시 쿠키 | 토큰 재발급 |
| POST | `/api/auth/logout` | 누구나 | 로그아웃 (쿠키 삭제, 리프레시 토큰 폐기) |
| GET | `/api/categories` | 누구나 | 카테고리 목록 |
| GET | `/api/issues?page&size` | 누구나 | 공개 이슈 목록 (게시·마감, 최신순) |
| GET | `/api/issues/{id}` | 누구나 | 이슈 상세 (질문, 배경 설명, 원문 출처) |
| POST | `/api/issues/{id}/views` | 누구나 | 조회 기록 (지표용, 없으면 `visitor_id` 쿠키 발급. 앱은 `X-Visitor-Id: <UUID>` 헤더) |
| GET | `/api/issues/{id}/participation` | 누구나 | 내 투표 입장·의견 (비회원은 `voted: false`) |
| POST | `/api/issues/{id}/votes` | 회원 | 투표 `{stance: AGREE \| NEUTRAL \| DISAGREE}` (1회, 변경 불가) |
| GET | `/api/issues/{id}/results` | 투표자 | 입장별 투표 수·비율 |
| GET | `/api/issues/{id}/opinions?stance&sort&page&size` | 투표자 | 입장별 의견 (`sort`: `RECOMMENDED`(기본) \| `LATEST`) |
| POST | `/api/issues/{id}/opinions` | 투표자 | 의견 작성 `{content}` (1개, 500자) |
| PUT | `/api/opinions/{id}` | 작성자 | 의견 수정 `{content}` (1회) |
| DELETE | `/api/opinions/{id}` | 작성자 | 의견 삭제 (삭제 후 같은 이슈에 다시 작성 불가) |
| POST | `/api/opinions/{id}/recommendations` | 투표자 | 의견 추천 (1회, 자기 의견 불가) |
| DELETE | `/api/opinions/{id}/recommendations` | 회원 | 추천 취소 |
| POST | `/api/opinions/{id}/reports` | 회원 | 의견 신고 `{reason}` (1회) |
| POST | `/api/opinions/{id}/author-block` | 회원 | 의견 작성자 차단 (내 의견 목록에서 그 회원의 의견이 빠짐, 상대에게 알리지 않음) |
| POST | `/api/admin/categories` | 관리자 | 카테고리 생성 `{name}` |
| GET | `/api/admin/issues` | 관리자 | 전체 이슈 목록 (모든 상태) |
| GET | `/api/admin/issues/{id}` | 관리자 | 이슈 상세 (모든 상태) |
| POST | `/api/admin/issues` | 관리자 | 이슈 작성 `{title, summary, categoryId, sources[{url, publisher, publishedOn}]}` |
| PUT | `/api/admin/issues/{id}` | 관리자 | 이슈 수정 (작성·검수 상태만, 수정하면 다시 작성 상태) |
| POST | `/api/admin/issues/{id}/review` | 관리자 | 검수 완료 |
| POST | `/api/admin/issues/{id}/publish` | 관리자 | 게시 (출처 1개 이상) |
| POST | `/api/admin/issues/{id}/close` | 관리자 | 투표 마감 |
| POST | `/api/admin/issues/{id}/extend` | 관리자 | 투표 연장 (게시 중·마감 이슈, 마감이면 다시 열림. 다음 날 06:59에 다시 자동 마감) |
| POST | `/api/admin/issues/{id}/correction` | 관리자 | 게시 후 배경 설명 정정 `{summary}` |
| GET | `/api/admin/reports?status&page&size` | 관리자 | 신고 목록 (기본 `RECEIVED`) |
| POST | `/api/admin/reports/{id}/hide` | 관리자 | 신고된 의견 숨김 |
| POST | `/api/admin/reports/{id}/dismiss` | 관리자 | 신고 기각 |
| POST | `/api/admin/opinions/{id}/hide` | 관리자 | 의견 직접 숨김 |
| GET | `/api/admin/opinions/hidden?page&size` | 관리자 | 숨긴 의견 목록 |
| POST | `/api/admin/opinions/{id}/unhide` | 관리자 | 숨김 해제 |
| GET | `/api/admin/metrics?from&to` | 관리자 | 참여 지표 (날짜 `YYYY-MM-DD`) |
| POST | `/api/admin/news-drafts` | 관리자 | 뉴스 자동 초안 수동 실행 (백그라운드, 202. 실행 중이면 409) |
| GET | `/api/admin/news-drafts/status` | 관리자 | 실행 중 여부와 마지막 실행 결과 `{running, lastRun}` |

이슈 상태: `DRAFT` → `REVIEWED` → `PUBLISHED` → `CLOSED`. 투표는 `PUBLISHED`에서만 가능합니다. 매일 06:59(Asia/Seoul)에 그날 0시 전에 게시(연장했으면 연장)된 이슈를 자동 마감합니다.

## 뉴스 자동 초안

매일 07:00(Asia/Seoul)에 언론사 8곳(연합뉴스, 한겨레, 경향신문, 동아일보, 조선일보, SBS, 한국경제, 뉴시스)의 정치·경제·사회·국제 면 RSS 32개에서 지난 24시간 기사를 피드마다 최대 12건씩 시간대별로 고르게 모으고, OpenAI(`gpt-6.1-sol`, Responses API 구조화 출력)가 토론할 만한 주제를 골라 질문·배경 설명 초안을 씁니다. 초안은 `DRAFT`로만 등록되고, 관리자가 검수·게시합니다. 관리 화면의 "지금 초안 만들기"로 바로 실행할 수도 있습니다.

- 환경변수: `OPENAI_API_KEY`([platform.openai.com](https://platform.openai.com) API 키, ChatGPT 구독과 별도 과금. RSS는 키가 필요 없습니다). 모델은 `NEWS_DRAFT_MODEL`로 바꿀 수 있습니다.
- 로컬 테스트: `.env`에 `NEWS_DRAFT_WRITER=codex-cli`를 넣으면 API 키 없이 이 PC에 설치·로그인된 Codex CLI(ChatGPT 요금제)로 초안을 씁니다. 운영 서버의 매일 자동 실행에는 쓰지 않습니다. 매일 자동 실행은 `NEWS_DRAFT_ENABLED=true`일 때만 합니다.
- 피드 목록·건수: `application.yml`의 `news-draft.feeds`. **운영 전에 언론사별 RSS 이용 조건(상업적 이용, AI 입력)을 확인하거나 허락을 받아야 합니다.** 네이버 검색 API는 2026-09-07 약관부터 검색 결과를 AI에 넣는 것을 금지해 쓰지 않습니다.
- RSS에는 조회수가 없어 관심도는 보도량(같은 주제를 다룬 언론사 수)으로 판단합니다. 여러 언론사가 함께 다룬 주제를 우선합니다.
- 기사 제목·요약문(앞 200자)은 초안 작성에만 쓰고 저장하지 않습니다. 출처는 수집한 기사 URL만 인정하고, 언론사·발행일은 피드 값을 씁니다.
