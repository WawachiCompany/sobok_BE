#!/bin/bash
set -e

# 마커 파일로 시크릿 다운로드 상태 확인
if [ ! -f "/opt/secrets/.secrets_downloaded" ] || [ "$(find /opt/secrets -name "*.json" -mtime +7 | wc -l)" -gt 0 ]; then
  echo "[INFO] Secret 파일 다운로드 중..."

  # 디렉토리 생성
  mkdir -p /opt/secrets

  # S3에서 비밀 파일 다운로드
  aws s3 cp s3://sobok-secrets/firebase-adminsdk.json /opt/secrets/firebase-adminsdk.json
  aws s3 cp s3://sobok-secrets/.env /opt/secrets/.env
  aws s3 cp s3://sobok-secrets/apple-authkey.p8 /opt/secrets/apple-authkey.p8

  # 파일 권한 설정
  chmod 644 /opt/secrets/firebase-adminsdk.json
  chmod 644 /opt/secrets/.env
  chmod 644 /opt/secrets/apple-authkey.p8

  # 소유권 설정
  chown root:root /opt/secrets/firebase-adminsdk.json
  chown root:root /opt/secrets/.env
  chown root:root /opt/secrets/apple-authkey.p8

  # 마커 파일 생성
  date > /opt/secrets/.secrets_downloaded

  echo "[INFO] Secret 파일이 성공적으로 다운로드되었습니다."
else
  echo "[INFO] Secret 파일이 이미 최신 상태입니다."
fi