resource "aws_security_group" "web_sg" {
  name        = var.security_group_name
  description = var.security_group_description

  # SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
  }

  # HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
  }

  # Egress
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
  }

  tags = {
    Name = var.security_group_name
  }
}

output "security_group_id" {
  value = aws_security_group.web_sg.id
}