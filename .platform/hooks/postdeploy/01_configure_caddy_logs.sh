#!/bin/bash
set -e

CFG_DIR="/opt/aws/amazon-cloudwatch-agent/etc"
CFG_FILE="${CFG_DIR}/amazon-cloudwatch-agent.json"

sudo mkdir -p "$CFG_DIR"

# CloudWatch Agent 설정(JSON): Caddy 로그 3종 업로드
sudo tee "$CFG_FILE" >/dev/null <<'JSON'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/app/current/logs/caddy/access.log",
            "log_group_name": "/aws/elasticbeanstalk/caddy/access",
            "log_stream_name": "{instance_id}",
            "timestamp_format": "%b %d %H:%M:%S",
            "retention_in_days": 14
          },
          {
            "file_path": "/var/app/current/logs/caddy/system.log",
            "log_group_name": "/aws/elasticbeanstalk/caddy/system",
            "log_stream_name": "{instance_id}",
            "timestamp_format": "%b %d %H:%M:%S",
            "retention_in_days": 14
          },
          {
            "file_path": "/var/app/current/logs/caddy/error.log",
            "log_group_name": "/aws/elasticbeanstalk/caddy/error",
            "log_stream_name": "{instance_id}",
            "timestamp_format": "%b %d %H:%M:%S",
            "retention_in_days": 14
          }
        ]
      }
    },
    "log_stream_name": "default-stream"
  }
}
JSON

# 설정 적용 & 에이전트 시작
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -c file:"$CFG_FILE" -s

sudo systemctl enable amazon-cloudwatch-agent
sudo systemctl restart amazon-cloudwatch-agent