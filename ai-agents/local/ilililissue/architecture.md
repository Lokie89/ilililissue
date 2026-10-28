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
- 프레임워크: Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security
- 런타임 버전: JDK 17
- 패키지 관리자: Gradle (wrapper)
- 주요 라이브러리: Lombok, Hibernate Validator, JWT 라이브러리(미정), H2(테스트)

## 클라이언트 선택 (2026-10-28)

- 백엔드는 화면을 렌더링하지 않는 JSON REST API로만 만든다. 웹과 앱이 같은 API를 쓴다.
- 웹: Next.js (React, TypeScript). 이슈 상세 페이지가 검색·공유 링크로 외부 유입되어야 하므로 서버 렌더링이 가능한 쪽을 택한다.
- 앱: React Native (Expo). 웹과 언어·타입·API 클라이언트를 공유한다.
- 순서: API → 웹 → 앱. 앱은 4주 검증 이후.

## 애플리케이션 구조

- 구조 유형: 모듈러 모놀리스, 헥사고날 아키텍처
- 주요 모듈: `member`, `issue`, `vote`, `opinion`(기존 `comment`), `report`, `common`
- 모듈 내부 패키지: `domain` / `application`(port.in, port.out, service) / `adapter.in.web` / `adapter.out.persistence`
- 도메인 모델과 JPA 엔티티는 분리한다. 모듈 간 참조는 id 값으로 한다.

## 통신 방식

- 내부 통신: 메서드 호출 (모놀리스)
- 외부 API 방식: REST (JSON)
- 이벤트 기반 통신 여부: 없음

## 외부 연동 인터페이스 문서

- 해당 없음 (MVP에서는 외부 연동 없음. 뉴스 API·RSS 수집은 MVP 이후)

## 데이터와 저장소

- 주요 데이터 저장소: MySQL 8
- 캐시, 검색엔진, 오브젝트 스토리지: 해당 없음 (MVP)
- 데이터 일관성 요구: 투표·의견·추천·신고의 "1회" 제약은 DB 유니크 제약으로 최종 보장한다.

## 비동기 처리

- 해당 없음 (MVP)

## 인증과 권한

- 인증 방식: Spring Security + JWT. 웹은 토큰을 `HttpOnly`, `Secure`, `SameSite` 쿠키로 주고받고 서버가 쿠키의 JWT를 검증한다. 앱은 `Authorization: Bearer` 헤더를 함께 지원한다(앱 개발 시점).
- 토큰: 짧은 수명의 access token + refresh token. 로그아웃 시 쿠키 삭제와 refresh token 무효화(서버 저장).
- CSRF: 쿠키 기반 인증이므로 `SameSite` 설정과 CSRF 대응을 함께 적용한다 (구체 방식 미정).
- 인가 모델: RBAC (`USER`, `ADMIN`)
- 보안 고위험 영역: 비밀번호 저장(해시), JWT 서명 키, 개인정보(이메일)

## 운영 요구

- 로깅: 참여 이벤트 로그(지표 측정)와 애플리케이션 로그 분리
- 모니터링, 백업, 장애 대응, 배포와 롤백: 미정
- 마이그레이션: 미정 (Flyway 도입 권장)

## 비밀값 이름

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` — 실제 값은 저장소에 두지 않는다.

## 변경 가능성

- 나중에 바꿔도 되는 결정: 배포 인프라, 클라이언트 프레임워크
- 지금 고정해야 하는 결정: REST API 경계, JWT 쿠키 인증, 도메인 유니크 제약
- 바꾸기 어려운 결정과 이유: 투표·의견의 "회원당 1회" 데이터 구조 — 운영 데이터가 쌓이면 변경 비용이 크다.

## 결정되지 않은 질문

- 배포 인프라와 도메인
- 클라이언트 저장소 구성 (모노레포 여부)
- CSRF 대응 방식 상세
