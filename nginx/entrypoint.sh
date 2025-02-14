#!/bin/sh

# 인증서 경로 설정
CERT_PATH="/etc/letsencrypt/live/sobok-app.com/fullchain.pem"
KEY_PATH="/etc/letsencrypt/live/sobok-app.com/privkey.pem"
CERTBOT_WEBROOT="/var/www/certbot"

# Certbot에서 인증서를 발급할 때까지 대기
if [ ! -f "$CERT_PATH" ] || [ ! -f "$KEY_PATH" ]; then
  echo "🔄 SSL 인증서가 없습니다. HTTP 모드로 Nginx를 시작합니다..."

  # HTTPS 설정 비활성화, HTTP 설정 활성화
  mv /etc/nginx/conf.d/default-https.conf /etc/nginx/conf.d/default-https.conf.disabled 2>/dev/null
  mv /etc/nginx/conf.d/default-http.conf.disabled /etc/nginx/conf.d/default-http.conf 2>/dev/null

  # Nginx를 HTTP 모드로 시작 (certbot 인증서 발급을 위한 HTTP 접근 허용)
  nginx -g "daemon off;" &

  # Certbot을 별도 컨테이너에서 실행하는 경우 인증서 발급을 기다림
  echo "🔄 Certbot이 인증서를 발급할 때까지 대기 중..."
  while [ ! -f "$CERT_PATH" ] || [ ! -f "$KEY_PATH" ]; do
    sleep 5
  done

  echo "✅ 인증서가 발급되었습니다! HTTPS 모드로 전환합니다..."

  # Nginx 종료
  nginx -s stop

  # HTTPS 설정 활성화, HTTP 설정 비활성화
  mv /etc/nginx/conf.d/default-http.conf /etc/nginx/conf.d/default-http.conf.disabled 2>/dev/null
  mv /etc/nginx/conf.d/default-https.conf.disabled /etc/nginx/conf.d/default-https.conf 2>/dev/null
fi

echo "🚀 Nginx를 HTTPS 모드로 시작합니다..."
exec nginx -g "daemon off;"