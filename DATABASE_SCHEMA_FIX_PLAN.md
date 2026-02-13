# Database Schema Fix Plan

## ���� CRITICAL ISSUES IDENTIFIED

### 1. Schema Mismatch Between Frontend and Backend
**Problem:** Frontend collects data that backend database doesn't have columns for

### 2. Field Naming Inconsistencies
**Problem:** Frontend uses "village" but displays as address, backend has district/state not needed

### 3. Category Value Mismatch
**Problem:** App uses "Pregnant Woman", "Lactating Mother", "Child (0-5 yrs)" but backend may have different values

### 4. Missing Fields
**Problem:** Blood group, medicine columns not in schema

## 📋 REQUIRED CHANGES

### PATIENTS TABLE Changes

#### ✅ KEEP (No Change):
- `id` (PRIMARY KEY)
- `asha_id` (INTEGER - links to ASHA worker)
- `name` (TEXT)
- `age` (INTEGER)
- `gender` (TEXT - Male/Female/Other)
- `phone` (TEXT - PRIMARY contact number)
- `abha_id` (TEXT - Ayushman Bharat Health Account ID)
- `category` (TEXT - Patient category)
- `is_high_risk` (INTEGER 0/1 - High risk flag)
- `high_risk_reason` (TEXT - Reason for high risk)
- `sync_status` (TEXT - PENDING/SYNCED/FAILED)
- `created_at` (DATETIME)
- `updated_at` (DATETIME)

#### ❌ REMOVE:
- `district` - Not needed, address is enough
- `state` - Not needed, address is enough  
- `emergency_contact` - Redundant, use phone instead
- `village` - Rename to address

#### ➕ ADD/RENAME:
- **RENAME** `village` → `address` (TEXT - Full address)
- **ADD** `blood_group` (TEXT - A+, A-, B+, B-, AB+, AB-, O+, O-, Unknown)

#### 🔄 UPDATE:
- `category` VALUES must be standardized to:
  - "Pregnant Woman"
  - "Lactating Mother"
  - "Child (0-5 yrs)"
  - "Adult"
  - "General"

#### ✏️ MODIFY:
- `high_risk_reason` should store comma-separated values:
  - Options: "Severe Headache", "Swelling", "Bleeding", "High Blood Pressure", "Vision Problems", "Reduced Fetal Movement", "Custom"

### PREGNANCY_VISITS TABLE Changes

#### Current Fields:
- `id` (PRIMARY KEY)
- `patient_id` (FOREIGN KEY)
- `visit_date` (DATETIME)
- `lmp_date` (DATE - Last Menstrual Period)
- `edd` (DATE - Expected Delivery Date)
- `blood_pressure` (TEXT - format: "120/80")
- `weight` (DECIMAL)
- `hemoglobin` (DECIMAL)
- `danger_signs` (TEXT - comma-separated)
- `medicines` (TEXT - comma-separated)
- `next_visit_date` (DATE)
- `created_at` (DATETIME)
- `updated_at` (DATETIME)

#### ✅ Keep All Current Fields
#### ➕ ADD:
- `visit_type` (TEXT - "Initial Visit", "Follow-up", "Emergency")
- `notes` (TEXT - Additional notes)
- `sync_status` (TEXT - PENDING/SYNCED/FAILED)

### VISITS TABLE Changes

#### ✅ KEEP:
- `id` (PRIMARY KEY)
- `patient_id` (FOREIGN KEY)
- `visit_date` (DATETIME)
- `visit_type` (TEXT)
- `findings` (TEXT - Examination findings)
- `notes` (TEXT)
- `next_visit_date` (DATE)
- `created_at` (DATETIME)
- `updated_at` (DATETIME)

#### ➕ ADD:
- `medicines_prescribed` (TEXT - comma-separated list)
- `sync_status` (TEXT - PENDING/SYNCED/FAILED)

## 🗄️ UPDATED SQL SCHEMA

### 1. ALTER PATIENTS TABLE

```sql
-- Rename village to address
ALTER TABLE patients CHANGE COLUMN village address TEXT;

-- Add blood_group column
ALTER TABLE patients ADD COLUMN blood_group VARCHAR(10) DEFAULT NULL;

-- Drop unnecessary columns
ALTER TABLE patients DROP COLUMN district;
ALTER TABLE patients DROP COLUMN state;
ALTER TABLE patients DROP COLUMN emergency_contact;

-- Update existing category values
UPDATE patients SET category = 'Pregnant Woman' WHERE category IN ('pregnant', 'pregn');
UPDATE patients SET category = 'Lactating Mother' WHERE category IN ('lactating', 'mother');
UPDATE patients SET category = 'Child (0-5 yrs)' WHERE category IN ('child', 'infant');
UPDATE patients SET category = 'Adult' WHERE category IN ('adult', 'general_adult');
UPDATE patients SET category = 'General' WHERE category IN ('general', 'other');
```

### 2. UPDATE PREGNANCY_VISITS TABLE

```sql
-- Add missing columns
ALTER TABLE pregnancy_visits ADD COLUMN visit_type VARCHAR(50) DEFAULT 'Follow-up';
ALTER TABLE pregnancy_visits ADD COLUMN notes TEXT DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN sync_status VARCHAR(20) DEFAULT 'SYNCED';

-- Ensure all required columns exist
ALTER TABLE pregnancy_visits ADD COLUMN lmp_date DATE DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN edd DATE DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN blood_pressure VARCHAR(20) DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN hemoglobin DECIMAL(4,2) DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN danger_signs TEXT DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN medicines TEXT DEFAULT NULL;
ALTER TABLE pregnancy_visits ADD COLUMN next_visit_date DATE DEFAULT NULL;
```

### 3. UPDATE VISITS TABLE

```sql
-- Add medicines column
ALTER TABLE visits ADD COLUMN medicines_prescribed TEXT DEFAULT NULL;
ALTER TABLE visits ADD COLUMN sync_status VARCHAR(20) DEFAULT 'SYNCED';

-- Ensure next_visit_date exists
ALTER TABLE visits ADD COLUMN next_visit_date DATE DEFAULT NULL;
```

## 📱 ANDROID APP UPDATES REQUIRED

### 1. Patient Model (Patient.java)
```java
// REMOVE
private String district;
private String state;
private String village;
private String emergencyContact;

// ADD/RENAME
private String address;  // Renamed from village
private String bloodGroup;  // New field

// UPDATE getters/setters accordingly
```

### 2. DatabaseHelper.java
- Update CREATE_TABLE_PATIENTS to match new schema
- Update all insert/update methods
- Increment DATABASE_VERSION
- Handle migration in onUpgrade()

### 3. AddPatientActivity.java
- Remove district/state EditTexts from UI
- Rename village EditText to address
- Add blood group spinner
- Update savePatient() method to use new field names
- Update validation logic

### 4. PatientsActivity.java / PatientListActivity.java
- Update patient list item to show address instead of village
- Update API response parsing for new field names

### 5. PatientProfileActivity.java
- Display address instead of village/district/state
- Show blood group field
- Update data binding

### 6. PregnancyVisit Model & Activity
- Ensure all fields match backend schema
- Add visit_type field
- Ensure danger_signs and medicines are comma-separated strings

### 7. Visit Model & Activity
- Add medicines_prescribed field
- Ensure next_visit_date is captured

## 🔄 BACKEND PHP UPDATES REQUIRED

### 1. patients.php
```php
// Update SELECT query to use 'address' instead of 'village'
// Remove district, state, emergency_contact from SELECT
// Add blood_group to SELECT
// Update INSERT/UPDATE queries

// Example SELECT:
$sql = "SELECT id, asha_id, name, age, gender, phone, abha_id, 
        address, category, blood_group, is_high_risk, high_risk_reason, 
        sync_status, created_at, updated_at 
        FROM patients WHERE asha_id = ?";

// Update INSERT:
$sql = "INSERT INTO patients (asha_id, name, age, gender, phone, abha_id, 
        address, category, blood_group, is_high_risk, high_risk_reason) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
```

### 2. pregnancy_visits.php
```php
// Ensure all fields are included in SELECT/INSERT
// Add visit_type, notes if missing
// Keep danger_signs, medicines as TEXT fields

$sql = "SELECT id, patient_id, visit_date, lmp_date, edd, 
        blood_pressure, weight, hemoglobin, danger_signs, medicines, 
        next_visit_date, visit_type, notes, created_at 
        FROM pregnancy_visits WHERE patient_id = ?";
```

### 3. visits.php
```php
// Add medicines_prescribed to SELECT/INSERT
$sql = "SELECT id, patient_id, visit_date, visit_type, findings, 
        medicines_prescribed, next_visit_date, notes, created_at 
        FROM visits WHERE patient_id = ?";
```

## ✅ VALIDATION RULES

### Blood Group Options:
- A+, A-, B+, B-, AB+, AB-, O+, O-, Unknown

### Category Options (Standardized):
- Pregnant Woman
- Lactating Mother
- Child (0-5 yrs)
- Adult  
- General

### Danger Signs Options (for pregnancy):
- Severe Headache
- Swelling (Edema)
- Bleeding
- High Blood Pressure
- Vision Problems
- Reduced Fetal Movement
- Severe Abdominal Pain
- Fever
- Convulsions
- Custom (allows text input)

### Medicine Common Options:
- Iron Tablets (IFA)
- Folic Acid
- Calcium Supplements
- Vitamin D
- Antibiotics
- Pain Relievers
- Custom (allows text input)

## 🚀 IMPLEMENTATION STEPS

### Phase 1: Database Migration (Backend)
1. ✅ Backup current database
2. ✅ Run ALTER TABLE statements on patients table
3. ✅ Run ALTER TABLE statements on pregnancy_visits table
4. ✅ Run ALTER TABLE statements on visits table
5. ✅ Update category values to standardized format
6. ✅ Test SELECT queries to ensure data integrity

### Phase 2: Backend PHP Updates
1. ✅ Update patients.php (GET/POST/PUT endpoints)
2. ✅ Update pregnancy_visits.php
3. ✅ Update visits.php
4. ✅ Test all endpoints with Postman/curl
5. ✅ Verify response formats match app expectations

### Phase 3: Android Model Updates
1. ✅ Update Patient.java model
2. ✅ Update PregnancyVisit.java model
3. ✅ Update Visit.java model
4. ✅ Update DatabaseHelper.java schema
5. ✅ Increment DATABASE_VERSION
6. ✅ Implement onUpgrade() migration logic

### Phase 4: Android UI Updates
1. ✅ Update AddPatientActivity layout and logic
2. ✅ Add blood group spinner with options
3. ✅ Rename village field to address
4. ✅ Remove district/state fields
5. ✅ Update category spinner values
6. ✅ Update danger signs checkboxes/chips
7. ✅ Add medicine selection chips

### Phase 5: Android API Integration
1. ✅ Update all API request payloads
2. ✅ Update all API response parsing
3. ✅ Test patient creation (online mode)
4. ✅ Test patient viewing
5. ✅ Test pregnancy visit creation
6. ✅ Test visit creation
7. ✅ Test offline sync queue

### Phase 6: Testing & Validation
1. ✅ Test patient registration flow (online)
2. ✅ Test patient list display
3. ✅ Test patient profile display
4. ✅ Test pregnancy visit recording
5. ✅ Test general visit recording
6. ✅ Test data consistency between app and backend
7. ✅ Test offline mode (sync queue)
8. ✅ Verify all fields are saved and displayed correctly

## 📝 TESTING CHECKLIST

### Patient Registration:
- [ ] Name, age, gender saved correctly
- [ ] Phone number saved (not as emergency_contact)
- [ ] Address saved (not as village)
- [ ] Blood group saved and displayed
- [ ] Category matches standardized values
- [ ] ABHA ID saved correctly
- [ ] High risk flag and reason saved
- [ ] No district/state fields present

### Patient List View:
- [ ] Shows address (not village)
- [ ] Shows correct category
- [ ] Shows blood group
- [ ] No district/state displayed

### Patient Profile:
- [ ] All fields display correctly
- [ ] Address shown (not village/district/state)
- [ ] Blood group displayed
- [ ] Category displayed correctly

### Pregnancy Visit:
- [ ] LMP date captured
- [ ] EDD calculated and displayed
- [ ] Blood pressure captured
- [ ] Weight and hemoglobin captured
- [ ] Danger signs saved as comma-separated
- [ ] Medicines saved as comma-separated
- [ ] Next visit date captured
- [ ] Visit type (Initial/Follow-up) saved

### General Visit:
- [ ] Visit date captured
- [ ] Visit type captured
- [ ] Findings recorded
- [ ] Medicines prescribed saved
- [ ] Next visit date captured
- [ ] Notes saved

### Backend API:
- [ ] GET patients returns correct fields
- [ ] POST patient accepts new schema
- [ ] GET pregnancy_visits returns all fields
- [ ] POST pregnancy_visit saves correctly
- [ ] GET visits returns all fields
- [ ] POST visit saves correctly

## ⚠️ IMPORTANT NOTES

1. **Backup First**: Always backup database before running ALTER statements
2. **Test Locally**: Test all changes on local/dev database first
3. **Migration Path**: Existing app users need data migration in onUpgrade()
4. **API Compatibility**: Backend must handle both old and new field names during transition
5. **Validation**: Add proper validation for blood group and category values
6. **Data Integrity**: Ensure foreign key constraints remain intact after ALTER statements

## 🔗 ARCHITECTURE COMPLIANCE

This fix follows the architecture defined in ARCHITECTURE_PROMPT.md:
- ✅ Online-first data fetching
- ✅ No local DB reads for display
- ✅ Proper GET/POST request patterns
- ✅ Response format compatibility checks
- ✅ Sync queue for offline operations
- ✅ SessionManager for user context
- ✅ ApiHelper for network calls

---

**Created:** February 6, 2026
**Status:** PLAN - Ready for Implementation
**Priority:** HIGH - Blocks core patient management functionality
