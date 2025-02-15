#!/bin/sh

CERT_PATH="/etc/letsencrypt/live/sobok-app.com/fullchain.pem"
KEY_PATH="/etc/letsencrypt/live/sobok-app.com/privkey.pem"
NGINX_CONF_DIR="/etc/nginx/conf.d"

# 기존 프로세스 종료 (혹시 있을 경우)
echo "🔻 Stopping any existing Nginx process..."
nginx -s stop 2>/dev/null || pkill -9 nginx 2>/dev/null

# 1) 우선 default-http.conf를 default.conf로 복사해서 HTTP 모드로 실행
echo "🔧 Using HTTP mode configuration"
cp "$NGINX_CONF_DIR/default-http.conf" "$NGINX_CONF_DIR/default.conf" 2>/dev/null

echo "🚀 Starting Nginx in HTTP mode..."
nginx -g "daemon off;" &

# 2) 인증서가 발급될 때까지 대기
echo "🔍 Waiting for certificate files..."
while [ ! -f "$CERT_PATH" ] || [ ! -f "$KEY_PATH" ]; do
    echo "⏳ Certificate not found yet, sleeping 5s..."
    sleep 5
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