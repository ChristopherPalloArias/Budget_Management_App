resource "tls_private_key" "ec2_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "generated_key" {
  key_name   = "${var.instance_name}-key"
  public_key = tls_private_key.ec2_key.public_key_openssh
}

resource "local_sensitive_file" "private_key" {
  content  = tls_private_key.ec2_key.private_key_pem
  filename = "${path.module}/${var.instance_name}-key.pem"
}

resource "aws_instance" "web_server" {
  ami           = var.ami_id
  instance_type = var.instance_type
  key_name      = aws_key_pair.generated_key.key_name

  vpc_security_group_ids = [var.security_group_id]
  user_data              = templatefile("${path.module}/user_data.sh", {
    repo_url    = var.repo_url
    repo_branch = var.repo_branch
  })

  tags = {
    Name = var.instance_name
  }
}

output "instance_id" {
  value = aws_instance.web_server.id
}

output "private_key_path" {
  value = local_sensitive_file.private_key.filename
}

resource "aws_instance" "main" {
  ami           = var.ami_id
  instance_type = var.instance_type
  vpc_security_group_ids = [var.security_group_id]

  tags = {
    Name = "ec2-instance"
    Environment = var.environment
  }
}

output "instance_id" {
  value = aws_instance.main.id
}