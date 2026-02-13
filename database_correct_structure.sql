-- ======================================================================
-- CORRECT DATABASE STRUCTURE FOR ASHA SMARTCARE
-- ======================================================================
-- Based on AddPatientActivity.java frontend fields
-- Each category gets its own table with ONLY relevant data
-- visits table contains ONLY visit information (no vitals)
-- ======================================================================

USE asha_smartcare;

-- Step 1: BACKUP existing data before changes
DROP TABLE IF EXISTS pregnancy_backup;
CREATE TABLE pregnancy_backup AS SELECT * FROM pregnancy;

DROP TABLE IF EXISTS visits_backup;
CREATE TABLE visits_backup AS SELECT * FROM visits;

DROP TABLE IF EXISTS child_growth_backup;
CREATE TABLE child_growth_backup AS SELECT * FROM child_growth;

-- ======================================================================
-- PREGNANCY TABLE - ALL pregnancy-related health tracking data
-- ======================================================================
DROP TABLE IF EXISTS pregnancy;
CREATE TABLE pregnancy (
    id INT(11) NOT NULL AUTO_INCREMENT,
    patient_id INT(11) NOT NULL,
    asha_id INT(11),
    
    -- Pregnancy Dates
    lmp_date DATE,
    edd_date DATE,
    
    -- Obstetric History
    gravida INT(11) DEFAULT 1,
    para INT(11) DEFAULT 0,
    abortion INT(11) DEFAULT 0,
    living_children INT(11) DEFAULT 0,
    
    -- Current Pregnancy Vitals (from AddPatientActivity.java)
    blood_pressure VARCHAR(20),           -- "systolic/diastolic"
    weight DECIMAL(5,2),                  -- kg
    hemoglobin DECIMAL(4,2),              -- g/dL
    
    -- Danger Signs (from Chips in frontend)
    danger_signs TEXT,                    -- "Severe Headache,Swelling,Bleeding,Blurred Vision,Reduced Movement"
    
    -- Medicines/Interventions (from Checkboxes)
    iron_tablets_given TINYINT(1) DEFAULT 0,
    calcium_tablets_given TINYINT(1) DEFAULT 0,
    tetanus_injection_given TINYINT(1) DEFAULT 0,
    
    -- Additional pregnancy tracking
    urine_sugar VARCHAR(20),              -- Test result
    urine_protein VARCHAR(20),            -- Test result
    fetal_heart_rate INT(11),             -- bpm
    fundal_height DECIMAL(5,2),           -- cm
    presentation VARCHAR(50),             -- Cephalic/Breech
    gestational_weeks INT(11),            -- Weeks
    
    -- Schedule
    next_visit_date DATE,
    last_visit_date DATE,
    
    -- Status
    pregnancy_status ENUM('Active', 'Delivered', 'Terminated') DEFAULT 'Active',
    pregnancy_outcome VARCHAR(100),
    delivery_date DATE,
    
    -- Risk Assessment
    is_high_risk TINYINT(1) DEFAULT 0,
    high_risk_reason TEXT,
    
    -- Notes
    notes TEXT,
    
    -- Sync
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_asha_id (asha_id),
    KEY idx_pregnancy_status (pregnancy_status),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (asha_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================================
-- CHILD_GROWTH TABLE - ALL child (0-5 years) health tracking data
-- ======================================================================
DROP TABLE IF EXISTS child_growth;
CREATE TABLE child_growth (
    id INT(11) NOT NULL AUTO_INCREMENT,
    patient_id INT(11) NOT NULL,
    asha_id INT(11),
    record_date DATE NOT NULL,
    
    -- Growth Measurements (from AddPatientActivity.java)
    age_months INT(11),
    weight DECIMAL(5,2),                  -- kg
    height DECIMAL(5,2),                  -- cm
    muac DECIMAL(5,2),                    -- cm (Mid-Upper Arm Circumference)
    head_circumference DECIMAL(5,2),      -- cm
    temperature DECIMAL(4,2),             -- Celsius
    
    -- Feeding Assessment (RadioGroups in frontend)
    breastfeeding VARCHAR(50),            -- "Exclusive"/"Partial"/"None"
    complementary_feeding VARCHAR(50),    -- "Appropriate"/"Inadequate"/"None"
    appetite VARCHAR(50),                 -- "Good"/"Poor"/"Refusing"
    
    -- Symptoms (Checkboxes in frontend)
    fever TINYINT(1) DEFAULT 0,
    diarrhea TINYINT(1) DEFAULT 0,
    cough_cold TINYINT(1) DEFAULT 0,
    vomiting TINYINT(1) DEFAULT 0,
    weakness TINYINT(1) DEFAULT 0,
    
    -- Vaccination
    last_vaccine VARCHAR(100),            -- Vaccine name
    next_vaccine_date DATE,
    
    -- Assessment
    nutritional_status ENUM('Normal', 'MAM', 'SAM', 'Overweight') DEFAULT 'Normal',
    milestones TEXT,                      -- Development milestones
    
    -- Notes
    notes TEXT,
    
    -- Sync
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_asha_id (asha_id),
    KEY idx_record_date (record_date),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (asha_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================================
-- GENERAL_ADULT TABLE - ALL general adult health tracking data
-- (NEW TABLE - Was missing before)
-- ======================================================================
CREATE TABLE general_adult (
    id INT(11) NOT NULL AUTO_INCREMENT,
    patient_id INT(11) NOT NULL,
    asha_id INT(11),
    record_date DATE NOT NULL,
    
    -- Vitals (from AddPatientActivity.java)
    blood_pressure VARCHAR(20),           -- "systolic/diastolic"
    weight DECIMAL(5,2),                  -- kg
    sugar_level DECIMAL(5,2),             -- mg/dL
    temperature DECIMAL(4,2),             -- Celsius
    pulse_rate INT(11),                   -- bpm
    
    -- Symptoms (Checkboxes in frontend)
    fever TINYINT(1) DEFAULT 0,
    body_pain TINYINT(1) DEFAULT 0,
    breathlessness TINYINT(1) DEFAULT 0,
    dizziness TINYINT(1) DEFAULT 0,
    chest_pain TINYINT(1) DEFAULT 0,
    
    -- Lifestyle Assessment (RadioGroups in frontend)
    tobacco_use VARCHAR(20),              -- "Yes"/"No"/"Quit"
    alcohol_use VARCHAR(20),              -- "Yes"/"No"/"Quit"
    physical_activity VARCHAR(20),        -- "Low"/"Moderate"/"Active"
    
    -- Referral
    referral_required TINYINT(1) DEFAULT 0,
    referral_reason TEXT,
    
    -- Schedule
    follow_up_date DATE,
    
    -- Chronic Conditions (Optional - can be expanded)
    has_diabetes TINYINT(1) DEFAULT 0,
    has_hypertension TINYINT(1) DEFAULT 0,
    chronic_conditions TEXT,
    
    -- Notes
    notes TEXT,
    
    -- Sync
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_asha_id (asha_id),
    KEY idx_record_date (record_date),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (asha_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================================
-- VISITS TABLE - ONLY visit information (dates, notes, recommendations)
-- NO VITALS - those go in category-specific tables
-- ======================================================================
DROP TABLE IF EXISTS visits;
CREATE TABLE visits (
    id INT(11) NOT NULL AUTO_INCREMENT,
    patient_id INT(11) NOT NULL,
    asha_id INT(11),
    
    -- Visit Information ONLY
    visit_type VARCHAR(50) NOT NULL,      -- "Pregnancy Visit", "Child Checkup", "General Visit"
    visit_date DATE NOT NULL,
    
    -- Visit Details (General information, no vitals)
    purpose TEXT,                         -- Reason for visit
    findings TEXT,                        -- Observations
    recommendations TEXT,                 -- Advice given
    medicines_prescribed TEXT,            -- Medicine names
    next_visit_date DATE,
    notes TEXT,
    
    -- Sync
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    KEY idx_patient_id (patient_id),
    KEY idx_asha_id (asha_id),
    KEY idx_visit_type (visit_type),
    KEY idx_visit_date (visit_date),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (asha_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================================
-- MIGRATE DATA FROM BACKUPS
-- ======================================================================

-- Migrate pregnancy data (keep existing metadata, no vitals yet)
INSERT INTO pregnancy (patient_id, asha_id, lmp_date, edd_date, gravida, para, abortion, 
                      living_children, pregnancy_status, pregnancy_outcome, delivery_date,
                      is_high_risk, high_risk_reason, notes, sync_status, created_at, updated_at)
SELECT patient_id, asha_id, lmp_date, edd_date, gravida, para, abortion,
       living_children, pregnancy_status, pregnancy_outcome, delivery_date,
       is_high_risk, high_risk_reason, notes, sync_status, created_at, updated_at
FROM pregnancy_backup;

-- Migrate child_growth data (basic data only, symptoms will be added with new records)
INSERT INTO child_growth (patient_id, asha_id, record_date, age_months, weight, height,
                         head_circumference, muac, nutritional_status, milestones, notes,
                         sync_status, created_at, updated_at)
SELECT patient_id, asha_id, record_date, age_months, weight, height,
       head_circumference, muac, nutritional_status, milestones, notes,
       sync_status, created_at, updated_at
FROM child_growth_backup;

-- Migrate visits data (ONLY visit information, drop all vitals columns)
INSERT INTO visits (patient_id, asha_id, visit_type, visit_date, purpose, findings,
                   recommendations, medicines_prescribed, next_visit_date, notes,
                   sync_status, created_at, updated_at)
SELECT patient_id, asha_id, visit_type, visit_date, purpose, findings,
       recommendations, medicines_prescribed, next_visit_date, notes,
       sync_status, created_at, updated_at
FROM visits_backup;

-- ======================================================================
-- VERIFICATION QUERIES
-- ======================================================================
SELECT '✅ Database structure corrected!' as status;
SELECT 'Pregnancy records:' as info, COUNT(*) as count FROM pregnancy;
SELECT 'Child growth records:' as info, COUNT(*) as count FROM child_growth;
SELECT 'General adult records:' as info, COUNT(*) as count FROM general_adult;
SELECT 'Visit records:' as info, COUNT(*) as count FROM visits;

SELECT '📊 Patients by category:' as info;
SELECT category, COUNT(*) as patient_count 
FROM patients 
GROUP BY category;

SELECT '✅ Tables created successfully!' as status;
SHOW TABLES;
