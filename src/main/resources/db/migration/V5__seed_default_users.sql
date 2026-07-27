INSERT INTO users (id, user_name, first_name, last_name, email, role, password_hash)
VALUES
('admin-id', NULL, NULL, NULL, 'admin@example.com', 'ADMIN', '$2a$10$bEhvzR6VvB4jOThmzo2DnOQgGiU/v0VzJ/IJDBENHCjZeZyVe3rGi'),
('user-id', NULL, NULL, NULL, 'test@example.com', 'USER', '$2a$10$lKoa9VYIx3AFS..U4atx2ugnZ2774Z1JI520L845ua3nFtF6phBZi')
ON DUPLICATE KEY UPDATE email=email;
