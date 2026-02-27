resource "aws_vpc" "main" {
  tags = {
    Name = "vpc-main"
    Environment = var.environment
  }
}
