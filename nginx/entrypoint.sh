#!/bin/sh

CERT_PATH="/etc/letsencrypt/live/sobok-app.com/fullchain.pem"
KEY_PATH="/etc/letsencrypt/live/sobok-app.com/privkey.pem"
NGINX_CONF_DIR="/etc/nginx/conf.d"
MAX_WAIT_TIME=300  # 최대 대기 시간 (초 단위)

# 기존 프로세스 종료 (혹시 있을 경우)
echo "🔻 Stopping any existing Nginx process..."
nginx -s stop 2>/dev/null || pkill -9 nginx 2>/dev/null

# 1) 우선 default-http.conf를 default.conf로 복사해서 HTTP 모드로 실행
echo "🔧 Using HTTP mode configuration"
cp "$NGINX_CONF_DIR/default-http.conf" "$NGINX_CONF_DIR/default.conf" 2>/dev/null

echo "🚀 Starting Nginx in HTTP mode..."
nginx -g "daemon off;" &
NGINX_PID=$!

# 2) 인증서가 발급될 때까지 대기 (시간 제한 추가)
echo "🔍 Waiting for certificate files (max ${MAX_WAIT_TIME}s)..."
ELAPSED_TIME=0

while [ ! -f "$CERT_PATH" ] || [ ! -f "$KEY_PATH" ]; do
    if [ $ELAPSED_TIME -ge $MAX_WAIT_TIME ]; then
        echo "⚠️ Certificate not found after ${MAX_WAIT_TIME} seconds. Continuing with HTTP mode."
        # 컨테이너 종료 대신 HTTP 모드로 계속 실행
        wait $NGINX_PID
        exit 0
    fi

    echo "⏳ Certificate not found yet, sleeping 5s... (${ELAPSED_TIME}s elapsed)"
    sleep 5
    ELAPSED_TIME=$((ELAPSED_TIME + 5))
done

echo "✅ Certificate files detected, switching to HTTPS mode..."


# 3) Nginx 중지 (파일 이동 충돌 방지)
nginx -s stop 2>/dev/null || pkill -9 nginx 2>/dev/null

# 4) default-https.conf.disabled → default-https.conf 로 이름 변경
if [ -f "$NGINX_CONF_DIR/default-https.conf.disabled" ]; then
    echo "🔧 Renaming default-https.conf.disabled → default-https.conf"
    mv "$NGINX_CONF_DIR/default-https.conf.disabled" "$NGINX_CONF_DIR/default-https.conf" 2>/dev/null || {
        echo "⏩ mv failed, trying cp+rm fallback..."
        cp "$NGINX_CONF_DIR/default-https.conf.disabled" "$NGINX_CONF_DIR/default-https.conf"
        rm -f "$NGINX_CONF_DIR/default-https.conf.disabled"
    }
fi

# 5) 이제 default-https.conf를 default.conf로 설정
if [ -f "$NGINX_CONF_DIR/default-https.conf" ]; then
    cp "$NGINX_CONF_DIR/default-https.conf" "$NGINX_CONF_DIR/default.conf"
fi

# 6) Nginx HTTPS 모드로 다시 실행
echo "🚀 Restarting Nginx with HTTPS..."
exec nginx -g "daemon off;"