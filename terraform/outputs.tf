output "instance_public_ip" {
  description = "인스턴스 공인 IP — DNS A 레코드 및 GitHub Secret OCI_HOST에 설정"
  value       = oci_core_instance.app.public_ip
}

output "instance_id" {
  description = "인스턴스 OCID"
  value       = oci_core_instance.app.id
}

output "object_storage_namespace" {
  description = "Object Storage 네임스페이스 — 03_download_secrets.sh의 OCI_NAMESPACE 값"
  value       = data.oci_objectstorage_namespace.ns.namespace
}

output "bucket_name" {
  description = "시크릿 버킷 이름"
  value       = oci_objectstorage_bucket.secrets.name
}

output "ssh_command" {
  description = "인스턴스 SSH 접속 명령어"
  value       = "ssh ubuntu@${oci_core_instance.app.public_ip}"
}
