output "aws_region" {
  value       = var.aws_region
  description = "AWS region deployed to"
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.app_repo.repository_url
  description = "URL of the AWS ECR Repository"
}

output "rds_endpoint" {
  value       = aws_db_instance.ledger_db.endpoint
  description = "Connection endpoint for RDS MySQL"
}

output "app_runner_url" {
  value       = "https://${aws_apprunner_service.ledger_service.service_url}"
  description = "Live public HTTPS URL of App Runner service"
}

output "swagger_ui_url" {
  value       = "https://${aws_apprunner_service.ledger_service.service_url}/swagger-ui/index.html"
  description = "Live interactive Swagger UI documentation URL"
}
