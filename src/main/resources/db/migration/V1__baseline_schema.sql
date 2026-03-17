-- =====================================================
-- V1 : Baseline schema — KarFlow SaaS
-- Crée les tables de base pour les features auth et organisation.
-- Les tables métier seront ajoutées par les migrations suivantes.
-- =====================================================

-- ── Organisations (tenants) ──
CREATE TABLE organisation (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    name          VARCHAR(255) NOT NULL,
    siret         VARCHAR(20),
    phone         VARCHAR(20),
    email         VARCHAR(255),
    logo_url      VARCHAR(500),
    address       VARCHAR(500),
    subscription_plan VARCHAR(50) DEFAULT 'FREE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_org_tenant (tenant_id),
    INDEX idx_org_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Rôles ──
CREATE TABLE role (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_role_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Permissions ──
CREATE TABLE permission (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    name          VARCHAR(100) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_perm_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Role ↔ Permission (M:N) ──
CREATE TABLE role_permission (
    role_id       BINARY(16) NOT NULL,
    permission_id BINARY(16) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Utilisateurs ──
CREATE TABLE app_user (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    username      VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    photo_url     VARCHAR(500),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    organisation_id BINARY(16),
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_email_tenant (email, tenant_id),
    INDEX idx_user_tenant (tenant_id),
    INDEX idx_user_deleted (deleted),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── User ↔ Role (M:M) ──
CREATE TABLE user_role (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Villes ──
CREATE TABLE city (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    name          VARCHAR(255) NOT NULL,
    region        VARCHAR(255),
    country       VARCHAR(100) DEFAULT 'Maroc',
    latitude      DOUBLE,
    longitude     DOUBLE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_city_tenant (tenant_id),
    INDEX idx_city_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Organisation ↔ City (M:N) ──
CREATE TABLE organisation_city (
    organisation_id BINARY(16) NOT NULL,
    city_id         BINARY(16) NOT NULL,
    PRIMARY KEY (organisation_id, city_id),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE CASCADE,
    FOREIGN KEY (city_id) REFERENCES city(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Marques ──
CREATE TABLE brand (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    name          VARCHAR(100) NOT NULL,
    slug          VARCHAR(100),
    logo_url      VARCHAR(500),
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_brand_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Catégories ──
CREATE TABLE category (
    id                    BINARY(16)   NOT NULL,
    tenant_id             BINARY(16)   NOT NULL,
    name                  VARCHAR(100) NOT NULL,
    description           VARCHAR(500),
    daily_rate_multiplier DOUBLE       NOT NULL DEFAULT 1.0,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_category_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Modèles de véhicules ──
CREATE TABLE vehicle_model (
    id                BINARY(16)   NOT NULL,
    tenant_id         BINARY(16)   NOT NULL,
    name              VARCHAR(100) NOT NULL,
    horse_power       INT,
    door_count        INT,
    seat_count        INT,
    trunk_volume      INT,
    fuel_type         VARCHAR(30),
    transmission_type VARCHAR(20),
    year              INT,
    brand_id          BINARY(16)   NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_vmodel_tenant (tenant_id),
    FOREIGN KEY (brand_id) REFERENCES brand(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Véhicules ──
CREATE TABLE vehicle (
    id             BINARY(16)   NOT NULL,
    tenant_id      BINARY(16)   NOT NULL,
    license_plate  VARCHAR(20)  NOT NULL,
    color          VARCHAR(50),
    mileage        DOUBLE       NOT NULL DEFAULT 0,
    daily_rate     DOUBLE       NOT NULL,
    photo_url      VARCHAR(500),
    status         VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE',
    vehicle_model_id BINARY(16) NOT NULL,
    category_id    BINARY(16)   NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_vehicle_plate_tenant (license_plate, tenant_id),
    INDEX idx_vehicle_tenant (tenant_id),
    INDEX idx_vehicle_status (status),
    FOREIGN KEY (vehicle_model_id) REFERENCES vehicle_model(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Historique statuts véhicule ──
CREATE TABLE vehicle_status_history (
    id              BINARY(16)  NOT NULL,
    tenant_id       BINARY(16)  NOT NULL,
    vehicle_id      BINARY(16)  NOT NULL,
    previous_status VARCHAR(30),
    new_status      VARCHAR(30) NOT NULL,
    change_date     DATETIME    NOT NULL,
    comment         VARCHAR(500),
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_vsh_tenant (tenant_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Clients ──
CREATE TABLE client (
    id              BINARY(16)   NOT NULL,
    tenant_id       BINARY(16)   NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    cin             VARCHAR(30),
    password        VARCHAR(255),
    address         VARCHAR(500),
    license_number  VARCHAR(50),
    license_expiry  DATE,
    date_of_birth   DATE,
    photo_url       VARCHAR(500),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_client_tenant (tenant_id),
    INDEX idx_client_cin (cin),
    INDEX idx_client_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Assurances ──
CREATE TABLE insurance (
    id            BINARY(16)   NOT NULL,
    tenant_id     BINARY(16)   NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    coverage_type VARCHAR(100),
    daily_rate    DOUBLE       NOT NULL,
    provider      VARCHAR(255),
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_insurance_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Locations (rentals) ──
CREATE TABLE rental (
    id                 BINARY(16)  NOT NULL,
    tenant_id          BINARY(16)  NOT NULL,
    start_date         DATE        NOT NULL,
    end_date           DATE        NOT NULL,
    actual_return_date DATE,
    mileage_before     DOUBLE,
    mileage_after      DOUBLE,
    deposit            DOUBLE      DEFAULT 0,
    total_amount       DOUBLE      DEFAULT 0,
    status             VARCHAR(30) NOT NULL DEFAULT 'RESERVED',
    notes              TEXT,
    vehicle_id         BINARY(16)  NOT NULL,
    client_id          BINARY(16)  NOT NULL,
    insurance_id       BINARY(16),
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted            BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_rental_tenant (tenant_id),
    INDEX idx_rental_status (status),
    INDEX idx_rental_dates (start_date, end_date),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    FOREIGN KEY (client_id) REFERENCES client(id),
    FOREIGN KEY (insurance_id) REFERENCES insurance(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Fiches d'état (inspection reports) ──
CREATE TABLE inspection_report (
    id                        BINARY(16)  NOT NULL,
    tenant_id                 BINARY(16)  NOT NULL,
    type                      VARCHAR(20) NOT NULL,
    fuel_level                DOUBLE,
    mileage                   DOUBLE,
    exterior_front_scratches  BOOLEAN DEFAULT FALSE,
    exterior_front_dents      BOOLEAN DEFAULT FALSE,
    exterior_rear_scratches   BOOLEAN DEFAULT FALSE,
    exterior_rear_dents       BOOLEAN DEFAULT FALSE,
    exterior_left_scratches   BOOLEAN DEFAULT FALSE,
    exterior_left_dents       BOOLEAN DEFAULT FALSE,
    exterior_right_scratches  BOOLEAN DEFAULT FALSE,
    exterior_right_dents      BOOLEAN DEFAULT FALSE,
    interior_condition        VARCHAR(50),
    tire_condition            VARCHAR(50),
    comments                  TEXT,
    photos                    JSON,
    client_signature          TEXT,
    agent_signature           TEXT,
    rental_id                 BINARY(16) NOT NULL,
    agent_id                  BINARY(16),
    created_at                DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                DATETIME            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                   BOOLEAN    NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_ir_tenant (tenant_id),
    FOREIGN KEY (rental_id) REFERENCES rental(id),
    FOREIGN KEY (agent_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Factures ──
CREATE TABLE invoice (
    id             BINARY(16)   NOT NULL,
    tenant_id      BINARY(16)   NOT NULL,
    invoice_number VARCHAR(30)  NOT NULL,
    subtotal       DOUBLE       NOT NULL DEFAULT 0,
    tax_rate       DOUBLE       NOT NULL DEFAULT 20.0,
    tax_amount     DOUBLE       NOT NULL DEFAULT 0,
    discount       DOUBLE       DEFAULT 0,
    total_amount   DOUBLE       NOT NULL DEFAULT 0,
    status         VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    due_date       DATE,
    paid_date      DATE,
    rental_id      BINARY(16)   NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_invoice_number_tenant (invoice_number, tenant_id),
    INDEX idx_invoice_tenant (tenant_id),
    INDEX idx_invoice_status (status),
    FOREIGN KEY (rental_id) REFERENCES rental(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Lignes de facture ──
CREATE TABLE invoice_line (
    id          BINARY(16)   NOT NULL,
    tenant_id   BINARY(16)   NOT NULL,
    label       VARCHAR(255) NOT NULL,
    quantity    DOUBLE       NOT NULL DEFAULT 1,
    unit_price  DOUBLE       NOT NULL,
    total_price DOUBLE       NOT NULL,
    line_type   VARCHAR(30)  NOT NULL,
    invoice_id  BINARY(16)   NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_il_tenant (tenant_id),
    FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Paiements ──
CREATE TABLE payment (
    id              BINARY(16)  NOT NULL,
    tenant_id       BINARY(16)  NOT NULL,
    amount          DOUBLE      NOT NULL,
    payment_date    DATETIME    NOT NULL,
    payment_method  VARCHAR(30) NOT NULL,
    transaction_ref VARCHAR(100),
    notes           VARCHAR(500),
    invoice_id      BINARY(16)  NOT NULL,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_payment_tenant (tenant_id),
    FOREIGN KEY (invoice_id) REFERENCES invoice(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Réclamations ──
CREATE TABLE claim (
    id          BINARY(16)   NOT NULL,
    tenant_id   BINARY(16)   NOT NULL,
    subject     VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    resolution  TEXT,
    resolved_at DATETIME,
    client_id   BINARY(16)   NOT NULL,
    rental_id   BINARY(16),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_claim_tenant (tenant_id),
    INDEX idx_claim_status (status),
    FOREIGN KEY (client_id) REFERENCES client(id),
    FOREIGN KEY (rental_id) REFERENCES rental(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
