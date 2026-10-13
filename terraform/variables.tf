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
  description = "IP range allowed to reach the VM. Set this to your own IP, e.g. 1.2.3.4/32"
  type        = string
  default     = "0.0.0.0/0"
}

variable "ssh_public_key_path" {
  type    = string
  default = "~/.ssh/id_rsa.pub"
}
