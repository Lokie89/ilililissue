# 프로젝트 컨텍스트 지도

이 문서는 ilililissue에서 에이전트가 먼저 확인해야 하는 코드, 문서, 데이터, 설정 위치를 정의합니다. 경로는 저장소 루트(`D:\_Study\ilililissue`) 기준입니다.

## 먼저 확인할 위치

- `README.md`: 실행 방법, 인증 방식, 전체 API 목록
- 실행 진입점: `src/main/java/com/ilililissue/IlililissueApplication.java`
- 주요 소스 파일 (모듈별 `domain` / `application` / `adapter.in.web` / `adapter.out.persistence`):
  - 인증·보안: `src/main/java/com/ilililissue/auth/config/SecurityConfig.java`, `auth/application/service/AuthService.java`
  - 이슈: `issue/domain/entity/Issue.java`, `issue/application/service/IssueService.java`
  - 투표·결과: `vote/application/service/VoteService.java`
  - 의견·추천: `opinion/application/service/OpinionService.java`
  - 신고: `report/application/service/ReportService.java`
  - 참여 지표: `metrics/application/service/MetricsService.java`
  - 공통 예외 처리: `common/adapter/in/web/GlobalExceptionHandler.java`, `common/DomainException.java`
- 테스트: `src/test/java/com/ilililissue/**` (API 통합 테스트 공통: `support/ApiTestSupport.java`)
- 설정 파일: `build.gradle`, `src/main/resources/application.yml`, `src/test/resources/application.yml`, `docker-compose.yml`
- 환경변수 예시: `.env.example`

## 웹 클라이언트

- `web/README.md`: 실행, 구조, 화면 목록
- `web/AGENTS.md`: Next.js 16.4 문서 확인 규칙 (`web/node_modules/next/dist/docs/`)
- API 클라이언트: `web/src/lib/api.ts`, 타입: `web/src/lib/types.ts`, 서버 조회: `web/src/lib/server-api.ts`
- 이슈 참여 화면: `web/src/components/participation.tsx`, 관리 화면: `web/src/components/admin/`

## 앱(Expo) 클라이언트

- `app-client-guide.md`: 앱 착수 전 필독. 앱 모드 인증(`X-Client: app`), 조회 기록(`X-Visitor-Id`), 참여 규칙, 스토어 심사(탈퇴·차단) 주의 사항

## 도메인 문서

- `domain-policy.md`: 확정 결정, 개념별 정책, 미정 질문
- `docs/planning-2026-10-28.md`: 기획 원문
- `src/docs/D-Note.MD`: 초기 요구사항 메모 (2023). 기획 원문과 충돌하면 기획 원문과 `domain-policy.md`를 따른다.

## 아키텍처 문서

- `architecture.md`

## 분석 문서

- `analysis/2026-10-28-codebase-analysis.md`: 기존 코드 결함과 기획 대비 갭

## 검증 문서

- `validation.md`

## 주의할 위치

- 비밀값 또는 로컬 전용 파일: `db/` (Docker MySQL 볼륨), `.env` (생성 시)
- 생성물 또는 빌드 산출물: `build/`, `.gradle/`
- 수정 전 확인이 필요한 파일: `docker-compose.yml` (로컬 DB 데이터 볼륨)

## 결정되지 않은 질문

- 앱(Expo) 코드 위치
