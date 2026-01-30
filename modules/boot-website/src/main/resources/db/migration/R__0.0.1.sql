CREATE TABLE IF NOT EXISTS website_data
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    command VARCHAR(32)  NOT NULL,
    url     VARCHAR(2048) NOT NULL,
    alias   VARCHAR(32),
    CONSTRAINT uk_command UNIQUE (command)
);