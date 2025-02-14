#!/bin/sh

CERT_PATH="/etc/letsencrypt/live/sobok-app.com/fullchain.pem"
KEY_PATH="/etc/letsencrypt/live/sobok-app.com/privkey.pem"
NGINX_CONF_DIR="/etc/nginx/conf.d"

# 기존 프로세스 종료 (혹시 있을 경우)
nginx -s stop 2>/dev/null

# 인증서가 존재하면 HTTPS 모드로 실행
if [ -f "$CERT_PATH" ] && [ -f "$KEY_PATH" ]; then
    echo "✅ 인증서가 존재합니다. HTTPS 모드로 실행합니다."
    cp "$NGINX_CONF_DIR/default-https.conf" "$NGINX_CONF_DIR/default.conf"
else
    echo "⚠️ 인증서가 존재하지 않습니다. HTTP 모드로 실행합니다."
    cp "$NGINX_CONF_DIR/default-http.conf" "$NGINX_CONF_DIR/default.conf"
fi

# Nginx 시작
nginx &

# 인증서 발급을 위한 대기 (Certbot 컨테이너가 실행 중일 것으로 가정)
while [ ! -f "$CERT_PATH" ] || [ ! -f "$KEY_PATH" ]; do
    echo "⏳ 인증서 발급을 기다리는 중..."
    sleep 10  # 10초 간격으로 인증서 확인
done

# 인증서가 발급되었으면 HTTPS 설정 적용
echo "✅ 인증서 발급 확인 완료. HTTPS 설정을 적용합니다."
cp "$NGINX_CONF_DIR/default-https.conf" "$NGINX_CONF_DIR/default.conf"
nginx -s reload

# Nginx를 포그라운드 실행 (컨테이너가 종료되지 않도록)
exec nginx -g 'daemon off;'