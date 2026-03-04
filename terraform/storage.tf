data "oci_objectstorage_namespace" "ns" {
  compartment_id = var.compartment_ocid
}

# 시크릿 파일(.env, firebase-adminsdk.json) 보관 버킷
# 03_download_secrets.sh에서 이 버킷에서 파일을 다운로드함
resource "oci_objectstorage_bucket" "secrets" {
  compartment_id = var.compartment_ocid
  namespace      = data.oci_objectstorage_namespace.ns.namespace
  name           = "Sobok-env"
  access_type    = "NoPublicAccess"
  versioning     = "Enabled"
}
