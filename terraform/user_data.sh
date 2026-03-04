#!/bin/bash
# cloud-init: OCI Ubuntu 22.04 인스턴스 초기 세팅
set -euo pipefail
exec > /var/log/user-data.log 2>&1

echo "[INFO] === 초기화 시작 ==="

# ──────────────────────────────────────────────
# 패키지 업데이트
# ──────────────────────────────────────────────
apt-get update -y
apt-get install -y ca-certificates curl gnupg iptables-persistent

# ──────────────────────────────────────────────
# Docker 설치
# ──────────────────────────────────────────────
echo "[INFO] Docker 설치 중..."
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable docker
systemctl start docker
usermod -aG docker ubuntu
echo "[INFO] Docker 설치 완료"

# ──────────────────────────────────────────────
# 앱 디렉토리 생성
# ──────────────────────────────────────────────
mkdir -p /var/app/current
chown ubuntu:ubuntu /var/app/current

mkdir -p /opt/secrets
chmod 700 /opt/secrets
echo "[INFO] 디렉토리 생성 완료"

# ──────────────────────────────────────────────
# OCI Ubuntu 22.04의 iptables 규칙에 HTTP/HTTPS 추가
# (Security List에서 허용하더라도 OS 레벨 방화벽도 열어야 함)
# ──────────────────────────────────────────────
iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
iptables -I INPUT 7 -m state --state NEW -p tcp --dport 443 -j ACCEPT
netfilter-persistent save
echo "[INFO] iptables 규칙 추가 완료"

echo "[INFO] === 초기화 완료 ==="
