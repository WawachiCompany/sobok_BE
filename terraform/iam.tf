# ──────────────────────────────────────────────
# Dynamic Group
# 인스턴스가 OCI API를 직접 호출할 수 있도록 (Instance Principal)
# 시크릿 다운로드 스크립트(.platform/hooks/prebuild/03_download_secrets.sh)에서 사용
# ──────────────────────────────────────────────

resource "oci_identity_dynamic_group" "app" {
  # Dynamic Group은 tenancy 레벨에서 생성
  compartment_id = var.tenancy_ocid
  name           = "${var.app_name}-instance-group"
  description    = "${var.app_name} compute instances (Instance Principal)"

  # compartment 내 모든 인스턴스를 포함
  matching_rule = "ANY {instance.compartment.id = '${var.compartment_ocid}'}"
}

# ──────────────────────────────────────────────
# IAM Policy
# Dynamic Group이 Object Storage(Sobok-env)를 읽을 수 있도록 허용
# ──────────────────────────────────────────────

resource "oci_identity_policy" "app_object_storage" {
  compartment_id = var.compartment_ocid
  name           = "${var.app_name}-object-storage-policy"
  description    = "Allow ${var.app_name} instances to read secrets from Object Storage"

  statements = [
    "Allow dynamic-group ${oci_identity_dynamic_group.app.name} to read objects in compartment id ${var.compartment_ocid} where target.bucket.name='Sobok-env'",
  ]
}
