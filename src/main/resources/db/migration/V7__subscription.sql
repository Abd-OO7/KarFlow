CREATE TABLE subscription (
    id                    BINARY(16)    NOT NULL,
    tenant_id             BINARY(16)    NOT NULL,
    organisation_id       BINARY(16)    NOT NULL,
    plan                  VARCHAR(20)   NOT NULL DEFAULT 'TRIAL',
    status                VARCHAR(20)   NOT NULL DEFAULT 'TRIAL',
    trial_start_date      DATETIME      NOT NULL,
    trial_end_date        DATETIME      NOT NULL,
    current_period_start  DATETIME,
    current_period_end    DATETIME,
    monthly_price         DECIMAL(19,2) DEFAULT 0.00,
    auto_renew            BOOLEAN       NOT NULL DEFAULT TRUE,
    suspended_at          DATETIME,
    cancelled_at          DATETIME,
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted               BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_sub_tenant (tenant_id),
    INDEX idx_sub_status (status),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE subscription_history (
    id                BINARY(16)  NOT NULL,
    tenant_id         BINARY(16)  NOT NULL,
    subscription_id   BINARY(16)  NOT NULL,
    previous_plan     VARCHAR(20),
    new_plan          VARCHAR(20) NOT NULL,
    previous_status   VARCHAR(20),
    new_status        VARCHAR(20) NOT NULL,
    change_date       DATETIME    NOT NULL,
    reason            VARCHAR(500),
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_sh_sub (subscription_id),
    FOREIGN KEY (subscription_id) REFERENCES subscription(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create TRIAL subscriptions for existing organisations
INSERT INTO subscription (id, tenant_id, organisation_id, plan, status, trial_start_date, trial_end_date, monthly_price, auto_renew, created_at, updated_at, deleted)
SELECT UNHEX(REPLACE(UUID(), '-', '')), o.tenant_id, o.id, 'TRIAL', 'TRIAL', NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), 0.00, TRUE, NOW(), NOW(), FALSE
FROM organisation o WHERE o.deleted = FALSE
AND NOT EXISTS (SELECT 1 FROM subscription s WHERE s.organisation_id = o.id);
