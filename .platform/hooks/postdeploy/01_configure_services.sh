#!/bin/bash
set -e

# 호스트의 Nginx 비활성화
systemctl stop nginx || true
systemctl disable nginx || true

# Docker Compose 실행
cd /var/app/current

# Docker Compose가 실행 중인지 확인
if [ ! -f "/opt/.docker_compose_started" ] || ! docker-compose ps | grep -q "nginx.*Up"; then
  echo "[INFO] Docker Compose 서비스 시작..."
  docker-compose down || true
  docker-compose up -d
  touch /opt/.docker_compose_started
else
  echo "[INFO] Docker Compose 서비스가 이미 실행 중입니다."
fi

# fail2ban 설정 - 조건부 적용으로 개선
if [ ! -f "/etc/fail2ban/.sobok_configured" ]; then
  echo "[INFO] fail2ban 설정 적용 중..."
  cp .platform/files/jail.local /etc/fail2ban/jail.local
  cp .platform/files/filter.d/*.conf /etc/fail2ban/filter.d/
  touch /etc/fail2ban/.sobok_configured
  systemctl restart fail2ban
  echo "[INFO] fail2ban 설정 완료"
else
  echo "[INFO] fail2ban 설정이 이미 적용되어 있습니다."
fi