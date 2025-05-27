#!/bin/bash
set -e

# 호스트의 Nginx 비활성화
systemctl stop nginx || true
systemctl disable nginx || true

# Docker Compose 실행
cd /var/app/current

# 타임아웃 설정 (5분)
TIMEOUT=300
START_TIME=$(date +%s)

echo "[INFO] Docker Compose 서비스 시작 시도..."

# 실행 중인 Docker Compose 서비스 중지
docker-compose down || true

# 백그라운드에서 Docker Compose 실행
docker-compose up -d &
DOCKER_PID=$!

# 타임아웃 처리 로직
while kill -0 $DOCKER_PID 2>/dev/null; do
    CURRENT_TIME=$(date +%s)
    ELAPSED_TIME=$((CURRENT_TIME - START_TIME))

    if [ $ELAPSED_TIME -gt $TIMEOUT ]; then
        echo "[ERROR] Docker Compose 시작 타임아웃 (${TIMEOUT}초). 강제 종료합니다."
        kill $DOCKER_PID 2>/dev/null || true
        break
    fi

    # 1초 간격으로 상태 확인
    sleep 1

    # nginx 컨테이너가 실행 중인지 확인
    if docker-compose ps | grep -q "nginx.*Up"; then
        echo "[INFO] nginx 서비스가 성공적으로 시작되었습니다."
        touch /opt/.docker_compose_started
        break
    fi
done

# 서비스 상태 확인
echo "[INFO] Docker Compose 서비스 상태:"
docker-compose ps

# 서비스가 실행 중이지 않으면 로그 출력
if ! docker-compose ps | grep -q "Up"; then
    echo "[ERROR] 서비스 시작 실패. Docker Compose 로그 출력:"
    docker-compose logs
    exit 1
fi

echo "[INFO] Docker Compose 서비스 시작 완료"

## fail2ban 설정 - 조건부 적용으로 개선
#if [ ! -f "/etc/fail2ban/.sobok_configured" ]; then
#  echo "[INFO] fail2ban 설정 적용 중..."
#  cp .platform/files/jail.local /etc/fail2ban/jail.local
#  cp .platform/files/filter.d/*.conf /etc/fail2ban/filter.d/
#  touch /etc/fail2ban/.sobok_configured
#  systemctl restart fail2ban
#  echo "[INFO] fail2ban 설정 완료"
#else
#  echo "[INFO] fail2ban 설정이 이미 적용되어 있습니다."
#fi