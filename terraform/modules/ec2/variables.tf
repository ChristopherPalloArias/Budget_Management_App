variable "ami_id" {
  description = "The AMI ID to use for the instance"
  type        = string
}

variable "instance_type" {
  description = "The type of EC2 instance"
  type        = string
}

variable "instance_name" {
  description = "The name tag for the EC2 instance"
  type        = string
}

variable "repo_url" {
  description = "The Git repository URL to clone"
  type        = string
}

variable "repo_branch" {
  description = "The branch of the repository to clone"
  type        = string
}

variable "security_group_id" {
  description = "The ID of the security group to attach"
  type        = string
}

variable "environment" {}