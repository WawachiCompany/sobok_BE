#!/bin/sh

# Certbot 인증서 폴더 존재 여부 확인
if [ ! -d "/etc/letsencrypt/live/sobok-app.com" ]; then
    echo "No SSL certificate found, running Certbot..."
    certbot certonly --webroot -w /var/www/certbot \
        -d sobok-app.com -d www.sobok-app.com \
        --email hjkim4842@gmail.com --agree-tos --no-eff-email
fi

echo "Starting Nginx..."
nginx -g 'daemon off;'