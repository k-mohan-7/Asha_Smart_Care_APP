-- ========================================
-- ASHA Smart Care Database Schema Fix
-- Date: February 6, 2026
-- ========================================

USE asha_smartcare;

-- BACKUP NOTE: Run this first:
-- mysqldump -u root asha_smartcare > backup_before_migration.sql

-- ========================================
-- 1. ALTER PATIENTS TABLE
-- ========================================

-- Add blood_group column
ALTER TABLE patients ADD COLUMN IF NOT EXISTS blood_group VARCHAR(10) DEFAULT NULL;

-- Rename village to address (MySQL 8.0+)
ALTER TABLE patients CHANGE COLUMN village address TEXT;

-- Drop unnecessary columns
ALTER TABLE patients DROP COLUMN IF EXISTS district;
ALTER TABLE patients DROP COLUMN IF EXISTS state;
ALTER TABLE patients DROP COLUMN IF EXISTS emergency_contact;

-- Update existing category values to standardized format
UPDATE patients SET category = 'Pregnant Woman' WHERE category IN ('pregnant', 'pregn', 'Pregnant');
UPDATE patients SET category = 'Lactating Mother' WHERE category IN ('lactating', 'mother', 'Lactating');
UPDATE patients SET category = 'Child (0-5 yrs)' WHERE category IN ('child', 'infant', 'Child');
UPDATE patients SET category = 'Adult' WHERE category IN ('adult', 'general_adult', 'General Adult');
UPDATE patients SET category = 'General' WHERE category IN ('general', 'other', 'Others');

-- ========================================
-- 2. ALTER PREGNANCY_VISITS TABLE
-- ========================================

-- Add missing columns if they don't exist
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS visit_type VARCHAR(50) DEFAULT 'Follow-up';
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS sync_status VARCHAR(20) DEFAULT 'SYNCED';
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS lmp_date DATE DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS edd DATE DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS blood_pressure VARCHAR(20) DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS hemoglobin DECIMAL(4,2) DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS danger_signs TEXT DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS medicines TEXT DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN IF NOT EXISTS next_visit_date DATE DEFAULT NULL;

-- ========================================
-- 3. ALTER VISITS TABLE
-- ========================================

-- Add medicines column
ALTER TABLE visits ADD COLUMN IF NOT EXISTS medicines_prescribed TEXT DEFAULT NULL;
ALTER TABLE visits ADD COLUMN IF NOT EXISTS sync_status VARCHAR(20) DEFAULT 'SYNCED';
ALTER TABLE visits ADD COLUMN IF NOT EXISTS next_visit_date DATE DEFAULT NULL;
ALTER TABLE visits ADD COLUMN IF NOT EXISTS findings TEXT DEFAULT NULL;

-- ========================================
-- 4. VERIFY CHANGES
-- ========================================

-- Check patients table structure
SELECT 'Patients table columns:' as info;
SHOW COLUMNS FROM patients;

-- Check pregnancy_visits table structure  
SELECT 'Pregnancy visits table columns:' as info;
SHOW COLUMNS FROM pregnancy_visits;

-- Check visits table structure
SELECT 'Visits table columns:' as info;
SHOW COLUMNS FROM visits;

-- Count records per category
SELECT 'Patient categories distribution:' as info;
SELECT category, COUNT(*) as count FROM patients GROUP BY category;

-- ========================================
-- MIGRATION COMPLETE
-- ========================================
