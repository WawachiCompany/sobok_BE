#!/bin/bash
set -euo pipefail

LOG_CFG="/opt/aws/amazon-cloudwatch-agent/etc/log-config.json"
FRAG="/var/app/current/.platform/files/caddy-log-config.json"
BACKUP="${LOG_CFG}.$(date +%Y%m%d%H%M%S).bak"

echo "[INFO] Ensure /var/log/caddy exists and is writable for Caddy(uid 1000)"
sudo mkdir -p /var/log/caddy
sudo chown -R 1000:1000 /var/log/caddy
sudo chmod 755 /var/log/caddy

# jq가 없으면 설치 (AL2/AL2023 모두 동작)
if ! command -v jq >/dev/null 2>&1; then
  echo "[INFO] Installing jq ..."
  sudo yum -y install jq || sudo dnf -y install jq
fi

echo "[INFO] Backing up ${LOG_CFG} -> ${BACKUP}"
sudo cp -a "$LOG_CFG" "$BACKUP"

echo "[INFO] Merging Caddy log collectors into CloudWatch Agent config (idempotent)"
# collect_list가 없을 수도 있으니 안전하게 생성 후 병합
sudo jq --argfile frag "$FRAG" '
  .logs.logs_collected.files.collect_list =
    (
      (.logs.logs_collected.files.collect_list // [])
      +
      $frag
    )
  # 중복 제거(같은 file_path가 있으면 하나만 남김)
  | .logs.logs_collected.files.collect_list
    |= (unique_by(.file_path))
' "$LOG_CFG" | sudo tee "$LOG_CFG" >/dev/null

echo "[INFO] Restarting amazon-cloudwatch-agent"
sudo systemctl restart amazon-cloudwatch-agent
sudo systemctl enable amazon-cloudwatch-agent

echo "[INFO] Done. Tail agent log for verification:"
sudo tail -n 50 /opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log || true