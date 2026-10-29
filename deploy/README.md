# 배포 (단일 서버)

작게 시작하기 위해 클라우드 VM 한 대에서 모든 구성 요소를 Docker Compose로 띄웁니다.

```
인터넷 ─▶ Caddy (80/443, HTTPS 자동 발급)
            ├─ /api/*  ─▶ api  (Spring Boot, 8080)  ─▶ db (MySQL 8, 외부 비공개)
            └─ 그 외   ─▶ web  (Next.js, 3000)  ── 서버 렌더링 시 api 직접 호출
```

웹과 API가 같은 도메인을 쓰므로 인증 쿠키(HttpOnly, Secure, SameSite=Lax)가 CORS 설정 없이 동작합니다. 앱(Expo)도 같은 도메인의 `/api/*`를 호출하면 됩니다.

## 서버 준비

- 사양: 메모리 2GB 이상 권장. 로컬 시험에서 컨테이너 합계가 약 820MB(db 410MB, api 350MB, web 50MB, caddy 20MB)였습니다.
- OS: Ubuntu 22.04/24.04 등 Docker를 설치할 수 있는 리눅스.
- 방화벽(보안 그룹): 22(SSH), 80, 443만 엽니다. 3306은 열지 않습니다.
- 도메인의 DNS A 레코드를 서버 공인 IP로 지정합니다. 그래야 Caddy가 Let's Encrypt 인증서를 받습니다.

## 처음 배포

```sh
# Docker 설치 (Ubuntu)
curl -fsSL https://get.docker.com | sh

git clone <저장소 주소> /srv/ilililissue
cd /srv/ilililissue
cp .env.prod.example .env.prod      # DOMAIN, DB 비밀번호, JWT_SECRET 채우기 (뉴스 자동 초안을 쓰면 NEWS_DRAFT_*, NAVER_*, ANTHROPIC_API_KEY)
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

- DB 스키마는 api가 시작할 때 Flyway가 만듭니다.
- 상태 확인: `docker compose -f docker-compose.prod.yml --env-file .env.prod ps`, 로그는 `... logs -f api`.

### 관리자 지정

웹에서 가입한 뒤 DB에서 역할을 바꿉니다.

```sh
docker compose -f docker-compose.prod.yml --env-file .env.prod exec db \
  sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" ilililissue -e "UPDATE member SET role = '\''ADMIN'\'' WHERE email = '\''<이메일>'\''"'
```

바뀐 역할은 다시 로그인하면 적용됩니다.

## 업데이트

```sh
cd /srv/ilililissue
git pull
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

새 마이그레이션(`src/main/resources/db/migration`)이 있으면 api가 시작하면서 적용합니다. 배포 전에 백업을 받아 두세요.

## 백업

```sh
sh deploy/backup.sh     # backups/ 에 gzip 덤프 저장, 14일 지난 파일 삭제
```

매일 새벽에 돌리려면 `crontab -e`에 추가합니다.

```
0 4 * * * cd /srv/ilililissue && sh deploy/backup.sh >> backups/backup.log 2>&1
```

백업 파일은 서버 밖(오브젝트 스토리지 등)에도 복사해 두는 것을 권장합니다.

복구 예시:

```sh
gunzip -c backups/<파일>.sql.gz | docker compose -f docker-compose.prod.yml --env-file .env.prod exec -T db \
  sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" ilililissue'
```

## 로컬에서 운영 구성 시험

`.env.prod`에 `DOMAIN=localhost`, `HTTP_PORT=8088`, `HTTPS_PORT=8443`을 넣고 별도 프로젝트 이름으로 띄웁니다. Caddy가 내부 인증서를 쓰므로 브라우저 경고가 뜹니다.

```sh
docker compose -f docker-compose.prod.yml --env-file .env.prod -p ilililissue-prodtest up -d --build
# https://localhost:8443
docker compose -f docker-compose.prod.yml --env-file .env.prod -p ilililissue-prodtest down -v
```

## 규모가 커지면

- DB를 관리형 MySQL(RDS, Cloud SQL 등)로 옮기고 `DB_URL`만 바꿉니다.
- 정적 파일은 CDN에 두고, web·api를 여러 대로 늘릴 때는 로드밸런서를 앞에 둡니다.
