#!/bin/bash
set -euo pipefail

LOG_CFG="/opt/aws/amazon-cloudwatch-agent/etc/log-config.json"
FRAG="/var/app/current/.platform/files/caddy-log-config.json"
BACKUP="${LOG_CFG}.$(date +%Y%m%d%H%M%S).bak"
TMP="/tmp/log-config.json.$$"

echo "[INFO] ensure /var/log/caddy (uid=1000)"
sudo mkdir -p /var/log/caddy
sudo chown -R 1000:1000 /var/log/caddy
sudo chmod 755 /var/log/caddy

# jq 설치 (환경별)
if ! command -v jq >/dev/null 2>&1; then
  echo "[INFO] installing jq"
  sudo yum -y install jq || sudo dnf -y install jq
fi
echo "[INFO] jq version: $(jq --version || echo unknown)"

# 0) 조각 파일 검증 (배열이어야 함)
if ! jq -e 'type=="array"' "$FRAG" >/dev/null 2>&1; then
  echo "[ERROR] $FRAG must be a JSON array."; exit 2
fi
FRAG_JSON="$(cat "$FRAG")"   # --argjson로 주입할 원본

# 1) 원본 config 보장 (객체 스켈레톤)
if [ ! -f "$LOG_CFG" ] || ! jq -e 'type=="object"' "$LOG_CFG" >/dev/null 2>&1; then
  echo "[WARN] $LOG_CFG missing or not an object. Creating skeleton."
  sudo mkdir -p "$(dirname "$LOG_CFG")"
  sudo tee "$LOG_CFG" >/dev/null <<'JSON'
{
  "logs": {
    "logs_collected": {
      "files": { "collect_list": [] }
    }
  }
}
JSON
fi

# 2) 백업
echo "[INFO] backup -> $BACKUP"
sudo cp -a "$LOG_CFG" "$BACKUP"

# 3) 병합 (idempotent, jq 구버전 호환)
sudo jq --argjson frag "$FRAG_JSON" '
  .logs = (.logs // {}) |
  .logs.logs_collected = (.logs.logs_collected // {}) |
  .logs.logs_collected.files = (.logs.logs_collected.files // {}) |
  .logs.logs_collected.files.collect_list =
     (((.logs.logs_collected.files.collect_list // []) + $frag) | unique_by(.file_path))
' "$LOG_CFG" | sudo tee "$TMP" >/dev/null

# 4) 원자적 교체
sudo mv "$TMP" "$LOG_CFG"

# 5) 에이전트 재시작
sudo systemctl restart amazon-cloudwatch-agent || true
sudo systemctl enable amazon-cloudwatch-agent || true

echo "[INFO] tail agent log (last 50 lines):"
sudo tail -n 50 /opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log || true