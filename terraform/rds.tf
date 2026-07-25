# DB Subnet Group using default VPC subnets
resource "aws_db_subnet_group" "ledger_db_subnets" {
  name       = "${var.app_name}-db-subnets"
  subnet_ids = data.aws_subnets.default.ids

  tags = {
    Name = "${var.app_name}-db-subnets"
  }
}

# Free-Tier Eligible RDS MySQL Instance
resource "aws_db_instance" "ledger_db" {
  identifier             = "${var.app_name}-db"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro" # Free Tier eligible
  allocated_storage      = 20            # Free Tier limit (GB)
  max_allocated_storage  = 20            # Prevent auto-scaling costs
  storage_type           = "gp3"
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password
  parameter_group_name   = "default.mysql8.0"
  db_subnet_group_name   = aws_db_subnet_group.ledger_db_subnets.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible    = true
  skip_final_snapshot    = true
  deletion_protection    = false

  tags = {
    Name = "${var.app_name}-db"
  }
}
