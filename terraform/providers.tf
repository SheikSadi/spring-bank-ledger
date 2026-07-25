terraform {
  required_version = ">= 1.5.0"

  backend "s3" {
    bucket  = "spring-bank-ledger-tfstate-sheiksadi"
    key     = "terraform.tfstate"
    region  = "ap-northeast-1"
    encrypt = true
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "spring-bank-ledger"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
