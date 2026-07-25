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

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.ledger_cluster.name
  description = "Name of the ECS Fargate Cluster"
}

output "ecs_service_name" {
  value       = aws_ecs_service.ledger_service.name
  description = "Name of the ECS Fargate Service"
}

output "github_actions_role_arn" {
  value       = aws_iam_role.github_actions.arn
  description = "IAM Role ARN for GitHub Actions OIDC Authentication"
}

output "get_task_public_ip_command" {
  value       = "aws ecs list-tasks --cluster ${aws_ecs_cluster.ledger_cluster.name} --region ${var.aws_region}"
  description = "AWS CLI command to list running Fargate tasks"
}
