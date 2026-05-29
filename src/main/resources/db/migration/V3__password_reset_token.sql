CREATE TABLE password_reset_token (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    token         VARCHAR(255) NOT NULL,
    user_id       BINARY(16)   NOT NULL,
    expires_at    DATETIME     NOT NULL,
    used          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_prt_token (token),
    FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
