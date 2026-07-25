variable "aws_region" {
  type        = string
  default     = "ap-northeast-1"
  description = "AWS region for deployment (Tokyo)"
}

variable "environment" {
  type        = string
  default     = "production"
  description = "Environment name"
}

variable "app_name" {
  type        = string
  default     = "spring-bank-ledger"
  description = "Application name used for resource naming"
}

variable "db_username" {
  type        = string
  default     = "ledger_user"
  description = "Master username for RDS MySQL"
}

variable "db_password" {
  type        = string
  sensitive   = true
  description = "Master password for RDS MySQL"
}

variable "db_name" {
  type        = string
  default     = "ledger"
  description = "Initial database name"
}
