# Backend Fix Required for Child Growth Sync

## ❌ Problem
Child growth records fail to sync with error: **"Invalid data format"**

## 🔍 Root Cause
**File:** `C:\xampp\htdocs\asha_api\child_growth.php`  
**Line:** 99  
**Error:** `ArgumentCountError: bind_param type mismatch`

```php
// CURRENT (BROKEN) - Line 99:
$stmt->bind_param('iisiddddss', $param1, $param2, ... $param11);
//                ^^^^^^^^^^    10 type characters
//                              but 11 parameters!
```

## ✅ Solution Options

### Option 1: Add one more type character (Recommended)
```php
$stmt->bind_param('iisiddddsss', $param1, $param2, ... $param11);
//                ^^^^^^^^^^^    11 type characters - matches 11 params
```

### Option 2: Remove one parameter
Remove the 11th parameter to match 10 type characters

## 📋 Type Characters Reference
- `i` = integer
- `d` = double (float)
- `s` = string
- `b` = blob

## 🧪 Test Command
After fixing, test with:
```powershell
$body = '{"patient_id":107,"weight":22,"height":55,"head_circumference":35,"age_months":6}'
Invoke-WebRequest -Uri 'http://192.168.1.69/asha_api/child_growth.php' -Method POST -Body $body -ContentType 'application/json'
```

Expected response: `{"success":true,"id":...}`

## 📱 App Impact
- ✅ Patient sync: **Working** (fixed by using getUserId())
- ❌ Child growth sync: **Blocked** until backend fix
- ℹ️ Other syncs: Untested (may have similar issues)

## 🔧 Files Modified (App Side)
1. `SyncService.java` - Fixed to use getUserId() instead of getWorkerId()
2. `PatientProfileActivity.java` - Fixed view ID mismatches
3. `ARCHITECTURE_PROMPT.md` - Documented known backend bugs

---

**Priority:** HIGH - Blocking child growth data sync  
**Complexity:** LOW - Single line fix in PHP  
**Testing:** Required after fix
