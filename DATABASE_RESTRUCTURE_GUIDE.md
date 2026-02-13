# Database Restructuring Complete - Implementation Guide

## ✅ Database Changes Completed

### New Table Structure:
1. **`pregnancy`** - Stores pregnancy metadata (LMP, EDD, gravida, para, etc.)
2. **`visits`** - Universal table for ALL visit types (pregnancy, child, general)
3. **`child_growth`** - Child growth tracking (existing, kept as-is)
4. **`patients`** - Patient master data (existing, kept as-is)
5. **`users`** - ASHA workers and admin (existing, kept as-is)

### Migration Results:
- ✅ Created `pregnancy` table: **4 records** (for 4 pregnant women)
- ✅ Enhanced `visits` table: Added pregnancy-specific columns (BP, weight, Hb, FHR, etc.)
- ✅ Migrated data from `pregnancy_visits` → `visits`: **8 records**
- ✅ Backed up old data to `pregnancy_visits_backup`
- ✅ Patient categories: **Pregnant Woman (4), General (5)**

---

## 📋 Backend PHP Files Status

### ✅ Created:
1. **`C:\xampp\htdocs\asha_api\pregnancy.php`**
   - Handles pregnancy metadata CRUD
   - GET by id, patient_id, or asha_id
   - POST to create new pregnancy
   - PUT to update (auto-calculates EDD from LMP)
   - DELETE to remove pregnancy record

### ⚠️ Needs Update:
2. **`C:\xampp\htdocs\asha_api\visits.php`**
   - Current: Basic visits CRUD
   - **Required:** Add support for pregnancy-specific columns
   - **Update sections:**
     - `createVisit()` - Add parameters for BP, weight, Hb, FHR, etc.
     - `getVisits()` - Include pregnancy columns in SELECT
     - `updateVisit()` - Support updating vitals

3. **`C:\xampp\htdocs\asha_api\patients.php`**
   - **Verify:** Returns `address`, `blood_group`, `category` fields
   - Status: Likely already correct from previous migration

### ✅ Keep As-Is:
4. **`C:\xampp\htdocs\asha_api\child_growth.php`** - No changes needed

---

## 📱 Android Frontend Required Updates

### Priority 1: PatientProfileActivity.java

**Current Issue:** Attempts to load from `pregnancy_visits.php` which no longer exists.

**Fix Required:**
```java
// OLD CODE (line 296):
String endpoint = "pregnancy_visits.php?patient_id=" + patientId;

// NEW CODE:
// 1. Get pregnancy metadata first
String pregnancyEndpoint = "pregnancy.php?patient_id=" + patientId;

// 2. Then get visit records
String visitsEndpoint = "visits.php?patient_id=" + patientId + "&visit_type=Pregnancy";
```

**Updated Logic:**
```java
private void displayPatientData() {
    // ... existing code ...
    
    // For Pregnant Woman category
    if ("Pregnant Woman".equalsIgnoreCase(category)) {
        loadPregnancyMetadata();  // NEW: Load LMP, EDD from pregnancy.php
        loadPregnancyVisits();     // NEW: Load visit records from visits.php
    }
}

private void loadPregnancyMetadata() {
    String endpoint = "pregnancy.php?patient_id=" + patientId;
    apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
        @Override
        public void onSuccess(JSONObject response) {
            // Extract LMP, EDD, gravida, para
            JSONObject pregnancyData = response.optJSONObject("data");
            if (pregnancyData != null) {
                String lmp = pregnancyData.optString("lmp_date");
                String edd = pregnancyData.optString("edd_date");
                // Update UI with pregnancy info
            }
        }
    });
}

private void loadPregnancyVisits() {
    String endpoint = "visits.php?patient_id=" + patientId + "&visit_type=Pregnancy";
    apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
        @Override
        public void onSuccess(JSONObject response) {
            // Get latest visit for vitals display
            JSONArray visits = response.optJSONArray("data");
            if (visits != null && visits.length() > 0) {
                JSONObject latestVisit = visits.getJSONObject(0);
                displayVitals(latestVisit);
            }
        }
    });
}
```

---

### Priority 2: AddPatientActivity.java

**Current:** Already saves to `patients.php` correctly.

**Verify:** When category = "Pregnant Woman":
- Backend creates patient record ✅
- **(NEW)** Should also create pregnancy record in `pregnancy` table

**Add Pregnancy Creation:**
```java
private void savePatient() {
    // ... existing patient save code ...
    
    // If category is Pregnant Woman, create pregnancy record
    if ("Pregnant Woman".equals(selectedCategory)) {
        createPregnancyRecord(patient_id, lmpDate);
    }
}

private void createPregnancyRecord(int patientId, String lmpDate) {
    JSONObject params = new JSONObject();
    params.put("patient_id", patientId);
    params.put("asha_id", sessionManager.getUserId());
    params.put("lmp_date", lmpDate);
    params.put("gravida", etGravida.getText().toString());
    params.put("para", etPara.getText().toString());
    
    apiHelper.makePostRequest("pregnancy.php", params, new ApiHelper.ApiCallback() {
        // Handle response
    });
}
```

---

### Priority 3: Add/Edit Pregnancy Visit Activity

**File:** (Create new or update existing pregnancy visit screen)

**Required Changes:**
- Change endpoint from `pregnancy_visits.php` → `visits.php`
- Set `visit_type` = "Pregnancy Visit - Initial" or "Pregnancy Visit - Follow-up"
- Include `pregnancy_id` in request (get from pregnancy table)

**Sample POST Request:**
```json
{
  "patient_id": 109,
  "asha_id": 1,
  "visit_type": "Pregnancy Visit - Follow-up",
  "visit_date": "2026-02-07",
  "pregnancy_id": 4,
  "gestational_weeks": 20,
  "weight": 66.0,
  "blood_pressure": "120/80",
  "hemoglobin": 11.5,
  "fetal_heart_rate": 142,
  "ifa_tablets_given": 30,
  "tt_injection_given": 1,
  "danger_signs": "None",
  "notes": "Patient feeling well"
}
```

---

## 🗂️ Category-Specific Data Flow

### Pregnant Woman:
1. **Patient record** → `patients` table
2. **Pregnancy metadata** → `pregnancy` table (LMP, EDD, gravida, para)
3. **Visit records** → `visits` table (visit_type = "Pregnancy Visit")
4. **Display:**
   - Overview: Show LMP, EDD, pregnancy weeks
   - Visits: List all pregnancy visits with vitals
   - Growth: (N/A for pregnant women)

### Child (0-5 yrs):
1. **Patient record** → `patients` table
2. **Growth tracking** → `child_growth` table (weight, height, MUAC, milestones)
3. **Visit records** → `visits` table (visit_type = "Child Checkup" or "Sick Visit")
4. **Display:**
   - Overview: Show growth chart summary
   - Visits: List all child visits
   - Growth: Detailed growth tracking with WHO charts

### General Adult:
1. **Patient record** → `patients` table
2. **Visit records** → `visits` table (visit_type = "General Visit")
3. **Display:**
   - Overview: Show recent vitals
   - Visits: List all visits
   - Growth: (N/A for adults)

---

## 🔍 Testing Checklist

### Database Testing:
```sql
-- Verify pregnancy table
SELECT * FROM pregnancy;

-- Verify visits migrated
SELECT * FROM visits WHERE patient_id = 109;

-- Verify categories
SELECT category, COUNT(*) as count FROM patients GROUP BY category;
```

### Backend API Testing:
```bash
# Test pregnancy.php
curl "http://192.168.1.69/asha_api/pregnancy.php?patient_id=109"

# Test visits.php
curl "http://192.168.1.69/asha_api/visits.php?patient_id=109&visit_type=Pregnancy"

# Test patients.php
curl "http://192.168.1.69/asha_api/patients.php?asha_id=1"
```

### Frontend Testing:
1. Open patient list → Click patient 109 (test6)
2. **Expected:**
   - Shows name, age, address
   - Shows "High Risk" + "Pregnant" tags
   - Shows pregnancy vitals (BP, weight, Hb, FHR)
3. Add new pregnancy visit → Save
4. **Expected:**
   - Saves to `visits` table
   - Visit appears in patient profile

---

## 🔧 Quick Fixes Required

### 1. Update visits.php Backend (10 minutes):
File: `C:\xampp\htdocs\asha_api\visits.php`

Add to `createVisit()` function around line 90:
```php
$weight = isset($data['weight']) ? floatval($data['weight']) : null;
$bloodPressure = isset($data['blood_pressure']) ? sanitize($conn, $data['blood_pressure']) : null;
$hemoglobin = isset($data['hemoglobin']) ? floatval($data['hemoglobin']) : null;
$fetalHeartRate = isset($data['fetal_heart_rate']) ? intval($data['fetal_heart_rate']) : null;
$gestationalWeeks = isset($data['gestational_weeks']) ? intval($data['gestational_weeks']) : null;
$dangerSigns = isset($data['danger_signs']) ? sanitize($conn, $data['danger_signs']) : null;

$sql = "INSERT INTO visits (patient_id, asha_id, visit_type, visit_date, 
        weight, blood_pressure, hemoglobin, fetal_heart_rate, gestational_weeks, 
        danger_signs, findings, notes) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
```

### 2. Update PatientProfileActivity.java (20 minutes):
File: `app/src/main/java/com/simats/ashasmartcare/PatientProfileActivity.java`

Change line 296:
```java
// OLD:
String endpoint = "pregnancy_visits.php?patient_id=" + patientId;

// NEW:
String endpoint = "visits.php?patient_id=" + patientId + "&visit_type=Pregnancy";
```

Update response parsing (line 310):
```java
// OLD:
if (response.has("visits") && response.getJSONArray("visits").length() > 0) {

// NEW:
if (response.has("data") && response.getJSONArray("data").length() > 0) {
    JSONArray visitsArray = response.getJSONArray("data");
```

### 3. Rebuild Android App (5 minutes):
```powershell
.\gradlew.bat assembleDebug
```

---

## 📊 Architecture Compliance

### Online-First Logic ✅:
- Patient data: Fetched from backend API directly
- Visit data: Fetched from backend API directly
- No local SQLite reads for patient profile
- NetworkMonitorService checks connectivity
- Offline mode: Local SQLite + sync queue (already implemented)

### Data Flow:
```
User Action → Android Activity → ApiHelper → Backend PHP → MySQL Database
                                             ↓
                                    JSON Response ← Display in UI
```

---

## 🎯 Summary

**Completed:**
- ✅ Database restructured (pregnancy + visits tables)
- ✅ Created pregnancy.php backend
- ✅ Migration SQL executed successfully
- ✅ 4 pregnancy records + 8 visits created

**Remaining:**
- ⚠️ Update visits.php to support pregnancy columns
- ⚠️ Update PatientProfileActivity.java endpoint
- ⚠️ Update pregnancy visit flow in Android
- ⚠️ Test with rebuilt APK

**Next Step:** Update visits.php backend file, then update Android frontend endpoints.
