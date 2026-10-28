# 앱(Expo) 클라이언트 주의 사항

앱을 만들기 전에 이 문서를 먼저 읽는다. 백엔드 API는 웹과 같고(`README.md`의 API 표), 아래는 웹에서는 드러나지 않거나 웹이 대신 처리하던 부분이다. 앱 작업을 시작할 때 각 항목을 확인하고, 처리한 내용은 이 문서에 반영한다.

## 1. 인증: 앱 모드 (`X-Client: app`)

- 로그인·재발급·로그아웃 요청에 `X-Client: app` 헤더를 붙인다. 붙이지 않으면 웹처럼 쿠키로 동작한다.
- 로그인 `POST /api/auth/login {email, password}` → `{member, accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn}` (만료는 초). 쿠키는 주지 않는다.
- 두 토큰은 `expo-secure-store`에 저장한다. AsyncStorage 같은 평문 저장소에 두지 않는다.
- API 요청은 `Authorization: Bearer <accessToken>`으로 인증한다.
- 재발급 `POST /api/auth/refresh {refreshToken}` → 로그인과 같은 형식. 받은 두 토큰으로 **모두** 바꿔 저장한다(리프레시 토큰도 새로 나온다).
- 로그아웃 `POST /api/auth/logout {refreshToken}` → 204. 서버에서 리프레시 토큰을 폐기하므로 저장한 토큰도 지운다.
- 탈퇴 `POST /api/members/me/withdrawal {password}`는 Bearer 헤더로 호출한다. 성공하면 저장한 토큰을 지운다(모든 기기의 리프레시 토큰이 폐기된다).
- 코드: `auth/adapter/in/web/AuthController.java`, 테스트: `AuthApiTest.appAuthFlow`

## 2. 토큰 재발급

- 액세스 토큰 30분, 리프레시 토큰 14일. 401을 받으면 재발급을 **한 번만** 하고 원래 요청을 다시 보낸다(`web/src/lib/api.ts`와 같은 방식).
- 리프레시 토큰은 1회용이다. 여러 요청이 동시에 401을 받아도 재발급 요청은 하나만 보내고 나머지는 그 결과를 기다리게 한다. 동시에 두 번 보내면 하나는 실패해 로그아웃된다.
- 재발급 응답을 저장하기 전에 앱이 종료되면 이전 리프레시 토큰은 이미 폐기된 상태라 다시 로그인해야 한다. 응답을 받으면 바로 저장한다.
- 재발급이 실패하면 저장한 토큰을 지우고 로그인 화면으로 보낸다.
- 탈퇴한 회원의 토큰은 만료 전이라도 401이다.

## 3. 조회 기록 (지표)

- 앱을 처음 실행할 때 UUID를 하나 만들어 저장하고(방문자 ID, 개인정보 아님), 이슈 상세 화면을 열 때마다 `POST /api/issues/{id}/views`를 `X-Visitor-Id: <UUID>` 헤더와 함께 **한 번** 호출한다. 로그인 상태면 Bearer 헤더도 같이 보낸다.
- 헤더가 있으면 서버는 쿠키를 발급하지 않는다. 웹은 `ViewTracker` 컴포넌트가 쿠키(`visitor_id`)로 같은 일을 한다.
- 이 호출을 빼먹으면 투표율(투표 수 / 이슈별 고유 방문자 수)이 실제보다 높게, 심하면 100%를 넘게 나온다.
- `GET /api/issues/{id}`는 지표에 기록하지 않는다.

## 4. 참여 규칙 (응답 코드로 화면을 나눈다)

- 비회원은 이슈 목록·상세만 볼 수 있다. 투표·결과·의견·추천·신고는 로그인이 필요하다(401).
- 결과(`/results`)와 의견 목록(`/opinions`)은 **투표한 회원만** 볼 수 있다. 투표 전에는 403이므로 호출하지 말고 `GET /api/issues/{id}/participation`의 `voted`로 화면을 나눈다.
- 투표는 1회이며 바꿀 수 없다. 투표 전에 이 점을 안내한다.
- 의견은 이슈당 1개이고 1회 수정할 수 있다. 삭제하면 그 이슈에는 다시 쓸 수 없다. 삭제 전에 확인 창을 띄운다. 삭제한 의견은 `participation.opinion.deleted = true`이고 `content`는 null이다.
- 추천은 버튼을 다시 누르면 취소한다(`DELETE /api/opinions/{id}/recommendations`). 내 의견(`mine = true`)에는 추천 버튼을 보여주지 않는다(서버는 403).
- 의견 목록은 내 입장과 **다른 입장의 탭을 먼저** 연다. "반대 의견 열람률" 지표와 서비스 취지(왜 다르게 생각하는지 보기)에 맞춘 것이다. 탭 첫 페이지를 열 때 서버가 열람 이벤트를 기록한다.
- 마감된 이슈(`status = CLOSED`)는 투표할 수 없지만 투표한 회원은 결과를 볼 수 있다.
- 오류 응답은 `{code, message}`이며 `message`는 사용자에게 그대로 보여줄 수 있는 한국어 문장이다.

## 5. 저작권 (기획서 11장)

- 기사 본문·사진을 보여주지 않는다. 자체 작성한 배경 설명과 원문 링크만 보여준다.
- 원문 링크는 앱 안에 기사를 다시 그리지 말고 외부 브라우저(또는 `expo-web-browser`)로 연다.

## 6. 앱 스토어 심사 대비

- **회원 탈퇴를 앱 안에서 할 수 있어야 한다** (Apple 심사 가이드라인 5.1.1(v)). `POST /api/members/me/withdrawal {password}`를 쓰는 화면을 만든다. 탈퇴 안내 문구는 웹 `/me` 화면(`web/src/components/account.tsx`)과 맞춘다.
- 사용자 생성 콘텐츠(의견)가 있으므로 Apple 가이드라인 1.2의 신고·차단이 필요하다. 둘 다 구현되어 있으니 앱 화면에 넣는다.
  - 신고: 의견 카드에서 `POST /api/opinions/{id}/reports {reason}`
  - 차단: 의견 카드에서 `POST /api/opinions/{id}/author-block` → 성공하면 그 카드를 목록에서 바로 뺀다(작성자는 이슈당 의견 1개). 확인 창에 "상대에게 알리지 않고, 내 정보에서 해제할 수 있다"고 안내한다
  - 차단 목록·해제: 내 정보 화면에서 `GET /api/members/me/blocks`, `DELETE /api/members/me/blocks/{blockId}`. 웹 `web/src/components/blocked-members.tsx`와 같은 구성
- 1.2는 부적절한 콘텐츠를 금지하는 **이용약관 동의**와 개발자 연락처도 요구한다. **이용약관은 아직 없다.** 가입 화면에 약관 동의를 넣어야 한다.
- 개인정보 처리방침 URL은 `https://<도메인>/privacy`(웹 페이지)를 스토어에 등록한다. **초안이라 대괄호 항목(운영자, 보호책임자, 연락처, 클라우드 사업자, 이용 기록 보관 기간, 시행일)을 채우고 법률 검토를 받아야 한다.** 앱에서는 가입 화면과 내 정보 화면에서 이 URL을 연다.
- 앱은 쿠키 대신 `X-Visitor-Id`를 쓰므로, 처리방침 1장의 "앱 설치 시 만든 임의의 값" 문구가 이를 다룬다. 다른 식별자(광고 ID, 푸시 토큰 등)를 추가하면 처리방침과 스토어의 개인정보 항목(App Privacy, 데이터 보안 섹션)을 함께 고친다.

## 7. 그 밖의 형식

- 날짜·시각은 시간대 없는 문자열(`2026-10-28T09:00:00`)이며 서버 기준 Asia/Seoul이다. 기기 시간대로 변환하지 말고 한국 시간으로 표시한다.
- 목록 응답은 `{content, page, size, totalElements}`이고 page는 0부터 시작한다.
- 운영에서는 웹과 같은 도메인의 `/api/*`를 호출한다(`deploy/README.md`). 네이티브 앱은 `Origin` 헤더를 보내지 않으므로 CORS 설정이 필요 없다.
- 웹과 타입을 공유하려면 `web/src/lib/types.ts`를 공용 패키지로 옮기는 것을 검토한다(코드 위치는 `context-map.md` 미정 질문).
