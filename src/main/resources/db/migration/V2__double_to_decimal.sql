-- =====================================================
-- V2 : Migrate DOUBLE columns to DECIMAL(19,2) for financial precision
-- =====================================================

-- ── Category ──
ALTER TABLE category MODIFY daily_rate_multiplier DECIMAL(19,2) NOT NULL DEFAULT 1.00;

-- ── Vehicle ──
ALTER TABLE vehicle MODIFY mileage DECIMAL(19,2) NOT NULL DEFAULT 0.00;
ALTER TABLE vehicle MODIFY daily_rate DECIMAL(19,2) NOT NULL;

-- ── Insurance ──
ALTER TABLE insurance MODIFY daily_rate DECIMAL(19,2) NOT NULL;

-- ── Rental ──
ALTER TABLE rental MODIFY mileage_before DECIMAL(19,2);
ALTER TABLE rental MODIFY mileage_after DECIMAL(19,2);
ALTER TABLE rental MODIFY deposit DECIMAL(19,2) DEFAULT 0.00;
ALTER TABLE rental MODIFY total_amount DECIMAL(19,2) DEFAULT 0.00;

-- ── Inspection Report ──
ALTER TABLE inspection_report MODIFY fuel_level DECIMAL(5,2);
ALTER TABLE inspection_report MODIFY mileage DECIMAL(19,2);

-- ── Invoice ──
ALTER TABLE invoice MODIFY subtotal DECIMAL(19,2) NOT NULL DEFAULT 0.00;
ALTER TABLE invoice MODIFY tax_rate DECIMAL(5,2) NOT NULL DEFAULT 20.00;
ALTER TABLE invoice MODIFY tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00;
ALTER TABLE invoice MODIFY discount DECIMAL(19,2) DEFAULT 0.00;
ALTER TABLE invoice MODIFY total_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00;

-- ── Invoice Line ──
ALTER TABLE invoice_line MODIFY quantity DECIMAL(19,2) NOT NULL DEFAULT 1.00;
ALTER TABLE invoice_line MODIFY unit_price DECIMAL(19,2) NOT NULL;
ALTER TABLE invoice_line MODIFY total_price DECIMAL(19,2) NOT NULL;

-- ── Payment ──
ALTER TABLE payment MODIFY amount DECIMAL(19,2) NOT NULL;

-- ── City (geographic, keep DOUBLE) ──
-- latitude/longitude remain DOUBLE as they are not financial
