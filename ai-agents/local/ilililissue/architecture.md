# 아키텍처 청사진

이 문서는 ilililissue의 기술 구조, 배포 형태, 데이터 저장소, 운영 전제를 정의합니다.

## 프로젝트 형태

- 유형: API 서버 + 클라이언트(웹, 이후 모바일 앱)
- 주요 배포 단위: Spring Boot API 서버 1개 (이 저장소). 클라이언트는 별도 저장소 또는 별도 디렉터리(미정)
- 주요 실행 흐름: 클라이언트 → REST API → MySQL

## 실행 환경

- 로컬 개발 환경: Windows, JDK 17 (`~/.jdks/corretto-17.0.13`), Docker Compose로 MySQL 8
- 서버 OS 또는 런타임 환경: 미정 (D-Note 가안: AWS EC2 + RDS + Route53)
- Docker 또는 컨테이너 사용 여부: 로컬 DB는 Docker. 서버 배포는 미정
- 예상 PC 또는 서버 스펙: 미정 (검증 단계 소규모)

## 기술 스택

- 언어: Java 17
- 프레임워크: Spring Boot 4.1.x, Spring Web, Spring Data JPA, Spring Security
- 런타임 버전: JDK 17
- 패키지 관리자: Gradle (wrapper)
- 주요 라이브러리: Lombok, Hibernate Validator, Spring Security OAuth2 Resource Server(Nimbus JOSE), MySQL Connector/J, H2(테스트)

## 클라이언트 선택 (2026-10-28)

- 백엔드는 화면을 렌더링하지 않는 JSON REST API로만 만든다. 웹과 앱이 같은 API를 쓴다.
- 웹: Next.js (React, TypeScript). 이슈 상세 페이지가 검색·공유 링크로 외부 유입되어야 하므로 서버 렌더링이 가능한 쪽을 택한다.
- 앱: React Native (Expo). 웹과 언어·타입·API 클라이언트를 공유한다.
- 순서: API → 웹 → 앱. 앱은 4주 검증 이후. 앱 착수 전 `app-client-guide.md`를 확인한다.
- 웹 구현 (2026-10-28): 저장소 내 `web/` (Next.js 16.4, Cache Components, Tailwind 4). 브라우저 요청은 Next.js rewrite로 `/api/*` → 백엔드(같은 출처라 CORS 불필요). 공개 이슈는 서버 컴포넌트가 백엔드를 직접 호출(Suspense 안, 요청마다 조회), 회원별 화면은 클라이언트 컴포넌트. 상세: `web/README.md`

## 애플리케이션 구조

- 구조 유형: 모듈러 모놀리스, 헥사고날 아키텍처
- 주요 모듈: `member`, `issue`, `vote`, `opinion`(기존 `comment`), `report`, `metrics`, `newsdraft`(뉴스 자동 초안), `common`
- 모듈 내부 패키지: `domain` / `application`(port.in, port.out, service) / `adapter.in.web` / `adapter.out.persistence`
- 도메인 모델과 JPA 엔티티는 분리한다. 모듈 간 참조는 id 값으로 한다.

## 통신 방식

- 내부 통신: 메서드 호출 (모놀리스)
- 외부 API 방식: REST (JSON)
- 이벤트 기반 통신 여부: 없음

## 외부 연동 인터페이스 문서

뉴스 자동 초안(2026-10-29, 모듈 `newsdraft`). 실패해도 서비스 핵심 흐름에는 영향이 없다.

| 연동 | 방향·방식 | 계약 | 비밀값 | 실패 처리 |
| --- | --- | --- | --- | --- |
| 언론사 RSS 피드 8곳 | 아웃바운드 HTTPS, `RestClient`(JDK `HttpClient`, 리다이렉트 따름) | `GET <피드 URL>`, 인증 없음, User-Agent `ilililissue-news-draft/1.0`. RSS 2.0·RSS 1.0·Atom. 항목의 `title`, `link`(없으면 URL형 `guid`), `description`/`summary`(앞 200자), 시각 `pubDate`(RFC 1123, `+09:00`형 포함)·`dc:date`·`published`·`updated`, 없으면 피드 `lastBuildDate`(한겨레). `utm_*` 파라미터는 뗀다. 목록: `application.yml` `news-draft.feeds` | 없음 | 타임아웃 10초. 피드별 실패는 건너뛰고, 모두 실패하면 실행 실패. DOCTYPE 거부(XXE·봇 차단 HTML). 봇 차단은 우회하지 않는다(매일경제 제외) |
| OpenAI Responses API | 아웃바운드 HTTPS, `RestClient`(SDK 없이 JSON) | `POST https://api.openai.com/v1/responses`, 본문 `{model, instructions, input, text.format{type: json_schema, name: issue_drafts, schema, strict: true}}`. 모델 `news-draft.model`(기본 `gpt-6.1-sol`, 환경변수 `NEWS_DRAFT_MODEL`). 출력 `{drafts[{title, summary, categoryId, sourceUrls[]}]}`는 `output[type=message].content[type=output_text].text`. 입력은 카테고리·최근 이슈 질문·기사 메타데이터(언론사·URL·발행 시각·제목·요약문 앞 200자) | `OPENAI_API_KEY`(Bearer) | 거절(`content[type=refusal]`)이면 초안 0건, `status=incomplete`·HTTP 오류면 실행 실패. 읽기 타임아웃 10분, 재시도 없음 |

- 실행: `NewsDraftScheduler`(매일 07:00, `news-draft.enabled=true`일 때) 또는 `POST /api/admin/news-drafts`(백그라운드 `applicationTaskExecutor`). 서버 한 대 전제로 메모리 플래그로 중복 실행을 막고, 마지막 결과도 메모리에만 둔다(재시작하면 사라짐).
- 신뢰 경계: AI 응답의 출처 URL은 수집한 기사 URL과 일치할 때만 쓰고, 언론사·발행일은 피드 값을 쓴다. 카테고리 id가 없거나 이슈 검증(제목 100자 등)에 실패한 초안은 건너뛴다. 기사 텍스트는 프롬프트에서 데이터로만 취급하도록 지시한다.
- 비용: 기사 최대 200건(약 수만 토큰) 입력, 하루 1회 기준.
- 초안 작성 모델 변경(2026-10-29): 처음엔 Claude(Anthropic Java SDK)였으나 사용자 요청으로 OpenAI API 키 방식으로 바꿨다. Codex CLI(ChatGPT 로그인)를 서버에서 호출하는 방식은 운영 서버 자동 실행에 맞지 않아 쓰지 않는다.
- 수집원 변경(2026-10-29): 처음엔 네이버 검색 API(뉴스)를 썼으나, 2026-07-30부터 개발자센터 신규 신청이 막히고(NAVER API HUB로 이관) 2026-09-07 시행 약관이 검색 결과를 AI에 입력하는 것을 금지해 언론사 RSS로 바꿨다.
- 법적 확인 필요: 신문사들이 공동으로 게시한 「디지털 뉴스콘텐츠 이용규칙」은 RSS를 개인 열람용으로 보고, 무단 재배포·봇 대량 수집·영리 목적 링크 모음을 문제 삼는다. 이 서비스는 기사 본문을 저장·게시하지 않고 자체 작성 설명과 원문 링크만 쓰지만, 운영 전에 언론사별 조건을 확인하거나 이용 허락을 받는다.

## 데이터와 저장소

- 주요 데이터 저장소: MySQL 8
- 캐시, 검색엔진, 오브젝트 스토리지: 해당 없음 (MVP)
- 데이터 일관성 요구: 투표·의견·추천·신고의 "1회" 제약은 DB 유니크 제약으로 최종 보장한다.

## 비동기 처리

- 해당 없음 (MVP)

## 인증과 권한

- 구현: Spring Security OAuth2 Resource Server(JWT 검증) + `NimbusJwtEncoder`(발급). 코드: `src/main/java/com/ilililissue/auth/`
- 액세스 토큰: JWT HS256, `iss=ilililissue`, `sub=회원 id`, `role=USER|ADMIN`, 수명 30분. 웹은 `access_token` 쿠키, 앱은 `Authorization: Bearer` 헤더.
- 앱 모드: 로그인·재발급 요청에 `X-Client: app` 헤더가 있으면 토큰을 응답 본문(`accessToken`, `refreshToken`, 만료 초)으로 주고 쿠키를 쓰지 않는다(`Cache-Control: no-store`). 재발급·로그아웃은 본문 `{refreshToken}`을 받는다. 코드: `auth/adapter/in/web/AuthController.java`
- 리프레시 토큰: 32바이트 난수(불투명 토큰), 수명 14일. 서버(`refresh_token` 테이블)에는 SHA-256 해시만 저장. 재발급 시 기존 토큰을 삭제하고 새로 발급(rotation)하며, 삭제에 성공한 요청만 재발급한다. 쿠키 경로는 `/api/auth`.
- 쿠키: `HttpOnly`, `SameSite=Lax`, `Secure`(운영 true, local false). 쿠키 Max-Age는 토큰 수명과 같다.
- `/api/auth/**`는 액세스 토큰을 읽지 않는다 (만료 쿠키가 남아도 로그인·재발급·로그아웃 가능).
- CSRF: CSRF 토큰은 쓰지 않는다. `SameSite=Lax` 쿠키 + CORS 허용 Origin(`auth.allowed-origins`) + 상태 변경 API의 JSON 본문으로 대응한다. 웹 클라이언트를 API와 다른 사이트(registrable domain)에 배포하면 이 결정을 재검토한다.
- 로그인 실패: 이메일 없음과 비밀번호 불일치를 같은 401 응답·비슷한 처리 시간으로 응답한다.
- 인가 모델: RBAC (`USER`, `ADMIN`). 비회원은 `GET /api/issues/**`, `GET /api/categories/**`만 허용. `/api/admin/**`은 `ADMIN`.
- 관리자 계정: 가입 API는 항상 `USER`로 만든다. 관리자는 DB에서 직접 지정한다 (`tools.md` 참고).
- 보안 고위험 영역: 비밀번호 저장(`DelegatingPasswordEncoder`, bcrypt), `JWT_SECRET`, 개인정보(이메일)

## 운영 요구

- 로깅: 참여 이벤트 로그(지표 측정)와 애플리케이션 로그 분리
- 모니터링, 백업, 장애 대응, 배포와 롤백: 미정
- 마이그레이션: 미정 (Flyway 도입 권장)

## 비밀값 이름

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`(docker), `JWT_SECRET`(32바이트 이상), `ALLOWED_ORIGINS`, 선택: `NEWS_DRAFT_ENABLED`, `NEWS_DRAFT_MODEL`, `OPENAI_API_KEY` — 실제 값은 저장소에 두지 않는다. 예시: 루트 `.env.example`

## 변경 가능성

- 나중에 바꿔도 되는 결정: 배포 인프라, 클라이언트 프레임워크
- 지금 고정해야 하는 결정: REST API 경계, JWT 쿠키 인증, 도메인 유니크 제약
- 바꾸기 어려운 결정과 이유: 투표·의견의 "회원당 1회" 데이터 구조 — 운영 데이터가 쌓이면 변경 비용이 크다.

## 결정되지 않은 질문

- 배포 인프라와 도메인
