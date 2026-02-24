variable "security_group_name" {
  description = "Name of the security group"
  type        = string
}

variable "security_group_description" {
  description = "Description of the security group"
  type        = string
}




variable "environment" {
  description = "The environment name (e.g., dev, prod)"
  type        = string
}