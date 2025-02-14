#!/bin/sh

# 인증서 경로 설정
CERT_PATH="/etc/letsencrypt/live/sobok-app.com/fullchain.pem"
KEY_PATH="/etc/letsencrypt/live/sobok-app.com/privkey.pem"

# 인증서가 존재할 때까지 대기
echo "🔄 Checking for existing SSL certificate..."
while [ ! -f "$CERT_PATH" ] || [ ! -f "$KEY_PATH" ]; do
    echo "⚠️ SSL certificate not found. Waiting for certbot to generate it..."
    sleep 5  # 5초 대기 후 다시 확인
done

echo "✅ SSL certificate found! Starting Nginx with HTTPS..."
nginx -g 'daemon off;'