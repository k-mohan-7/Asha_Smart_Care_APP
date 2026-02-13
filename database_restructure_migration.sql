-- ===================================================================
-- Database Restructuring Migration for ASHASmartCare
-- ===================================================================
-- Purpose: Reorganize tables based on patient categories
-- Categories: Pregnant Woman, Child (0-5 yrs), General Adult
-- 
-- Changes:
-- 1. Create 'pregnancy' table for pregnancy metadata (LMP, EDD, etc.)
-- 2. Enhance 'visits' table to store ALL visit types (pregnancy, child, general)
-- 3. Migrate data from 'pregnancy_visits' to new structure
-- 4. Keep 'child_growth' for child growth tracking
-- ===================================================================

USE asha_smartcare;

-- ===================================================================
-- STEP 1: Create new 'pregnancy' table for pregnancy metadata
-- ===================================================================
CREATE TABLE IF NOT EXISTS `pregnancy` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `patient_id` INT(11) NOT NULL,
  `asha_id` INT(11) DEFAULT NULL,
  `lmp_date` DATE DEFAULT NULL COMMENT 'Last Menstrual Period date',
  `edd_date` DATE DEFAULT NULL COMMENT 'Expected Delivery Date',
  `gravida` INT(11) DEFAULT 1 COMMENT 'Number of pregnancies',
  `para` INT(11) DEFAULT 0 COMMENT 'Number of deliveries',
  `abortion` INT(11) DEFAULT 0 COMMENT 'Number of abortions',
  `living_children` INT(11) DEFAULT 0 COMMENT 'Number of living children',
  `pregnancy_status` ENUM('Active', 'Delivered', 'Terminated') DEFAULT 'Active',
  `pregnancy_outcome` VARCHAR(100) DEFAULT NULL COMMENT 'Delivery outcome if completed',
  `delivery_date` DATE DEFAULT NULL,
  `is_high_risk` TINYINT(1) DEFAULT 0,
  `high_risk_reason` TEXT DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `sync_status` VARCHAR(20) DEFAULT 'SYNCED',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_asha_id` (`asha_id`),
  KEY `idx_status` (`pregnancy_status`),
  FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`asha_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Pregnancy metadata - one record per pregnancy';

-- ===================================================================
-- STEP 2: Enhance 'visits' table to support all visit types
-- ===================================================================

-- Add pregnancy-specific columns
ALTER TABLE `visits` 
  ADD COLUMN IF NOT EXISTS `pregnancy_id` INT(11) DEFAULT NULL COMMENT 'Link to pregnancy table',
  ADD COLUMN IF NOT EXISTS `gestational_weeks` INT(11) DEFAULT NULL COMMENT 'Pregnancy weeks',
  ADD COLUMN IF NOT EXISTS `weight` DECIMAL(5,2) DEFAULT NULL COMMENT 'Weight in kg',
  ADD COLUMN IF NOT EXISTS `blood_pressure` VARCHAR(20) DEFAULT NULL COMMENT 'BP reading',
  ADD COLUMN IF NOT EXISTS `hemoglobin` DECIMAL(4,2) DEFAULT NULL COMMENT 'Hb level',
  ADD COLUMN IF NOT EXISTS `fetal_heart_rate` INT(11) DEFAULT NULL COMMENT 'FHR in bpm',
  ADD COLUMN IF NOT EXISTS `urine_protein` VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `urine_sugar` VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `fundal_height` DECIMAL(5,2) DEFAULT NULL COMMENT 'Fundal height in cm',
  ADD COLUMN IF NOT EXISTS `presentation` VARCHAR(50) DEFAULT NULL COMMENT 'Fetal presentation',
  ADD COLUMN IF NOT EXISTS `ifa_tablets_given` INT(11) DEFAULT 0 COMMENT 'Iron tablets count',
  ADD COLUMN IF NOT EXISTS `tt_injection_given` TINYINT(1) DEFAULT 0 COMMENT 'Tetanus toxoid',
  ADD COLUMN IF NOT EXISTS `calcium_tablets_given` INT(11) DEFAULT 0 COMMENT 'Calcium tablets count';

-- Add general vitals columns (for all visit types)
ALTER TABLE `visits`
  ADD COLUMN IF NOT EXISTS `temperature` DECIMAL(4,2) DEFAULT NULL COMMENT 'Temperature in °C',
  ADD COLUMN IF NOT EXISTS `pulse_rate` INT(11) DEFAULT NULL COMMENT 'Pulse in bpm',
  ADD COLUMN IF NOT EXISTS `respiratory_rate` INT(11) DEFAULT NULL COMMENT 'Respiration per minute',
  ADD COLUMN IF NOT EXISTS `oxygen_saturation` INT(11) DEFAULT NULL COMMENT 'SpO2 percentage';

-- Add danger signs column
ALTER TABLE `visits`
  ADD COLUMN IF NOT EXISTS `danger_signs` TEXT DEFAULT NULL COMMENT 'Comma-separated danger signs';

-- Add foreign key for pregnancy_id
ALTER TABLE `visits`
  ADD KEY IF NOT EXISTS `idx_pregnancy_id` (`pregnancy_id`);

-- ===================================================================
-- STEP 3: Migrate data from pregnancy_visits to new structure
-- ===================================================================

-- First, create pregnancy records from existing pregnancy_visits data
-- Extract unique patient pregnancies and create pregnancy metadata
INSERT INTO `pregnancy` (
  `patient_id`, 
  `asha_id`, 
  `is_high_risk`, 
  `high_risk_reason`,
  `pregnancy_status`,
  `created_at`,
  `updated_at`
)
SELECT DISTINCT
  pv.patient_id,
  pv.asha_id,
  MAX(pv.is_high_risk) as is_high_risk,
  MAX(pv.high_risk_reason) as high_risk_reason,
  'Active' as pregnancy_status,
  MIN(pv.created_at) as created_at,
  MAX(pv.updated_at) as updated_at
FROM `pregnancy_visits` pv
WHERE pv.patient_id IN (SELECT id FROM patients WHERE category = 'Pregnant Woman')
GROUP BY pv.patient_id, pv.asha_id;

-- Now migrate pregnancy visit records to visits table
INSERT INTO `visits` (
  `patient_id`,
  `asha_id`,
  `visit_type`,
  `visit_date`,
  `pregnancy_id`,
  `gestational_weeks`,
  `weight`,
  `blood_pressure`,
  `hemoglobin`,
  `fetal_heart_rate`,
  `urine_protein`,
  `urine_sugar`,
  `fundal_height`,
  `presentation`,
  `ifa_tablets_given`,
  `tt_injection_given`,
  `findings`,
  `notes`,
  `danger_signs`,
  `sync_status`,
  `created_at`,
  `updated_at`
)
SELECT 
  pv.patient_id,
  pv.asha_id,
  IFNULL(pv.visit_type, 'Pregnancy Visit') as visit_type,
  pv.visit_date,
  p.id as pregnancy_id,
  pv.gestational_weeks,
  pv.weight,
  pv.blood_pressure,
  pv.hemoglobin,
  pv.fetal_heart_rate,
  pv.urine_protein,
  pv.urine_sugar,
  pv.fundal_height,
  pv.presentation,
  pv.ifa_tablets_given,
  pv.tt_injection_given,
  '' as findings,
  pv.notes,
  pv.high_risk_reason as danger_signs,
  pv.sync_status,
  pv.created_at,
  pv.updated_at
FROM `pregnancy_visits` pv
LEFT JOIN `pregnancy` p ON p.patient_id = pv.patient_id
ORDER BY pv.visit_date ASC;

-- ===================================================================
-- STEP 4: Backup and drop old pregnancy_visits table
-- ===================================================================

-- Create backup of pregnancy_visits (optional - for safety)
CREATE TABLE IF NOT EXISTS `pregnancy_visits_backup` LIKE `pregnancy_visits`;
INSERT INTO `pregnancy_visits_backup` SELECT * FROM `pregnancy_visits`;

-- Drop old pregnancy_visits table
DROP TABLE IF EXISTS `pregnancy_visits`;

-- ===================================================================
-- STEP 5: Update patient categories to ensure consistency
-- ===================================================================

-- Ensure all patients have valid categories
UPDATE `patients` 
SET `category` = 'General' 
WHERE `category` IS NULL OR `category` = '';

-- ===================================================================
-- STEP 6: Create indexes for better performance
-- ===================================================================

-- Index on visits.visit_type for faster filtering
ALTER TABLE `visits` ADD INDEX IF NOT EXISTS `idx_visit_type` (`visit_type`);

-- Index on visits.visit_date for date range queries
ALTER TABLE `visits` ADD INDEX IF NOT EXISTS `idx_visit_date` (`visit_date`);

-- ===================================================================
-- STEP 7: Verify data integrity
-- ===================================================================

-- Check pregnancy records created
SELECT 'Pregnancy records created:' as Status, COUNT(*) as Count FROM pregnancy;

-- Check visit records migrated
SELECT 'Visit records in visits table:' as Status, COUNT(*) as Count FROM visits WHERE visit_type LIKE '%Pregnancy%' OR visit_type LIKE '%pregnancy%';

-- Check child growth records
SELECT 'Child growth records:' as Status, COUNT(*) as Count FROM child_growth;

-- Check patients by category
SELECT category, COUNT(*) as patient_count FROM patients GROUP BY category;

-- ===================================================================
-- Migration Complete
-- ===================================================================

SELECT '✓ Database restructuring completed successfully!' as Message;
SELECT 'Tables: patients, pregnancy, visits, child_growth, users' as Structure;
SELECT 'pregnancy_visits has been migrated and archived' as Note;
