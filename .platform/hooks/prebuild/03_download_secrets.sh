#!/bin/bash
set -e

# Object Storage 고정 설정 (환경 변수 없이 직접 지정)
OCI_NAMESPACE="axtjkitujsdc"
OCI_BUCKET="Sobok-env"
ENV_OBJECT=".env"
FIREBASE_OBJECT="firebase-adminsdk.json"

if [ -z "$OCI_NAMESPACE" ] || [ -z "$OCI_BUCKET" ]; then
  echo "[ERROR] OCI namespace 또는 bucket 정보가 없습니다. OCI_NAMESPACE/OCI_BUCKET 환경 변수를 설정하세요."
  exit 1
fi

if ! command -v oci >/dev/null 2>&1; then
  echo "[ERROR] oci CLI를 찾을 수 없습니다. 인스턴스에 OCI CLI를 설치해주세요."
  exit 1
fi

head_object() {
    local object_name=$1
    oci os object head \
        --namespace "$OCI_NAMESPACE" \
        --bucket-name "$OCI_BUCKET" \
        --name "$object_name" \
        --query 'data."etag"' \
        --raw-output 2>/dev/null || echo ""
}

download_object() {
    local object_name=$1
    local target_path=$2
    oci os object get \
        --namespace "$OCI_NAMESPACE" \
        --bucket-name "$OCI_BUCKET" \
        --name "$object_name" \
        --file "$target_path"
}

check_object_updates() {
    echo "[INFO] Object Storage 파일 업데이트 확인 중..."

    ENV_ETAG=$(head_object "$ENV_OBJECT")
    FIREBASE_ETAG=$(head_object "$FIREBASE_OBJECT")

    STORED_ENV_ETAG=""
    STORED_FIREBASE_ETAG=""

    if [ -f "/opt/secrets/.last_modified" ]; then
        source /opt/secrets/.last_modified
    fi

    if [ "$ENV_ETAG" != "$STORED_ENV_ETAG" ] || \
       [ "$FIREBASE_ETAG" != "$STORED_FIREBASE_ETAG" ] || \
       [ ! -f "/opt/secrets/.secrets_downloaded" ]; then
        return 0
    else
        return 1
    fi
}

# 마커 파일로 시크릿 다운로드 상태 확인 또는 Object Storage 파일 변경 확인
if check_object_updates; then
  echo "[INFO] Secret 파일 다운로드 중..."

  # 디렉토리 생성
  mkdir -p /opt/secrets

  # Object Storage에서 비밀 파일 다운로드
  download_object "$FIREBASE_OBJECT" /opt/secrets/firebase-adminsdk.json
  download_object "$ENV_OBJECT" /opt/secrets/.env

  # 파일 권한 설정
  chmod 644 /opt/secrets/firebase-adminsdk.json
  chmod 644 /opt/secrets/.env

  # 소유권 설정
  chown root:root /opt/secrets/firebase-adminsdk.json
  chown root:root /opt/secrets/.env

  # 마커 파일 생성
  date > /opt/secrets/.secrets_downloaded

  # Object Storage 파일들의 최신 수정 시간 저장
  cat > /opt/secrets/.last_modified << EOF
STORED_ENV_ETAG="$ENV_ETAG"
STORED_FIREBASE_ETAG="$FIREBASE_ETAG"
EOF

  echo "[INFO] Secret 파일이 성공적으로 다운로드되었습니다."
else
  echo "[INFO] Secret 파일이 이미 최신 상태입니다."
fi