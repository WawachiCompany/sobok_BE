# Nginx 기반
FROM nginx


# 엔트리포인트 설정
COPY entrypoint.sh /etc/nginx/entrypoint.sh
RUN chmod +x /etc/nginx/entrypoint.sh

# 컨테이너 실행 시 entrypoint.sh 실행
ENTRYPOINT ["/bin/sh", "-c", "/etc/nginx/entrypoint.sh"]