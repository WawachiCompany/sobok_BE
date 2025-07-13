#!/bin/bash
set -e

REQUIRED_VERSION="v1.1"
CRON_JOB="0 3 * * 1 cd /var/app/current && docker-compose run --rm certbot certbot renew --webroot -w /var/www/certbot --post-hook \"docker-compose exec nginx nginx -s reload\""
CRON_MARKER="/opt/.sobok_certbot_cron_configured"

# 크론 작업이 이미 설정되어 있는지 확인 (버전까지 체크)
if [ ! -f "$CRON_MARKER" ] || [ "$(cat $CRON_MARKER 2>/dev/null)" != "$REQUIRED_VERSION" ]; then
  echo "[INFO] certbot 자동 갱신 크론 작업 설정 중..."

  # 기존 크론 작업 가져오기
  CURRENT_CRONTAB=$(crontab -l 2>/dev/null || echo "")

  # 크론 작업이 이미 있는지 확인
  if echo "$CURRENT_CRONTAB" | grep -q "docker-compose run --rm certbot"; then
    echo "[INFO] certbot 갱신 크론 작업이 이미 존재합니다."
  else
    # 새 크론 작업 추가
    echo "$CURRENT_CRONTAB" > /tmp/current_cron
    echo "$CRON_JOB" >> /tmp/current_cron
    crontab /tmp/current_cron
    rm /tmp/current_cron

    echo "[INFO] certbot 갱신 크론 작업이 성공적으로 추가되었습니다."
  fi

  # 마커 파일에 버전 기록
  echo "$REQUIRED_VERSION" > $CRON_MARKER
else
  echo "[INFO] certbot 갱신 크론 작업이 이미 설정되어 있습니다."
fi