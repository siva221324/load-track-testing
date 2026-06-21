-- ============================================================
-- LoadTrack — PostgreSQL Schema (Neon) — Multi-Tenant v2
-- ============================================================
-- Apply this file against a FRESH Neon branch via DBeaver
-- (right-click connection → SQL Editor → Open SQL Script → Execute).
--
-- WARNING: This script drops existing tables. Safe to run on a fresh DB,
-- destructive on a populated one. For migrating an existing single-tenant
-- database, use docs/migration_multi_tenant.sql instead.
--
-- Design notes:
--   - Every business table has an organization_id FK (multi-tenant isolation)
--   - users.username is globally unique; truck_number / license_number /
--     sand_type.name are unique per-org (composite indexes)
--   - Money columns are NUMERIC(14,2) — never use FLOAT for currency
--   - rate_per_ton is snapshotted on the trip row so price changes don't
--     retroactively alter historical trip totals
--   - settings has UNIQUE(organization_id) — exactly one row per org
-- ============================================================

-- Drop in reverse-FK order
DROP TABLE IF EXISTS payment_transactions CASCADE;
DROP TABLE IF EXISTS receipts CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS trip_requests CASCADE;
DROP TABLE IF EXISTS trips CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS drivers CASCADE;
DROP TABLE IF EXISTS settings CASCADE;
DROP TABLE IF EXISTS sand_types CASCADE;
DROP TABLE IF EXISTS dealers CASCADE;
DROP TABLE IF EXISTS trucks CASCADE;
DROP TABLE IF EXISTS organizations CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- ============================================================
-- 0. organizations — multi-tenant root
-- ============================================================
CREATE TABLE organizations (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 1. roles — fixed lookup (global, not per-org)
-- ============================================================
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT chk_role_name CHECK (name IN ('ADMIN', 'DRIVER', 'DEALER'))
);

-- ============================================================
-- 2. trucks
-- ============================================================
CREATE TABLE trucks (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    truck_number        VARCHAR(20)  NOT NULL,
    model               VARCHAR(100) NOT NULL,
    capacity_tons       NUMERIC(8,2) NOT NULL CHECK (capacity_tons > 0),
    insurance_number    VARCHAR(50),
    rc_number           VARCHAR(50),
    status              VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_truck_status CHECK (status IN ('AVAILABLE', 'ON_TRIP', 'MAINTENANCE'))
);
CREATE UNIQUE INDEX uq_trucks_org_truck_number ON trucks (organization_id, truck_number);

-- ============================================================
-- 3. dealers
-- ============================================================
CREATE TABLE dealers (
    id              BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(15)  NOT NULL,
    address         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. sand_types
-- ============================================================
CREATE TABLE sand_types (
    id              BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(50)   NOT NULL,
    price_per_ton   NUMERIC(12,2) NOT NULL CHECK (price_per_ton > 0),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uq_sand_types_org_name ON sand_types (organization_id, lower(name));

-- ============================================================
-- 5. settings — exactly one row per org
-- ============================================================
CREATE TABLE settings (
    id                      BIGSERIAL PRIMARY KEY,
    organization_id         BIGINT NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    interest_rate_percent   NUMERIC(5,2) NOT NULL CHECK (interest_rate_percent >= 0),
    allowed_days            INTEGER      NOT NULL CHECK (allowed_days > 0),
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. drivers
-- ============================================================
CREATE TABLE drivers (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(15)  NOT NULL,
    license_number      VARCHAR(30)  NOT NULL,
    address             TEXT,
    salary_per_trip     NUMERIC(10,2) NOT NULL CHECK (salary_per_trip >= 0),
    assigned_truck_id   BIGINT REFERENCES trucks(id) ON DELETE SET NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uq_drivers_org_license_number ON drivers (organization_id, license_number);
CREATE INDEX idx_drivers_assigned_truck ON drivers(assigned_truck_id);

-- ============================================================
-- 7. users — authentication; admins linked to org directly,
--    driver/dealer logins inherit via linked_driver_id / linked_dealer_id
-- ============================================================
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL UNIQUE,  -- globally unique
    password            VARCHAR(255) NOT NULL,
    role_id             BIGINT       NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    organization_id     BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    linked_driver_id    BIGINT REFERENCES drivers(id)  ON DELETE SET NULL,
    linked_dealer_id    BIGINT REFERENCES dealers(id)  ON DELETE SET NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_user_link   CHECK (linked_driver_id IS NULL OR linked_dealer_id IS NULL)
);
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_org  ON users(organization_id);

-- ============================================================
-- 8. trips
-- ============================================================
CREATE TABLE trips (
    id                      BIGSERIAL PRIMARY KEY,
    organization_id         BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    truck_id                BIGINT NOT NULL REFERENCES trucks(id)     ON DELETE RESTRICT,
    driver_id               BIGINT NOT NULL REFERENCES drivers(id)    ON DELETE RESTRICT,
    dealer_id               BIGINT NOT NULL REFERENCES dealers(id)    ON DELETE RESTRICT,
    sand_type_id            BIGINT NOT NULL REFERENCES sand_types(id) ON DELETE RESTRICT,
    tons                    NUMERIC(8,2)  NOT NULL CHECK (tons > 0),
    source_location         VARCHAR(200) NOT NULL,
    destination_location    VARCHAR(200) NOT NULL,
    trip_date               DATE         NOT NULL,
    rate_per_ton            NUMERIC(12,2) NOT NULL CHECK (rate_per_ton > 0),
    total_amount            NUMERIC(14,2) NOT NULL CHECK (total_amount > 0),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_trip_status CHECK (status IN ('PENDING', 'STARTED', 'COMPLETED'))
);
CREATE INDEX idx_trips_org       ON trips(organization_id);
CREATE INDEX idx_trips_truck     ON trips(truck_id);
CREATE INDEX idx_trips_driver    ON trips(driver_id);
CREATE INDEX idx_trips_dealer    ON trips(dealer_id);
CREATE INDEX idx_trips_sand_type ON trips(sand_type_id);
CREATE INDEX idx_trips_date      ON trips(trip_date);
CREATE INDEX idx_trips_status    ON trips(status);

-- ============================================================
-- 9. payments
-- ============================================================
CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    organization_id     BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    trip_id             BIGINT NOT NULL UNIQUE REFERENCES trips(id) ON DELETE CASCADE,
    original_amount     NUMERIC(14,2) NOT NULL CHECK (original_amount > 0),
    interest_amount     NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (interest_amount >= 0),
    final_amount        NUMERIC(14,2) NOT NULL CHECK (final_amount > 0),
    paid_amount         NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    payment_status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    payment_date        TIMESTAMP,
    due_date            DATE NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('PENDING', 'PARTIAL', 'PAID'))
);
CREATE INDEX idx_payments_org      ON payments(organization_id);
CREATE INDEX idx_payments_status   ON payments(payment_status);
CREATE INDEX idx_payments_due_date ON payments(due_date);

-- ============================================================
-- 10. payment_transactions — installment history
-- ============================================================
CREATE TABLE payment_transactions (
    id          BIGSERIAL PRIMARY KEY,
    payment_id  BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    amount      NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    paid_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_payment_transactions_payment ON payment_transactions(payment_id);

-- ============================================================
-- 11. receipts
-- ============================================================
CREATE TABLE receipts (
    id              BIGSERIAL PRIMARY KEY,
    payment_id      BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    receipt_number  VARCHAR(30) NOT NULL UNIQUE,
    generated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pdf_path        TEXT
);
CREATE INDEX idx_receipts_payment ON receipts(payment_id);

-- ============================================================
-- 12. trip_requests — dealer-initiated, admin-approved
-- ============================================================
CREATE TABLE trip_requests (
    id                      BIGSERIAL PRIMARY KEY,
    organization_id         BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    dealer_id               BIGINT NOT NULL REFERENCES dealers(id)    ON DELETE CASCADE,
    sand_type_id            BIGINT NOT NULL REFERENCES sand_types(id) ON DELETE RESTRICT,
    tons                    NUMERIC(8,2)  NOT NULL CHECK (tons > 0),
    source_location         VARCHAR(200) NOT NULL,
    destination_location    VARCHAR(200) NOT NULL,
    requested_date          DATE NOT NULL,
    notes                   TEXT,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    admin_notes             TEXT,
    approved_trip_id        BIGINT REFERENCES trips(id) ON DELETE SET NULL,
    responded_at            TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_trip_req_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);
CREATE INDEX idx_trip_req_org    ON trip_requests(organization_id);
CREATE INDEX idx_trip_req_dealer ON trip_requests(dealer_id);
CREATE INDEX idx_trip_req_status ON trip_requests(status);

-- ============================================================
-- SEED DATA — roles only (orgs created by signup or migration)
-- ============================================================
INSERT INTO roles (name) VALUES ('ADMIN'), ('DRIVER'), ('DEALER');

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' ORDER BY table_name;
SELECT * FROM roles;
