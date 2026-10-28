# ilililissue 코드베이스 분석 (2026-10-28)

## 분석 범위

- 대상: `build.gradle`, `docker-compose.yml`, `src/main/resources/application.yml`, `src/docs/D-Note.MD`, `src/main/java/**`, `src/test/java/**`
- 비교 기준: 2026-10-28 기획 정리(1일 1이슈, B 방식: 찬성/중립/반대 탭, 대댓글 없음, 의견 1개·수정 1회)
- 실행한 검증: `JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew test`

## 현재 상태 요약

| 항목 | 상태 |
| --- | --- |
| 스택 | Spring Boot 3.1.0, Java 17, Gradle 7.5.1, JPA, Lombok, Hibernate Validator, 테스트용 H2 |
| 아키텍처 의도 | 헥사고날 (도메인 / adapter.out.persistence) — application 계층(port, use case)과 web adapter는 없음 |
| 구현된 것 | `Issue`, `Comment` 도메인 엔티티와 VO, `SelfValidating`, Issue/Comment JPA 엔티티, `IssuePersistenceAdapter.loadIssues()` |
| 없는 것 | web starter, Controller, Security/인증, 회원 영속화, 투표, 신고, DB 드라이버(MySQL), datasource 설정(`application.yml` 비어 있음) |
| 빌드 | 기본 `JAVA_HOME`이 JDK 8이라 Gradle 플러그인 해석 실패. JDK 17로는 main 컴파일 성공, **test 컴파일 실패** |

## 발견한 결함

### 빌드·테스트

1. `IssueTest`가 `User.of(UserRole)`을 호출하지만 실제 시그니처는 `User.of(Id, UserRole)` → `compileTestJava` 실패로 전체 테스트가 돌지 않는다.
2. 빌드가 JDK 17을 요구하는데 toolchain 설정이 없어 환경 의존적이다.

### 영속성

3. `IssueJpaEntity`의 `@OneToMany(mappedBy = "issue_id")`는 `IssueImageJpaEntity`에 연관 필드가 없어 유효하지 않다. JPA 컨텍스트가 뜨는 테스트(`IlililissueApplicationTests`, `CommentJpaRepositoryTest`)는 테스트 컴파일이 고쳐져도 기동 실패가 예상된다.
4. `IssueMapper`가 `User.of(new Id(userId), null)`로 role을 null로 매핑 → `Issue.withId()`의 관리자 검증에서 조회한 모든 이슈가 `UnauthorizedException`을 던진다. "관리자만 생성" 규칙이 생성 시점이 아닌 재구성(reconstitution) 시점에도 적용되는 것이 근본 원인이다.
5. `Comment`는 `issueId`를 도메인에서 제거(d83d688)했지만 JPA 엔티티에는 남아 있어 도메인↔DB 매핑 경로가 없다.

### 도메인

6. `IssueImage.splitExtension()`: `lastIndexOf("\\.")`는 정규식이 아니라 문자열 `\.`를 찾으므로 -1 반환 → `StringIndexOutOfBoundsException`. 확장자도 `fullFileName`이 아닌 잘린 `fileName`에서 추출한다.
7. `IssueImage`에 `equals/hashCode`가 없어 `IssueImages`의 `Set` 중복 제거가 동작하지 않는다.
8. 제네릭 오용: `issue.vo.Id extends SelfValidating<comment.vo.Id>`, `IssueImage extends SelfValidating<IssueImages>`.
9. `ImageExtension` 허용 목록 대소문자 혼재(`"HEIC"`만 대문자), 비교 시 정규화 없음.
10. `Comment.updateComment()`는 작성자가 달라도 `AlreadyEditedCommentException`(이미 수정됨)을 던진다 → 권한 오류와 수정 횟수 오류가 구분되지 않는다.
11. 사용자 개념이 3갈래: `comment.vo.User(id, username)`, `issue.vo.User(id, role)`, `member.Member(role)` — 회원 식별자(id)가 `Member`에 없다.

### 설정·운영

12. `docker-compose.yml`에 `MYSQL_ROOT_PASSWORD: 1111` 하드코딩, DB/계정 미지정, `./db/` 볼륨 경로가 `.gitignore`에 없다.
13. Spring Boot 3.1.x는 OSS 지원이 종료된 라인이므로 기능 개발 전에 지원 중인 버전으로 올리는 것이 비용이 적다.

## 기획 대비 갭

| 기획(2026-10-28) | 현재 코드 | 조치 |
| --- | --- | --- |
| 이슈: 제목, 중립적 배경 설명, 기사 출처, 카테고리, 게시일 | `Issue`는 id, images, register만 보유 | `Issue` 재설계. 이미지는 저작권 정책상 MVP 필수 아님 |
| 투표(찬성/중립/반대)와 의견 분리, 투표만 참여 가능 | 투표 개념 없음, D-Note는 "댓글 작성 시 투표 필수" | `Vote` 애그리거트 신설, 의견은 투표를 전제로 작성 |
| 계정당 이슈별 의견 1개, 수정 1회 | `Comment.updated` + `(issueId, userId)` 유니크 제약 | 재사용 가능. 이름을 `Opinion`으로 정리하고 입장(stance) 추가 |
| 입장별 의견 목록 | 조회 기능 없음 | stance 필터 + 페이지네이션 조회 |
| 투표 결과 집계 | 없음 (D-Note에서는 v1.1) | MVP로 앞당김 |
| 신고 + 최소 관리자 콘텐츠 관리 | 없음 (D-Note에서는 보류) | MVP로 앞당김 (신고 접수, 숨김 처리) |
| 좋아요/싫어요 | D-Note v1.1 | MVP 제외 유지 |
| IP 기반 비회원 참여 / 소유권 이전 | 메모 단계 | 채택하지 않음 |
| 참여 지표 측정(투표율, 의견 작성률, 반대 의견 열람률, 재방문율) | 없음 | 최소 이벤트 로그 필요 (특히 탭 열람) |

## 재사용 가능한 부분

- `SelfValidating` 패턴, VO 중심 도메인 모델링, 도메인/JPA 엔티티 분리 방향
- `Comment`의 수정 1회 규칙과 `(issueId, userId)` 유니크 제약
- `AuditedJpaEntity` (패키지를 `common`으로 옮겨 공용화 필요)

## 후속 조치 / 미정 항목

- 로컬 하네스(`goal.md`, `domain-policy.md`, `architecture.md`, `validation.md` 등) 미생성
- 미정: 로그인 없이 투표 허용 여부, 작성자 표시 방식, 투표 전 결과 공개 여부, 투표 변경 허용 여부, 의견 정렬, 프론트엔드 방식, 인증 방식(세션/JWT)
