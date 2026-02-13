-- ========================================
-- ASHA Smart Care - Corrected Schema Fix
-- Date: February 6, 2026
-- ========================================

USE asha_smartcare;

-- ========================================
-- 1. FIX PATIENTS TABLE
-- ========================================

-- Drop unnecessary columns (keep address, remove village/district/state/emergency_contact)
ALTER TABLE patients DROP COLUMN IF EXISTS village;
ALTER TABLE patients DROP COLUMN IF EXISTS district;
ALTER TABLE patients DROP COLUMN IF EXISTS state;
ALTER TABLE patients DROP COLUMN IF EXISTS emergency_contact;

-- Fix category ENUM to include proper values
ALTER TABLE patients MODIFY COLUMN category ENUM('Pregnant Woman','Lactating Mother','Child (0-5 yrs)','Adolescent Girl','Adult','General') DEFAULT 'General';

-- Update any existing 'Child (0-5)' to 'Child (0-5 yrs)'
UPDATE patients SET category = 'Child (0-5 yrs)' WHERE category = 'Child (0-5)';

-- ========================================
-- 2. FIX PREGNANCY_VISITS TABLE
-- ========================================

-- Check current structure first
SELECT '=== Pregnancy Visits Current Structure ===' as info;
DESCRIBE pregnancy_visits;

-- Add missing columns if they don't exist
SET @query = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'asha_smartcare' 
     AND TABLE_NAME = 'pregnancy_visits' 
     AND COLUMN_NAME = 'visit_type') = 0,
    'ALTER TABLE pregnancy_visits ADD COLUMN visit_type VARCHAR(50) DEFAULT ''Follow-up''',
    'SELECT ''visit_type exists'' as msg'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'asha_smartcare' 
     AND TABLE_NAME = 'pregnancy_visits' 
     AND COLUMN_NAME = 'notes') = 0,
    'ALTER TABLE pregnancy_visits ADD COLUMN notes TEXT DEFAULT NULL',
    'SELECT ''notes exists'' as msg'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'asha_smartcare' 
     AND TABLE_NAME = 'pregnancy_visits' 
     AND COLUMN_NAME = 'sync_status') = 0,
    'ALTER TABLE pregnancy_visits ADD COLUMN sync_status VARCHAR(20) DEFAULT ''SYNCED''',
    'SELECT ''sync_status exists'' as msg'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 3. FIX VISITS TABLE
-- ========================================

SET @query = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'asha_smartcare' 
     AND TABLE_NAME = 'visits' 
     AND COLUMN_NAME = 'medicines_prescribed') = 0,
    'ALTER TABLE visits ADD COLUMN medicines_prescribed TEXT DEFAULT NULL',
    'SELECT ''medicines_prescribed exists'' as msg'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'asha_smartcare' 
     AND TABLE_NAME = 'visits' 
     AND COLUMN_NAME = 'sync_status') = 0,
    'ALTER TABLE visits ADD COLUMN sync_status VARCHAR(20) DEFAULT ''SYNCED''',
    'SELECT ''sync_status exists'' as msg'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 4. VERIFY FINAL STRUCTURE
-- ========================================

SELECT '=== PATIENTS TABLE (Final) ===' as info;
DESCRIBE patients;

SELECT '=== PREGNANCY_VISITS TABLE (Final) ===' as info;
DESCRIBE pregnancy_visits;

SELECT '=== VISITS TABLE (Final) ===' as info;
DESCRIBE visits;

SELECT '=== Patient Categories Distribution ===' as info;
SELECT category, COUNT(*) as count FROM patients GROUP BY category;

SELECT '=== MIGRATION COMPLETE ===' as info;
