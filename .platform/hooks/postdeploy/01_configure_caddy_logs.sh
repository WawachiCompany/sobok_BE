#!/bin/bash
set -e

echo "[INFO] Setting up CloudWatch logs for Caddy..."

CONF_DIR="/etc/awslogs/config"
CONF_PATH="$CONF_DIR/caddy.conf"

sudo mkdir -p "$CONF_DIR"

sudo bash -c "cat > $CONF_PATH" << 'EOF'
[/var/app/current/logs/caddy/access.log]
file = /var/app/current/logs/caddy/access.log
log_group_name = /aws/elasticbeanstalk/caddy/access
log_stream_name = {instance_id}
initial_position = start_of_file
log_retention_days = 14

[/var/app/current/logs/caddy/system.log]
file = /var/app/current/logs/caddy/system.log
log_group_name = /aws/elasticbeanstalk/caddy/system
log_stream_name = {instance_id}
initial_position = start_of_file
log_retention_days = 14

[/var/app/current/logs/caddy/error.log]
file = /var/app/current/logs/caddy/error.log
log_group_name = /aws/elasticbeanstalk/caddy/error
log_stream_name = {instance_id}
initial_position = start_of_file
log_retention_days = 14
EOF

# awslogs 서비스 재시작
sudo systemctl restart awslogsd
echo "[INFO] CloudWatch log configuration applied successfully."