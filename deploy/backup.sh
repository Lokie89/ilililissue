#!/bin/sh
# MySQL 백업: 저장소 루트에서 실행한다. 예) crontab: 0 4 * * * cd /srv/ilililissue && sh deploy/backup.sh
# 14일이 지난 백업은 지운다.
set -eu
mkdir -p backups
file="backups/ilililissue-$(date +%Y%m%d-%H%M%S).sql.gz"
docker compose -f docker-compose.prod.yml --env-file .env.prod exec -T db \
  sh -c 'exec mysqldump --single-transaction --routines -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" ilililissue' | gzip > "$file"
find backups -name 'ilililissue-*.sql.gz' -mtime +14 -delete
echo "backup: $file"
