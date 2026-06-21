-- ============================================================
-- LoadTrack — Multi-Tenancy Migration
-- ============================================================
-- This script adds the `organizations` table and `organization_id` FK
-- columns to every business table. It also creates a "Default Fleet"
-- organization and assigns all existing rows to it, so the new code
-- (which expects every row to have an organization_id) can run cleanly.
--
-- Apply this BEFORE deploying the new multi-tenant backend.
-- Apply it once per database (dev branch, then production branch).
--
-- Order of operations:
--   1. Create organizations table
--   2. Insert Default Fleet
--   3. Add NULLABLE organization_id columns to each table
--   4. Backfill all rows with the Default Fleet id
--   5. Make the columns NOT NULL
--   6. Drop the now-obsolete global UNIQUE constraints on
--      truck_number / license_number / sand_types.name
--      and replace them with composite uniqueness (org_id, value)
-- ============================================================

BEGIN;

-- 1. organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Default org
INSERT INTO organizations (name)
SELECT 'Default Fleet'
WHERE NOT EXISTS (SELECT 1 FROM organizations WHERE name = 'Default Fleet');

-- Grab its ID
DO $$
DECLARE
    default_org_id BIGINT;
BEGIN
    SELECT id INTO default_org_id FROM organizations WHERE name = 'Default Fleet';

    -- 3 & 4. Add nullable organization_id to each table, backfill, then set NOT NULL
    -- users
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='users' AND column_name='organization_id') THEN
        ALTER TABLE users ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    -- only admin users should be assigned to the default org; driver/dealer logins inherit via their link
    UPDATE users SET organization_id = default_org_id
     WHERE organization_id IS NULL AND linked_driver_id IS NULL AND linked_dealer_id IS NULL;

    -- trucks
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='trucks' AND column_name='organization_id') THEN
        ALTER TABLE trucks ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE trucks SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE trucks ALTER COLUMN organization_id SET NOT NULL;

    -- drivers
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='drivers' AND column_name='organization_id') THEN
        ALTER TABLE drivers ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE drivers SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE drivers ALTER COLUMN organization_id SET NOT NULL;

    -- dealers
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='dealers' AND column_name='organization_id') THEN
        ALTER TABLE dealers ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE dealers SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE dealers ALTER COLUMN organization_id SET NOT NULL;

    -- sand_types
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='sand_types' AND column_name='organization_id') THEN
        ALTER TABLE sand_types ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE sand_types SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE sand_types ALTER COLUMN organization_id SET NOT NULL;

    -- settings (one row per org)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='settings' AND column_name='organization_id') THEN
        ALTER TABLE settings ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE settings SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE settings ALTER COLUMN organization_id SET NOT NULL;
    -- enforce uniqueness so we don't accidentally end up with 2 settings rows per org
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conname = 'settings_organization_id_unique'
    ) THEN
        ALTER TABLE settings ADD CONSTRAINT settings_organization_id_unique UNIQUE (organization_id);
    END IF;

    -- trips
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='trips' AND column_name='organization_id') THEN
        ALTER TABLE trips ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE trips SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE trips ALTER COLUMN organization_id SET NOT NULL;

    -- payments
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='payments' AND column_name='organization_id') THEN
        ALTER TABLE payments ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
    UPDATE payments SET organization_id = default_org_id WHERE organization_id IS NULL;
    ALTER TABLE payments ALTER COLUMN organization_id SET NOT NULL;

    -- trip_requests (may not exist on old deployments)
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='trip_requests') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name='trip_requests' AND column_name='organization_id') THEN
            ALTER TABLE trip_requests ADD COLUMN organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
        END IF;
        UPDATE trip_requests SET organization_id = default_org_id WHERE organization_id IS NULL;
        ALTER TABLE trip_requests ALTER COLUMN organization_id SET NOT NULL;
    END IF;
END $$;

-- 6. Drop global UNIQUE constraints and add composite (org, X) UNIQUE indexes
-- truck_number
ALTER TABLE trucks DROP CONSTRAINT IF EXISTS trucks_truck_number_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_trucks_org_truck_number
    ON trucks (organization_id, truck_number);

-- license_number
ALTER TABLE drivers DROP CONSTRAINT IF EXISTS drivers_license_number_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_drivers_org_license_number
    ON drivers (organization_id, license_number);

-- sand_types.name
ALTER TABLE sand_types DROP CONSTRAINT IF EXISTS sand_types_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_sand_types_org_name
    ON sand_types (organization_id, lower(name));

COMMIT;

-- ============================================================
-- Verification — should all return non-null counts after migration
-- ============================================================
SELECT 'organizations' AS tbl, count(*) FROM organizations
UNION ALL SELECT 'users (with org)', count(*) FROM users WHERE organization_id IS NOT NULL
UNION ALL SELECT 'trucks', count(*) FROM trucks WHERE organization_id IS NOT NULL
UNION ALL SELECT 'drivers', count(*) FROM drivers WHERE organization_id IS NOT NULL
UNION ALL SELECT 'dealers', count(*) FROM dealers WHERE organization_id IS NOT NULL
UNION ALL SELECT 'sand_types', count(*) FROM sand_types WHERE organization_id IS NOT NULL
UNION ALL SELECT 'settings', count(*) FROM settings WHERE organization_id IS NOT NULL
UNION ALL SELECT 'trips', count(*) FROM trips WHERE organization_id IS NOT NULL
UNION ALL SELECT 'payments', count(*) FROM payments WHERE organization_id IS NOT NULL;
