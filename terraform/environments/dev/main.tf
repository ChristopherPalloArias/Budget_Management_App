module "vpc" {
  source = "../../modules/vpc"
  environment = var.environment
}

module "security_group" {
  source = "../../modules/security-group"
  security_group_name = "web-sg"
  security_group_description = "Security group for web server"
  environment = var.environment
}

module "ec2" {
  source = "../../modules/ec2"
  ami_id = var.ami_id
  instance_type = var.instance_type
  security_group_id = module.security_group.security_group_id
  instance_name = var.instance_name
  repo_branch = var.repo_branch
  repo_url = var.repo_url
  environment = var.environment 
}

