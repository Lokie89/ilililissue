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

### 2026-10-29 (12) — 디자이너 에이전트 UI/UX 검수와 반영

- 목표: "디자이너 에이전트한테 UI UX 검수" → 결과 중 높음 5·중간 6 반영("응 그렇게 수정해줘")
- 검수: `ai-agents/.claude/agents/ux-ui-designer.md`가 Claude Code 에이전트로 등록돼 있지 않아(루트 `.claude/agents` 없음) 역할 정의를 general-purpose(opus)에 넘겨 읽기 전용으로 검수
- 변경(웹만):
  - 높음: 의견 첫 탭을 반대 입장으로(찬성↔반대, 중립→찬성), 글자용 입장색 `stance-*-ink`(대비 맞춤), 홈을 오늘의 이슈(게시 중)·지난 이슈(마감)로 구분, 상세 순서 변경(쟁점 → 이미지 → …)·모바일 이미지 2:1·투표 전 모바일 하단 고정 "내 입장 고르기"(투표 카드가 보이면 숨김), 비회원에게 입장 버튼(가입으로 연결)과 로그인 링크
  - 중간: 근거 카드 라벨 "찬성 측 근거/반대 측 근거"·제목 "입장별 근거", 의견 쓰기 한 줄로 접기(왜 찬성했나요?), 모바일 탭에 내 입장 "나" 표시, 신고·차단을 "⋯" 메뉴로 묶고 신고 사유는 카드 안 입력, 상세 하단 "다른 분야 오늘의 이슈"(마감 이슈 막다른 화면 해소), 홈 hero 모바일 축소·대표 카드 분야 중복 제거
- 검증: 웹 lint·tsc 통과. Chrome 390px: 홈(오늘 이슈 대표+4행, 가로 스크롤 0), 비회원 상세(하단 버튼 보임 → 투표 카드로 이동 후 숨김, 입장 링크 3), 반대 투표자 상세(첫 탭 찬성, 의견 접힘), 페이지 오류 0. 확인용 테스트 회원·투표는 바로 삭제
- 남은 작업: 검수 낮음 항목(0% 입장 막대 유지, 로그인·가입 label, 정렬 select 모양, 로그아웃 위치, 분야 탭 스크롤 단서), 기능 제안(홈 참여 상태 표시, 투표 직후 반대편 대표 의견 미리 보기, 탭별 의견 수 — 백엔드 필드 필요), `npm run build` 확인
- 주의 사항: 저장소 에이전트 정의를 서브에이전트로 직접 쓰려면 프로젝트 루트 `.claude/agents/`에 있어야 한다

### 2026-10-29 (11) — 핵심 쟁점·입장별 근거로 내용 강조

- 목표: "내용에서 강조할만한거 좀 표현 잘해줘볼래?" 사용자 선택: 핵심 쟁점 + 입장별 근거
- 변경:
  - 도메인 `IssueFocus`(keyPoint 100자, agreeReason·disagreeReason 각 300자, 근거는 둘 다 있거나 둘 다 없음), Flyway `V6__issue_focus.sql`(H2 호환 위해 ALTER 3문장), 작성·수정 요청과 상세 응답에 필드 추가
  - AI 초안 스키마·프롬프트: keyPoint, 사실관계만 담은 summary(150~350자), agreeReason, disagreeReason(각 150자, 길이·어조 맞춤)
  - 웹 상세: 핵심 쟁점(진한 배너) → 배경 → "이렇게 생각해요" 찬성·반대 카드(입장 색) → 원문 기사 → 투표. 관리 화면 작성 폼에 입력칸 3개
  - 로컬 게시 이슈 5건: 기존 배경 설명을 Codex로 쟁점·사실·근거로 나눠(새 사실 추가 금지) DB에 직접 반영(게시 후라 API로 수정 불가)
  - 테스트 설정에 `news-draft.writer: openai-api` 고정
- 검증: `./gradlew test` 108개 통과(IssueFocus 3개, 작성 API 1개 추가), 웹 lint·tsc 통과, Chrome 1280px·390px 상세 화면 확인(가로 스크롤 0, 오류 0)
- 남은 작업: `npm run build` 확인, OpenAI API 방식 이미지, 이전 남은 작업
- 주의 사항:
  - 로컬 `.env`의 `NEWS_DRAFT_WRITER=codex-cli`가 Gradle 테스트로 새어 이미지 API 테스트가 실제 Codex 이미지 생성을 띄웠다(프로세스는 종료). 테스트 설정에서 writer를 고정했다
  - `Get-CimInstance ... -match 'codex'`로 프로세스를 셀 때는 검사 명령 자신도 걸린다

### 2026-10-29 (10) — 이슈 대표 이미지(AI 일러스트)

- 목표: "그림같은것도 넣어주면 안되나? 만들어서"
- 확인: 이 PC의 Codex CLI에 `image_generation` 기능이 켜져 있어 ChatGPT 로그인으로 이미지를 만들 수 있다(시험 1장 95초)
- 변경:
  - 이슈 모듈: `Issue.imageKey`·`replaceImage`, Flyway `V5__issue_image_key.sql`, `IssueImageStore`/`FileIssueImageStore`(`issue.image-dir`), `GET /api/issues/{id}/image`(공개, 7일 캐시), 관리자 `GET`·`DELETE /api/admin/issues/{id}/image`, 응답에 `imageKey`
  - 뉴스 초안 모듈: `IssueImageGeneratorPort`, `CodexCliIssueImageGenerator`(PNG→1200px JPEG), `NoIssueImageGenerator`(OpenAI API 방식), `IssueImageService`(`GenerateIssueImageUseCase`), 초안 등록 뒤 이슈마다 이미지 생성(`NewsDraftRun.imageCount`), `POST /api/admin/issues/{id}/image`(백그라운드)·`/status`
  - 웹: `IssueImage` 컴포넌트, 홈 강조 카드 이미지·목록 썸네일, 상세 이미지와 "AI로 만든 이미지" 안내, 관리 화면 썸네일·"이미지 (다시) 만들기"·"이미지 삭제"
  - 운영: Dockerfile에 `/app/data/issue-images`(app 소유), compose 볼륨 `issue-images`, `ISSUE_IMAGE_DIR`. `.gitignore`에 `/data/`
- 검증: `./gradlew test` 104개 통과(이미지 저장소·API·JPEG 변환·서비스 연동 8개 추가), 웹 lint·tsc 통과. 로컬 V5 적용, 게시 중 5건 이미지 생성(48~79초, 60~220KB) — 실존 인물·글자·사진풍 없음 확인. Chrome 1280px·390px 홈·상세·관리 화면 이미지 표시, 가로 스크롤 0(썸네일 너비 충돌로 생긴 76px 넘침 수정), 오류 0
- 남은 작업: OpenAI API 방식(운영) 이미지 생성(이미지 API 연동), `npm run build` 확인, 이전 남은 작업
- 주의 사항: `IssueImage`는 너비 클래스를 받지 않으면 너비가 없다(쓰는 쪽에서 `w-full`·`w-20` 등 지정). 기본값에 `w-full`을 두면 썸네일 `w-20`과 충돌한다

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

