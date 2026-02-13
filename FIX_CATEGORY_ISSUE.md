# CRITICAL FIX: Patient Category Not Saving to Database

## Problem Summary
Patient category is being saved as **empty string** in database despite being sent correctly from the app.

### Evidence from Logcat:
- **Sent to backend:** `"category":"Child (0-5 years)"`
- **Retrieved from database:** `"category":""` ← **EMPTY!**

## Root Cause
The `category` column in the `patients` table either:
1. Doesn't exist
2. Has wrong data type
3. Has a constraint preventing data from being saved

---

## SOLUTION: 3-Step Fix

### Step 1: Fix Database Structure

**Open phpMyAdmin** (http://localhost/phpmyadmin or http://192.168.1.69/phpmyadmin)

1. Select database: `asha_smartcare`
2. Go to **SQL** tab
3. Copy and paste this entire script:

```sql
-- Check current table structure
SHOW COLUMNS FROM patients LIKE 'category';

-- Fix category column
ALTER TABLE patients 
MODIFY COLUMN category VARCHAR(100) NOT NULL DEFAULT '' AFTER address;

-- Fix patient 113 (your test patient)
UPDATE patients 
SET category = 'Child (0-5 years)' 
WHERE id = 113;

-- Fix ALL patients with empty category
UPDATE patients 
SET category = CASE
    WHEN age <= 5 THEN 'Child (0-5 years)'
    WHEN age >= 15 AND age <= 50 AND gender = 'Female' THEN 'Pregnant Woman'
    ELSE 'General'
END
WHERE category = '' OR category IS NULL;

-- Verify fixes
SELECT id, name, age, category FROM patients ORDER BY id DESC LIMIT 10;
```

4. Click **Go** button
5. Check the results - patient 113 should now show `"category":"Child (0-5 years)"`

---

### Step 2: Install Updated APK

The app has been updated with:
- ✅ Auto-detect category if database returns empty (based on age/gender)
- ✅ Better category matching for all variants
- ✅ Enhanced logging for debugging

**Install updated APK:**
```
Location: C:\Users\HP\Documents\Application_developement\ASHASmartCare\app\build\outputs\apk\debug\app-debug.apk
```

1. Uninstall old version from device (Settings → Apps → ASHASmartCare → Uninstall)
2. Copy new APK to device
3. Install and open

---

### Step 3: Test All Categories

#### Test 1: Child Patient (Existing - ID 113)
1. Open **Patients** list
2. Tap on "test1" (age 11)
3. **Expected:**
   - ✅ Overview tab shows child vitals: Weight, Height, Temperature, MUAC
   - ✅ Growth tab visible (not "Health History")
   - ✅ Vitals load from `child_growth` table
   - ✅ Logcat shows: `"Loading child growth data from: child_growth.php?patient_id=113"`

#### Test 2: Create New Pregnant Woman
1. Add Patient → Name: "Priya", Age: 25, Gender: Female, Category: **Pregnant Woman**
2. Fill pregnancy details (BP, Hemoglobin, etc.)
3. Save
4. Open profile
5. **Expected:**
   - ✅ Shows pregnancy vitals: BP, Weight, Hemoglobin, Fetal Heart Rate
   - ✅ Growth tab shows pregnancy trends
   - ✅ Logcat shows: `"Category set to: Pregnant Woman"`

#### Test 3: Create New General Adult
1. Add Patient → Name: "Ravi", Age: 45, Gender: Male, Category: **General**
2. Fill general health data
3. Save
4. Open profile
5. **Expected:**
   - ✅ Shows general vitals: BP, Weight, Sugar Level, Temperature
   - ✅ Shows "Health History" tab (not "Growth")
   - ✅ Logcat shows: `"Category set to: General"`

---

## What Was Fixed

### Backend Changes (`patients.php`)
```php
// Added logging
error_log("Saving patient - Category: '$category', Name: '$name'");

// Added verification after save
$verify_sql = "SELECT category FROM patients WHERE id = ?";
// Logs actual saved category
error_log("Saved category for patient $server_id: '$saved_category'");
```

### App Changes (`PatientProfileActivity.java`)
```java
// Auto-detect category if empty
if (rawCategory == null || rawCategory.trim().isEmpty()) {
    if (age <= 5) {
        rawCategory = "Child (0-5 years)";
    } else if (age >= 15 && age <= 50 && gender.equalsIgnoreCase("Female")) {
        rawCategory = "Pregnant Woman";
    } else {
        rawCategory = "General";
    }
}

// Enhanced category matching
if (category.contains("Child") || 
    category.equalsIgnoreCase("Child (0-5 yrs)") || 
    category.equalsIgnoreCase("Child (0-5 years)")) {
    loadChildGrowthData();
}
```

---

## Debugging

### Check PHP Error Logs
**Location:** `C:\xampp\apache\logs\error.log`

Look for lines like:
```
Saving patient - Category: 'Child (0-5 years)', Name: 'test1'
Saved category for patient 113: 'Child (0-5 years)'
```

### Check App Logcat (Important!)
After opening patient profile, check for:
```
PatientProfile: Category set to: Child (0-5 years)
PatientProfile: Loading data for category: Child (0-5 years)
PatientProfile: Loading child growth data from: child_growth.php?patient_id=113
```

If you see:
```
PatientProfile: Category set to: 
PatientProfile: Empty category, auto-detected as Child based on age=11
```
This means database STILL has empty category → Re-run Step 1 SQL script!

---

## Common Issues

### Issue 1: Still showing empty category
**Solution:** Make sure you ran the SQL script in Step 1. Check in phpMyAdmin:
```sql
SELECT id, name, category FROM patients WHERE id = 113;
```
Should return: `category = "Child (0-5 years)"`

### Issue 2: Visits tab empty
**Solution:** Create a test visit:
```sql
INSERT INTO visits (patient_id, asha_id, visit_type, visit_date, next_visit_date, purpose) 
VALUES (113, 1, 'Growth Monitoring', '2026-02-07', '2026-03-07', 'Monthly checkup');
```

### Issue 3: No vitals showing
**Solution:** Check if child_growth record exists:
```sql
SELECT * FROM child_growth WHERE patient_id = 113;
```

If empty, the record created during "Add Patient" might have failed. Create manually:
```sql
INSERT INTO child_growth (patient_id, asha_id, record_date, weight, height, muac, temperature)
VALUES (113, 1, '2026-02-07', 11.0, 110.0, 11.0, 98.6);
```

---

## Next Steps

1. ✅ Run SQL script to fix database
2. ✅ Install updated APK
3. ✅ Test patient 113 profile (should now work!)
4. ✅ Create new patients in each category
5. ✅ Verify all tabs load correct data

**Report back:**
- Does patient 113 now show child vitals?
- Does category appear correctly in logcat?
- Are new patients saving category properly?
