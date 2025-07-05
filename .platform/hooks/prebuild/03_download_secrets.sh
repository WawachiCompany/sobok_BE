#!/bin/bash
set -e

# S3 파일들의 최신 수정 시간 확인
check_s3_updates() {
    echo "[INFO] S3 파일 업데이트 확인 중..."

    # S3 파일들의 LastModified 시간 가져오기
    ENV_MODIFIED=$(aws s3api head-object --bucket sobok-secrets --key .env --query 'LastModified' --output text 2>/dev/null || echo "")
    FIREBASE_MODIFIED=$(aws s3api head-object --bucket sobok-secrets --key firebase-adminsdk.json --query 'LastModified' --output text 2>/dev/null || echo "")
    APPLE_MODIFIED=$(aws s3api head-object --bucket sobok-secrets --key apple-authkey.p8 --query 'LastModified' --output text 2>/dev/null || echo "")

    # 현재 저장된 수정 시간 읽기
    STORED_ENV_MODIFIED=""
    STORED_FIREBASE_MODIFIED=""
    STORED_APPLE_MODIFIED=""

    if [ -f "/opt/secrets/.last_modified" ]; then
        source /opt/secrets/.last_modified
    fi

    # 변경사항 확인
    if [ "$ENV_MODIFIED" != "$STORED_ENV_MODIFIED" ] || \
       [ "$FIREBASE_MODIFIED" != "$STORED_FIREBASE_MODIFIED" ] || \
       [ "$APPLE_MODIFIED" != "$STORED_APPLE_MODIFIED" ] || \
       [ ! -f "/opt/secrets/.secrets_downloaded" ]; then
        return 0  # 업데이트 필요
    else
        return 1  # 업데이트 불필요
    fi
}

# 마커 파일로 시크릿 다운로드 상태 확인 또는 S3 파일 변경 확인
if check_s3_updates; then
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

  # S3 파일들의 최신 수정 시간 저장
  cat > /opt/secrets/.last_modified << EOF
STORED_ENV_MODIFIED="$ENV_MODIFIED"
STORED_FIREBASE_MODIFIED="$FIREBASE_MODIFIED"
STORED_APPLE_MODIFIED="$APPLE_MODIFIED"
EOF

  echo "[INFO] Secret 파일이 성공적으로 다운로드되었습니다."
else
  echo "[INFO] Secret 파일이 이미 최신 상태입니다."
fi