# IAM Role for App Runner to pull images from ECR
resource "aws_iam_role" "app_runner_ecr_role" {
  name = "${var.app_name}-apprunner-ecr-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "build.apprunner.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "app_runner_ecr_policy" {
  role       = aws_iam_role.app_runner_ecr_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

# AWS App Runner Service
resource "aws_apprunner_service" "ledger_service" {
  service_name = var.app_name

  source_configuration {
    authentication_configuration {
      access_role_arn = aws_iam_role.app_runner_ecr_role.arn
    }

    image_repository {
      image_identifier      = "${aws_ecr_repository.app_repo.repository_url}:latest"
      image_repository_type = "ECR"

      image_configuration {
        port = "8080"
        runtime_environment_variables = {
          "SPRING_PROFILES_ACTIVE"     = "mysql"
          "SPRING_DATASOURCE_URL"      = "jdbc:mysql://${aws_db_instance.ledger_db.endpoint}/${var.db_name}?useSSL=false&allowPublicKeyRetrieval=true"
          "SPRING_DATASOURCE_USERNAME" = var.db_username
          "SPRING_DATASOURCE_PASSWORD" = var.db_password
          "PORT"                       = "8080"
        }
      }
    }

    auto_deployments_enabled = true
  }

  instance_configuration {
    cpu    = "1024" # 1 vCPU
    memory = "2048" # 2 GB RAM
  }

  tags = {
    Name = var.app_name
  }

  depends_on = [
    aws_iam_role_policy_attachment.app_runner_ecr_policy,
    aws_db_instance.ledger_db
  ]
}
