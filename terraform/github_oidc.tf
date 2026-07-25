# 1. GitHub OpenID Connect (OIDC) Identity Provider
resource "aws_iam_openid_connect_provider" "github" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
  thumbprint_list = [
    "6938fd4d98bab03faadb97b34396831e3780aea1",
    "1c58a21d290d98f469e0616144d15195a435d2b0"
  ]

  tags = {
    Name = "${var.app_name}-github-oidc"
  }
}

# 2. IAM Role for GitHub Actions (AssumeRole via OIDC WebIdentity)
resource "aws_iam_role" "github_actions" {
  name = "${var.app_name}-github-actions-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = [
              "repo:SheikSadi*/spring-bank-ledger*:*",
              "repo:sheiksadi*/spring-bank-ledger*:*"
            ]
          }
        }
      }
    ]
  })

  tags = {
    Name = "${var.app_name}-github-actions-role"
  }
}

# 3. Attach ECR Access Policy
resource "aws_iam_role_policy_attachment" "github_ecr_policy" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}

# 4. Attach ECS Access Policy
resource "aws_iam_role_policy_attachment" "github_ecs_policy" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonECS_FullAccess"
}
