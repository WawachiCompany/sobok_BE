#!/bin/bash
set -euo pipefail

LOG_CFG="/opt/aws/amazon-cloudwatch-agent/etc/log-config.json"
FRAG="/var/app/current/.platform/files/caddy-log-config.json"
BACKUP="${LOG_CFG}.$(date +%Y%m%d%H%M%S).bak"

echo "[INFO] Ensure /var/log/caddy exists and is writable for Caddy(uid 1000)"
sudo mkdir -p /var/log/caddy
sudo chown -R 1000:1000 /var/log/caddy
sudo chmod 755 /var/log/caddy

# jq 설치 (환경에 따라 yum 또는 dnf)
if ! command -v jq >/dev/null 2>&1; then
  echo "[INFO] Installing jq ..."
  sudo yum -y install jq || sudo dnf -y install jq
fi

# 0) 스켈레톤 생성 (객체 보장)
if [ ! -f "$LOG_CFG" ]; then
  echo "[INFO] $LOG_CFG not found. Creating skeleton config."
  sudo mkdir -p "$(dirname "$LOG_CFG")"
  sudo tee "$LOG_CFG" >/dev/null <<'JSON'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": []
      }
    }
  }
}
JSON
fi

# 1) 백업
echo "[INFO] Backing up ${LOG_CFG} -> ${BACKUP}"
sudo cp -a "$LOG_CFG" "$BACKUP"

# 2) 병합 (구버전 jq 호환: -s 사용, 입력 순서 보장)
#    입력0: 기존 config(객체), 입력1: fragment(배열)
echo "[INFO] Merging caddy collectors into ${LOG_CFG} (idempotent)"
sudo jq -s '
  . as $all
  | ($all[0] // {}) as $cfg
  | ($all[1] // []) as $frag
  | $cfg
  | .logs = (.logs // {})
  | .logs.logs_collected = (.logs.logs_collected // {})
  | .logs.logs_collected.files = (.logs.logs_collected.files // {})
  | .logs.logs_collected.files.collect_list =
      (
        (.logs.logs_collected.files.collect_list // [])
        + $frag
      )
  | .logs.logs_collected.files.collect_list |= unique_by(.file_path)
' "$LOG_CFG" "$FRAG" | sudo tee "$LOG_CFG" >/dev/null

echo "[INFO] Restarting amazon-cloudwatch-agent"
sudo systemctl restart amazon-cloudwatch-agent || true
sudo systemctl enable amazon-cloudwatch-agent || true

echo "[INFO] Tail agent log:"
sudo tail -n 50 /opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log || true