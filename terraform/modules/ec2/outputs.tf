output "instance_id" {
  value = aws_instance.web_server.id
}

output "private_key_path" {
  value = local_sensitive_file.private_key.filename
}

