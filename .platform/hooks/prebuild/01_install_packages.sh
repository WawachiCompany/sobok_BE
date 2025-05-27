#!/bin/bash
set -e

# Docker와 Docker Compose만 설치
if [ ! -f "/opt/.sobok_packages_installed" ]; then
  echo "[INFO] 필수 패키지 설치 중..."

  # fail2ban 설치 (필요한 경우)
  yum install -y epel-release fail2ban
  systemctl enable fail2ban

  # docker 서비스 활성화
  systemctl enable docker

  # 마커 파일 생성 및 버전 정보 기록
  echo "v1.0" > /opt/.sobok_packages_installed
  echo "[INFO] 패키지 설치 완료"
else
  echo "[INFO] 패키지가 이미 설치되어 있습니다."
fi