#!/bin/bash
set -e

# 기본 에러 처리 함수
handle_error() {
  echo "[ERROR] 스크립트 실행 중 오류 발생: $1"
  exit 1
}

# 호스트의 Nginx 비활성화
echo "[INFO] 호스트 Nginx 비활성화 중..."
systemctl stop nginx || echo "[WARN] Nginx 중지 실패 (무시됨)"
systemctl disable nginx || echo "[WARN] Nginx 비활성화 실패 (무시됨)"

# Docker Compose 실행
cd /var/app/current || handle_error "경로를 찾을 수 없음: /var/app/current"

# Docker Compose 파일 존재 확인
if [ ! -f "docker-compose.yml" ]; then
  handle_error "docker-compose.yml 파일이 없습니다"
fi

# 타임아웃 설정 (5분)
TIMEOUT=300
START_TIME=$(date +%s)

echo "[INFO] Docker Compose 서비스 시작 시도..."

# 실행 중인 Docker Compose 서비스 중지
docker-compose down --timeout 30 || echo "[WARN] 기존 서비스 중지 실패 (무시됨)"

# 백그라운드에서 Docker Compose 실행
docker-compose up -d &
DOCKER_PID=$!

# 프로세스 종료를 위한 트랩 설정
trap 'kill $DOCKER_PID 2>/dev/null || true' EXIT

# 타임아웃 처리 로직
STARTED=false
while kill -0 $DOCKER_PID 2>/dev/null; do
    CURRENT_TIME=$(date +%s)
    ELAPSED_TIME=$((CURRENT_TIME - START_TIME))

    if [ $ELAPSED_TIME -gt $TIMEOUT ]; then
        echo "[ERROR] Docker Compose 시작 타임아웃 (${TIMEOUT}초). 강제 종료합니다."
        kill $DOCKER_PID 2>/dev/null || true
        docker-compose logs --tail=100
        handle_error "Docker Compose 시작 타임아웃"
    fi

    # 1초 간격으로 상태 확인
    sleep 1

    # nginx 컨테이너가 실행 중인지 확인
    if docker-compose ps | grep -q "nginx.*Up"; then
        echo "[INFO] nginx 서비스가 성공적으로 시작되었습니다."
        touch /opt/.docker_compose_started
        STARTED=true
        break
    fi
done

# 트랩 제거
trap - EXIT

# Docker Compose 프로세스가 계속 실행중이면 종료
if kill -0 $DOCKER_PID 2>/dev/null; then
    kill $DOCKER_PID 2>/dev/null || true
fi

# 서비스 상태 확인
echo "[INFO] Docker Compose 서비스 상태:"
docker-compose ps

# 서비스가 실행 중인지 확인
if ! $STARTED || ! docker-compose ps | grep -q "Up"; then
    echo "[ERROR] 서비스 시작 실패. Docker Compose 로그 출력:"
    docker-compose logs --tail=100
    handle_error "Docker Compose 서비스 시작 실패"
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