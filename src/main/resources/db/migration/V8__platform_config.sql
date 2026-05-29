-- Platform configuration table (key-value store for super admin settings)
CREATE TABLE platform_config (
    id          BINARY(16)    NOT NULL,
    config_key  VARCHAR(100)  NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default values
INSERT INTO platform_config (id, config_key, config_value, description) VALUES
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000001', '-', '')), 'trial.duration.days',    '20',     'Duree de l''essai gratuit en jours'),
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000002', '-', '')), 'plan.pro.monthly.price', '299.00', 'Prix mensuel du plan Pro (MAD)'),
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000003', '-', '')), 'plan.max.monthly.price', '599.00', 'Prix mensuel du plan Max (MAD)'),
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000004', '-', '')), 'trial.reminder.days',    '5',      'Nombre de jours avant expiration pour envoyer le rappel'),
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000005', '-', '')), 'platform.name',          'KarFlow','Nom de la plateforme'),
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000006', '-', '')), 'platform.support.email', 'support@karflow.ma', 'Email de support'),
(UNHEX(REPLACE('c0000000-0000-0000-0000-000000000007', '-', '')), 'grace.period.days',      '7',      'Nombre de jours de grace avant suspension pour impaye');
