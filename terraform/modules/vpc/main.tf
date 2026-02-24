resource "aws_vpc" "main" {
  tags = {
    Name = "vpc-main"
    Environment = var.environment
  }
}

output "vpc_id" {
  value = aws_vpc.main.id
}