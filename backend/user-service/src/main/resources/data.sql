-- Insert roles if not exists
INSERT INTO roles (role_name) 
SELECT 'ROLE_USER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE role_name = 'ROLE_USER'
);

INSERT INTO roles (role_name) 
SELECT 'ROLE_SELLER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE role_name = 'ROLE_SELLER'
);

INSERT INTO roles (role_name) 
SELECT 'ROLE_BROKER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE role_name = 'ROLE_BROKER'
);

INSERT INTO roles (role_name) 
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE role_name = 'ROLE_ADMIN'
);

-- Password is 'admin123' encoded with BCrypt
INSERT INTO users (username, email, password, first_name, last_name, phone_number, active, created_at, updated_at)
SELECT 'admin', 'admin@example.com', '$2a$12$oo0/RS0XYgmumbzYOyxohedLvGUk6/gEHzD7VhBK22VIVwSfH7Lx.', 'Admin', 'User', '0123456789', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.role_id
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_name = 'ROLE_USER'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.role_id
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.role_id
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_name = 'ROLE_SELLER'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.role_id
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.role_id
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_name = 'ROLE_BROKER'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.role_id
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.role_id
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.role_id
); 