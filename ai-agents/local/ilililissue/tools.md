# 프로젝트 도구 정책

이 문서는 ilililissue에서 사용할 수 있는 도구, 주의할 도구, 승인 필요한 명령을 정의합니다.

## 사용할 수 있는 도구

- 파일 검색: `Glob`, `Grep`
- 파일 확인: `Read`
- 코드 변경: `Edit`, `Write`
- 검증: Gradle wrapper

## 프로젝트 명령

기본 `JAVA_HOME`이 JDK 8이면 Gradle이 실패한다. Gradle toolchain이 JDK 17을 찾지만, Gradle 자체 실행에도 JDK 17 이상이 필요하므로 아래처럼 지정한다.

| 목적 | 명령 | 비고 |
| --- | --- | --- |
| 로컬 DB | `docker compose up -d` | MySQL 8 |
| 실행 | `JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew bootRun` | 로컬 DB 필요 |
| 실행 (H2) | `JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew bootTestRun` | MySQL 불필요, 예시 데이터 생성 |
| 웹 실행 | `cd web && npm run dev` | http://localhost:3000, `/api/*`는 8080으로 전달 |
| 웹 검증 | `cd web && npm run lint && npm run build` | |
| 테스트 | `JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew test` | H2 사용, DB 불필요 |
| 빌드 | `JAVA_HOME=~/.jdks/corretto-17.0.13 ./gradlew build` |  |
| 린트 | 없음 |  |

## 관리자 계정 지정

가입 API는 항상 `USER`로 만든다. 관리자는 가입 후 DB에서 직접 지정한다.

```sql
UPDATE member SET role = 'ADMIN' WHERE email = '<관리자 이메일>';
```

## 승인 필요한 작업

- 네트워크 설치: Gradle 의존성 추가는 허용, 새 도구 설치는 확인
- 외부 서비스 호출: 확인 필요
- 데이터 삭제 또는 마이그레이션: `db/` 볼륨 삭제, 운영 DB 마이그레이션은 확인 필요

## 금지된 작업

- 비밀값 출력 또는 저장 (`JWT_SECRET`, DB 비밀번호)
- 사용자 승인 없는 파괴적 명령
- 관련 없는 파일 포맷팅

## 결정되지 않은 질문

- 정적 분석/포맷터 도입 여부
