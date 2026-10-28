# 프로젝트 컨텍스트 지도

이 문서는 ilililissue에서 에이전트가 먼저 확인해야 하는 코드, 문서, 데이터, 설정 위치를 정의합니다. 경로는 저장소 루트(`D:\_Study\ilililissue`) 기준입니다.

## 먼저 확인할 위치

- `README.md`: 없음 (Phase 0 이후 작성 예정)
- 실행 진입점: `src/main/java/com/ilililissue/IlililissueApplication.java`
- 주요 소스 파일:
  - `src/main/java/com/ilililissue/issue/domain/entity/Issue.java`
  - `src/main/java/com/ilililissue/comment/domain/entity/Comment.java`
  - `src/main/java/com/ilililissue/common/SelfValidating.java`
  - `src/main/java/com/ilililissue/issue/adapter/out/persistence/IssuePersistenceAdapter.java`
- 테스트: `src/test/java/com/ilililissue/**`
- 설정 파일: `build.gradle`, `src/main/resources/application.yml`, `docker-compose.yml`
- 환경변수 예시: 없음 (`architecture.md`의 비밀값 이름 참고)

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

- 클라이언트(웹/앱) 코드 위치
