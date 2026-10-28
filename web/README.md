# 1일 1이슈 웹

Next.js 16.4 (App Router, Cache Components, Tailwind CSS 4) 웹 클라이언트입니다. 백엔드는 저장소 루트의 Spring Boot API 서버입니다.

> 이 버전의 Next.js는 이전 버전과 API가 다릅니다. 코드를 쓰기 전에 [`AGENTS.md`](./AGENTS.md)대로 `node_modules/next/dist/docs/`를 확인하세요.

## 실행

```sh
# 1) 백엔드 (루트에서). MySQL 없이 H2 + 예시 데이터로 띄우려면:
JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew bootTestRun
#    관리자 admin@local.test / 회원 user@local.test (비밀번호 password1234)

# 2) 웹 (web/에서)
npm install
npm run dev        # http://localhost:3000
```

백엔드 주소는 환경변수 `BACKEND_URL`(기본 `http://localhost:8080`)로 바꿉니다.

## 검증

```sh
npm run lint
npm run build
```

## 구조

- 브라우저는 같은 출처의 `/api/*`로 요청하고, `next.config.ts`의 rewrite가 백엔드로 전달합니다. 인증 쿠키(HttpOnly, SameSite=Lax)가 CORS 없이 동작합니다.
- 서버 컴포넌트(이슈 목록·상세)는 백엔드를 직접 호출하며, 요청마다 조회하도록 `<Suspense>` 안에서 읽습니다.
- 로그인 상태, 투표·결과·의견 같은 회원별 화면은 클라이언트 컴포넌트가 `/api/*`를 호출합니다. 401이면 `/api/auth/refresh`로 한 번 재발급 후 재시도합니다 (`src/lib/api.ts`).

| 경로 | 내용 |
| --- | --- |
| `/` | 공개 이슈 목록 |
| `/issues/[id]` | 이슈 질문·배경·원문 링크, 투표 → 결과 → 입장별 의견(다른 입장 탭 먼저), 내 의견 수정·삭제, 추천·추천 취소 |
| `/login`, `/signup` | 로그인, 가입 (`?next=` 로 돌아갈 경로) |
| `/me` | 내 정보, 회원 탈퇴 |
| `/admin` | 관리자: 이슈 작성·검수·게시·마감·정정, 카테고리, 신고 처리, 숨긴 의견 해제, 참여 지표 |
