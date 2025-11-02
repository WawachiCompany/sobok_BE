#!/bin/bash
set -euo pipefail

DROPIN="/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.d/caddy.json"

sudo mkdir -p /var/log/caddy
sudo chown -R 1000:1000 /var/log/caddy
sudo chmod 755 /var/log/caddy

sudo mkdir -p "$(dirname "$DROPIN")"
sudo tee "$DROPIN" >/dev/null <<'JSON'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          { "file_path": "/var/log/caddy/access.log", "log_group_name": "/aws/elasticbeanstalk/caddy/access", "log_stream_name": "{instance_id}", "timestamp_format": "%b %d %H:%M:%S", "retention_in_days": 14 },
          { "file_path": "/var/log/caddy/system.log", "log_group_name": "/aws/elasticbeanstalk/caddy/system", "log_stream_name": "{instance_id}", "timestamp_format": "%b %d %H:%M:%S", "retention_in_days": 14 },
          { "file_path": "/var/log/caddy/error.log",  "log_group_name": "/aws/elasticbeanstalk/caddy/error",  "log_stream_name": "{instance_id}", "timestamp_format": "%b %d %H:%M:%S", "retention_in_days": 14 }
        ]
      }
    }
  }
}
JSON

/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -m ec2 -a append-config -c file:"$DROPIN" -s || true

systemctl restart amazon-cloudwatch-agent || true