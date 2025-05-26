#!/bin/bash
set -e

echo "[INFO] 스크립트 실행 권한 설정 중..."

# 모든 .platform/hooks 디렉토리 내 스크립트에 실행 권한 부여
find /var/app/staging/.platform/hooks -type f -name "*.sh" -exec chmod +x {} \;

# 권한 부여 확인
find /var/app/staging/.platform/hooks -type f -name "*.sh" | while read script; do
  if [ -x "$script" ]; then
    echo "[INFO] 권한 설정 완료: $script"
  else
    echo "[ERROR] 권한 설정 실패: $script"
    exit 1
  fi
done

echo "[INFO] 모든 스크립트에 실행 권한 부여 완료"