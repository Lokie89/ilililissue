# 1일 1이슈 (ilililissue)

매일 선정한 사회·경제·생활 이슈에 찬성·중립·반대로 투표하고, 서로 다른 입장의 의견을 비교하는 서비스의 API 서버입니다.

> '누가 이겼는지 보여주는 서비스'가 아니라 '사람들이 왜 다르게 생각하는지 보여주는 서비스'.

- 기획·도메인 정책: [`ai-agents/local/ilililissue/`](./ai-agents/local/ilililissue/) (`goal.md`, `domain-policy.md`, `architecture.md`)
- 에이전트 작업 기준: [`CLAUDE.md`](./CLAUDE.md), [`AGENTS.md`](./AGENTS.md)

## 기술 스택

Java 17, Spring Boot 4.1, Spring Security(JWT, OAuth2 Resource Server), Spring Data JPA, MySQL 8, Gradle 9. 헥사고날 구조의 모듈러 모놀리스(`member`, `auth`, `issue`, `vote`, `opinion`, `report`, `metrics`)입니다.

## 실행

JDK 17 이상이 필요합니다. 기본 `JAVA_HOME`이 JDK 8이면 아래처럼 지정합니다.

```sh
cp .env.example .env            # 값 채우기 (DB_ROOT_PASSWORD, DB_PASSWORD, JWT_SECRET)
docker compose up -d            # MySQL 8

set -a; . ./.env; set +a        # 환경변수 로드 (bash)
JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew bootRun
```

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
- 액세스 토큰이 만료되면 `POST /api/auth/refresh`로 재발급합니다. 리프레시 토큰은 1회용(rotation)입니다.
- 오류 응답 형식: `{"code": "UNAUTHORIZED", "message": "로그인이 필요합니다."}`

## API

| 메서드 | 경로 | 권한 | 설명 |
| --- | --- | --- | --- |
| POST | `/api/members` | 누구나 | 회원가입 `{email, nickname, password}` |
| GET | `/api/members/me` | 회원 | 내 정보 |
| POST | `/api/auth/login` | 누구나 | 로그인 `{email, password}` |
| POST | `/api/auth/refresh` | 리프레시 쿠키 | 토큰 재발급 |
| POST | `/api/auth/logout` | 누구나 | 로그아웃 (쿠키 삭제, 리프레시 토큰 폐기) |
| GET | `/api/categories` | 누구나 | 카테고리 목록 |
| GET | `/api/issues?page&size` | 누구나 | 공개 이슈 목록 (게시·마감, 최신순) |
| GET | `/api/issues/{id}` | 누구나 | 이슈 상세 (질문, 배경 설명, 원문 출처) |
| GET | `/api/issues/{id}/participation` | 누구나 | 내 투표 입장·의견 (비회원은 `voted: false`) |
| POST | `/api/issues/{id}/votes` | 회원 | 투표 `{stance: AGREE \| NEUTRAL \| DISAGREE}` (1회, 변경 불가) |
| GET | `/api/issues/{id}/results` | 투표자 | 입장별 투표 수·비율 |
| GET | `/api/issues/{id}/opinions?stance&sort&page&size` | 투표자 | 입장별 의견 (`sort`: `RECOMMENDED`(기본) \| `LATEST`) |
| POST | `/api/issues/{id}/opinions` | 투표자 | 의견 작성 `{content}` (1개, 500자) |
| PUT | `/api/opinions/{id}` | 작성자 | 의견 수정 `{content}` (1회) |
| POST | `/api/opinions/{id}/recommendations` | 투표자 | 의견 추천 (1회) |
| POST | `/api/opinions/{id}/reports` | 회원 | 의견 신고 `{reason}` (1회) |
| POST | `/api/admin/categories` | 관리자 | 카테고리 생성 `{name}` |
| GET | `/api/admin/issues` | 관리자 | 전체 이슈 목록 (모든 상태) |
| GET | `/api/admin/issues/{id}` | 관리자 | 이슈 상세 (모든 상태) |
| POST | `/api/admin/issues` | 관리자 | 이슈 작성 `{title, summary, categoryId, sources[{url, publisher, publishedOn}]}` |
| PUT | `/api/admin/issues/{id}` | 관리자 | 이슈 수정 (작성·검수 상태만, 수정하면 다시 작성 상태) |
| POST | `/api/admin/issues/{id}/review` | 관리자 | 검수 완료 |
| POST | `/api/admin/issues/{id}/publish` | 관리자 | 게시 (출처 1개 이상) |
| POST | `/api/admin/issues/{id}/close` | 관리자 | 투표 마감 |
| POST | `/api/admin/issues/{id}/correction` | 관리자 | 게시 후 배경 설명 정정 `{summary}` |
| GET | `/api/admin/reports?status&page&size` | 관리자 | 신고 목록 (기본 `RECEIVED`) |
| POST | `/api/admin/reports/{id}/hide` | 관리자 | 신고된 의견 숨김 |
| POST | `/api/admin/reports/{id}/dismiss` | 관리자 | 신고 기각 |
| POST | `/api/admin/opinions/{id}/hide` | 관리자 | 의견 직접 숨김 |
| GET | `/api/admin/metrics?from&to` | 관리자 | 참여 지표 (날짜 `YYYY-MM-DD`) |

이슈 상태: `DRAFT` → `REVIEWED` → `PUBLISHED` → `CLOSED`. 투표는 `PUBLISHED`에서만 가능합니다.
