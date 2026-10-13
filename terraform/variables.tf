variable "subscription_id" {
  description = "Azure subscription ID (run: az account show --query id -o tsv)"
  type        = string
}

variable "location" {
  description = "Azure region. Student subscriptions may only allow some regions."
  type        = string
  default     = "centralindia"
}

variable "vm_size" {
  description = "VM size. The full stack needs ~4 GB RAM."
  type        = string
  default     = "Standard_B2s"
}

variable "allowed_cidr" {
  description = "Your own IP, e.g. 1.2.3.4/32. Only this IP can reach SSH (22) and Grafana (3000)."
  type        = string
}

variable "ssh_public_key_path" {
  type    = string
  default = "~/.ssh/id_rsa.pub"
}
