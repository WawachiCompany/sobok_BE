variable "tenancy_ocid" {
  description = "OCI Tenancy OCID"
  type        = string
}

variable "compartment_ocid" {
  description = "리소스를 생성할 Compartment OCID"
  type        = string
}

variable "region" {
  description = "OCI 리전 (예: ap-seoul-1, ap-chuncheon-1)"
  type        = string
  default     = "ap-chuncheon-1"
}

variable "instance_shape" {
  description = "Compute 인스턴스 Shape (VM.Standard.A1.Flex = ARM 무료 티어)"
  type        = string
  default     = "VM.Standard.A1.Flex"
}

variable "instance_ocpus" {
  description = "Flexible Shape OCPU 수 (A1 무료 티어: 최대 4)"
  type        = number
  default     = 1
}

variable "instance_memory_in_gbs" {
  description = "Flexible Shape 메모리 (GB, A1 무료 티어: 최대 24)"
  type        = number
  default     = 6
}

variable "ssh_public_key" {
  description = "인스턴스 접속용 SSH 공개키"
  type        = string
}

variable "app_name" {
  description = "리소스 이름 prefix"
  type        = string
  default     = "sobok"
}

variable "config_file_profile" {
  description = "~/.oci/config 파일의 프로파일명"
  type        = string
  default     = "DEFAULT"
}
