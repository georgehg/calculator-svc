CREATE TABLE operations.record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    operation_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    user_balance DECIMAL(10, 2) NOT NULL,
    operation_response TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (operation_id) REFERENCES operation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
