-- Reservation table (separate from Rental)
CREATE TABLE reservation (
    id                    BINARY(16)   NOT NULL,
    tenant_id             BINARY(16)   NOT NULL,
    client_id             BINARY(16)   NOT NULL,
    vehicle_id            BINARY(16)   NOT NULL,
    insurance_id          BINARY(16),
    start_date            DATE         NOT NULL,
    end_date              DATE         NOT NULL,
    estimated_total       DECIMAL(19,2) DEFAULT 0.00,
    deposit_required      DECIMAL(19,2) DEFAULT 0.00,
    status                VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    notes                 TEXT,
    cancellation_reason   TEXT,
    cancelled_at          DATETIME,
    confirmed_at          DATETIME,
    converted_at          DATETIME,
    rental_id             BINARY(16),
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_reservation_tenant (tenant_id),
    INDEX idx_reservation_status (status),
    INDEX idx_reservation_client (client_id),
    INDEX idx_reservation_dates (start_date, end_date),
    FOREIGN KEY (client_id) REFERENCES client(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    FOREIGN KEY (insurance_id) REFERENCES insurance(id),
    FOREIGN KEY (rental_id) REFERENCES rental(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Enhance rental table
ALTER TABLE rental ADD COLUMN reservation_id BINARY(16) AFTER insurance_id;
ALTER TABLE rental ADD COLUMN delivered_by_agent_id BINARY(16) AFTER reservation_id;
ALTER TABLE rental ADD COLUMN delivered_at DATETIME AFTER delivered_by_agent_id;
ALTER TABLE rental ADD COLUMN returned_by_agent_id BINARY(16) AFTER delivered_at;
ALTER TABLE rental ADD COLUMN returned_at DATETIME AFTER returned_by_agent_id;
ALTER TABLE rental ADD COLUMN deposit_refund_amount DECIMAL(19,2) DEFAULT 0.00 AFTER returned_at;
ALTER TABLE rental ADD COLUMN damage_cost DECIMAL(19,2) DEFAULT 0.00 AFTER deposit_refund_amount;
ALTER TABLE rental ADD FOREIGN KEY (reservation_id) REFERENCES reservation(id);
