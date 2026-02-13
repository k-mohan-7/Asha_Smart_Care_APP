# ASHASmartCare Architecture Rules - Copy & Paste With Every Request

## 🏗️ ONLINE-FIRST ARCHITECTURE

### Rule 1: Data Fetching Logic
**ONLINE (Network Available):**
- ✅ Fetch ALL data from backend API
- ✅ Display data directly from API response
- ❌ DO NOT read from local database for display

**OFFLINE (No Network):**
- ✅ Show "No internet connection" message
- ✅ Clear data lists
- ✅ Display empty state
- ❌ DO NOT attempt to read from local database

**Local Database Usage:**
- ✅ ONLY for offline sync queue (pending add/edit operations)
- ❌ NEVER for reading/displaying data

---

## 📡 GET REQUEST PATTERN

### When to Use:
- Fetching lists (patients, visits, vaccinations, etc.)
- Reading data by ID or filters

### How It Works:
1. Check network availability first
2. If online: Call `apiHelper.makeGetRequest("endpoint.php?param=value", callback)`
3. Get ASHA/User ID from SessionManager
4. Pass parameters via URL query string
5. Handle response in callback

### Endpoint Format:
- `patients.php?asha_id=1`
- `visits.php?asha_id=1`
- `vaccinations.php?asha_id=1`
- `pregnancy_visits.php?patient_id=5`
- `child_growth.php?patient_id=3`

---

## 📤 POST REQUEST PATTERN

### When to Use:
- Login/authentication
- Adding new records (patients, visits, etc.)
- Updating existing records

### How It Works:
1. Check network availability first
2. If online: Create JSONObject with parameters
3. Call `apiHelper.makeRequest(Request.Method.POST, url, params, callback)`
4. Send data in request body as JSON
5. Handle response in callback

### Data Format:
- Create JSONObject with key-value pairs
- Send via POST body, not URL
- Content-Type: application/json

---

## 🔄 BACKEND RESPONSE COMPATIBILITY

### Problem:
Backend returns inconsistent JSON formats across endpoints

### Format Variations:
**Format 1:** `{"status":"success","patients":[...]}`
**Format 2:** `{"success":true,"data":[...]}`

### Solution:
ALWAYS check for BOTH formats:
- Success field: Check both `"success"` (boolean) and `"status"` (string)
- Data array: Check both `"data"` and endpoint-specific names (patients, visits, etc.)

---

## ✅ REQUIRED FIELD INITIALIZATIONS

### Must Initialize in initViews():
1. **apiHelper** - From `network.ApiHelper` (NOT utils!)
2. **sessionManager** - Provides getUserId()
3. **dbHelper** - For sync queue only

### Common Crash Cause:
Forgetting to initialize sessionManager → NullPointerException when calling `getUserId()`

---

## 📦 PACKAGE LOCATIONS

- **ApiHelper:** `com.simats.ashasmartcare.network.ApiHelper`
- **SessionManager:** `com.simats.ashasmartcare.utils.SessionManager`
- **NetworkUtils:** `com.simats.ashasmartcare.utils.NetworkUtils`
- **DatabaseHelper:** `com.simats.ashasmartcare.database.DatabaseHelper`

---

## 🎯 ACTIVITY LIFECYCLE FLOW

### Every List Activity Must Follow:
1. **onCreate()** → Initialize everything
2. **initViews()** → Initialize apiHelper, sessionManager, dbHelper
3. **loadData()** → Check network availability
4. **If online** → Call fetchDataFromBackend()
5. **If offline** → Show "No internet" message + clear lists
6. **fetchDataFromBackend()** → Make API call with response compatibility check
7. **onSuccess()** → Parse JSON, populate list, update UI
8. **onError()** → Show error message, clear list, update UI

---

## 🚨 CRITICAL RULES - NEVER BREAK

1. **NO LOCAL DB READS** - Database is for sync queue only, never for displaying data
2. **ALWAYS CHECK NETWORK** - Before making API calls
3. **INITIALIZE ALL FIELDS** - apiHelper and sessionManager in initViews()
4. **HANDLE BOTH RESPONSE FORMATS** - Check for "success"/"status" and array names
5. **OFFLINE = EMPTY STATE** - Show message, don't show stale data
6. **GET USER ID FROM SESSION** - Use sessionManager.getUserId() for API calls
7. **ERROR HANDLING** - Always handle onError() callback properly

---

## 🔍 AVAILABLE ENDPOINTS

### GET Endpoints (Working):
- `patients.php?asha_id=X` - Get patient list
- `visits.php?asha_id=X` - Get visit history
- `vaccinations.php?asha_id=X` - Get vaccinations
- `pregnancy_visits.php?patient_id=X` - Get pregnancy visits
- `child_growth.php?patient_id=X` - Get child growth records

### POST Endpoints:
- `auth.php` - Login (action=login)
- `patients.php` - Add/update patients (Working after getUserId() fix)
- `child_growth.php` - ⚠️ **BROKEN** - Backend has bind_param bug (11 params, 10 types)
- Other POST endpoints - Add/update operations

---

## ⚠️ COMMON BUGS & FIXES

**Bug:** Sync fails with "Invalid data format" for patient records
**Fix:** Use `getUserId()` (integer) not `getWorkerId()` (string) for asha_id field

**Bug:** child_growth sync fails with bind_param error
**Fix:** Backend bug in child_growth.php line 99 - bind_param has type mismatch. Cannot fix from app side. Backend needs fix: change 'iisiddddss' to have 11 characters or reduce bind variables to 10.

**Bug:** NullPointerException on sessionManager
**Fix:** Initialize sessionManager in initViews()

**Bug:** "Invalid data format" error
**Fix:** Handle both "success"/"status" and array name variations

**Bug:** "Cannot find symbol: ApiHelper"
**Fix:** Import from network package, not utils

**Bug:** App shows stale/cached data
**Fix:** Don't read from database, fetch from API

**Bug:** Type mismatch (double to float)
**Fix:** Cast explicitly when parsing JSON

---

## 📝 HOW TO USE THIS PROMPT

When requesting changes, paste this entire file and add:

```
My change request: [describe what you need]
```

This keeps the architecture consistent across all changes!

---

**Backend URL:** http://192.168.1.69/asha_api/
**Architecture:** Online-first, no local data display
**Last Updated:** February 6, 2026
