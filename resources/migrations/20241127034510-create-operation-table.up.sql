CREATE TABLE operations.operation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('addition', 'subtraction', 'multiplication', 'division', 'square_root', 'random_string') NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
