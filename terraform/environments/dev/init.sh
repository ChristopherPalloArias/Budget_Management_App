#!/bin/bash
set -e

# Initialize Terraform
terraform init

# Validate Terraform configuration
terraform validate

# Format Terraform files
terraform fmt -recursive

# Plan Terraform changes
terraform plan