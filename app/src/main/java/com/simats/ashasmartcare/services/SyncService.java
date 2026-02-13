package com.simats.ashasmartcare.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.ChildGrowth;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.PregnancyVisit;
import com.simats.ashasmartcare.models.SyncRecord;
import com.simats.ashasmartcare.models.Vaccination;
import com.simats.ashasmartcare.models.Visit;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.Constants;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONObject;

import java.util.List;

public class SyncService extends Service {

    private static final String TAG = "SyncService";
    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private Handler handler;
    private boolean isSyncing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "SyncService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SyncService started");

        if (!isSyncing && NetworkUtils.isNetworkAvailable(this)) {
            startSync();
        } else {
            Log.d(TAG, "Sync skipped - already syncing or no network");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private java.util.concurrent.atomic.AtomicInteger pendingRequestCount = new java.util.concurrent.atomic.AtomicInteger(
            0);

    private void startSync() {
        isSyncing = true;
        Log.d(TAG, "Starting sync...");

        // Notify UI that sync has started
        Intent startedIntent = new Intent("com.simats.ashasmartcare.SYNC_STARTED");
        sendBroadcast(startedIntent);

        new Thread(() -> {
            try {
                // Clean up old SYNCED records first (older than 5 minutes)
                dbHelper.cleanupOldSyncedRecords();
                
                // Get all pending sync records
                List<SyncRecord> pendingRecords = dbHelper.getPendingSyncRecords();
                Log.d(TAG, "Found " + pendingRecords.size() + " pending records");

                if (pendingRecords.isEmpty()) {
                    isSyncing = false;

                    // Notify UI that sync has finished (nothing to do)
                    Intent finishedIntent = new Intent("com.simats.ashasmartcare.SYNC_FINISHED");
                    sendBroadcast(finishedIntent);

                    stopSelf();
                    return;
                }

                // Start sequential sync from the first record
                processSequentially(pendingRecords, 0);

            } catch (Exception e) {
                Log.e(TAG, "Sync error: " + e.getMessage());
                isSyncing = false;

                // Notify UI that sync has finished/failed
                Intent finishedIntent = new Intent("com.simats.ashasmartcare.SYNC_FINISHED");
                sendBroadcast(finishedIntent);

                stopSelf();
            }
        }).start();
    }

    private void processSequentially(List<SyncRecord> records, int index) {
        if (index >= records.size()) {
            Log.d(TAG, "All records processed");

            // Show single summary toast
            int syncedCount = 0;
            int failedCount = 0;
            for (SyncRecord record : records) {
                if ("SYNCED".equalsIgnoreCase(record.getSyncStatus())) {
                    syncedCount++;
                } else if ("FAILED".equalsIgnoreCase(record.getSyncStatus())) {
                    failedCount++;
                }
            }

            if (syncedCount > 0 || failedCount > 0) {
                final String message = syncedCount > 0
                        ? "✅ Synced " + syncedCount + " records"
                                + (failedCount > 0 ? " (" + failedCount + " failed)" : "")
                        : "❌ Sync failed for " + failedCount + " records";

                handler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }

            isSyncing = false;

            // Notify UI that sync has finished
            Intent finishedIntent = new Intent("com.simats.ashasmartcare.SYNC_FINISHED");
            sendBroadcast(finishedIntent);

            stopSelf();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.d(TAG, "Network lost, stopping sync");
            isSyncing = false;

            // Notify UI that sync has finished/interrupted
            Intent finishedIntent = new Intent("com.simats.ashasmartcare.SYNC_FINISHED");
            sendBroadcast(finishedIntent);

            stopSelf();
            return;
        }

        SyncRecord record = records.get(index);
        syncRecord(record, new SequentialCallback() {
            @Override
            public void onComplete() {
                // Process next record after a small delay
                handler.postDelayed(() -> processSequentially(records, index + 1), 200);
            }
        });
    }

    private interface SequentialCallback {
        void onComplete();
    }

    private void syncRecord(SyncRecord record, SequentialCallback callback) {
        try {
            String tableName = record.getTableName();
            long recordId = record.getRecordId();

            // Check if the source record is already marked as SYNCED
            // This happens if a duplicate queue record exists or if manual update occurred
            String sourceStatus = dbHelper.getSourceSyncStatus(tableName, recordId);
            if (Constants.SYNC_SYNCED.equalsIgnoreCase(sourceStatus)) {
                Log.d(TAG, "Record " + recordId + " in " + tableName + " is already SYNCED. Cleaning up queue.");
                updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                callback.onComplete();
                return;
            }

            switch (tableName) {
                case "patients":
                    syncPatient(recordId, record, callback);
                    break;
                case "pregnancy_visits":
                    syncPregnancyVisit(recordId, record, callback);
                    break;
                case "child_growth":
                    syncChildGrowth(recordId, record, callback);
                    break;
                case "vaccinations":
                    syncVaccination(recordId, record, callback);
                    break;
                case "visits":
                    syncVisit(recordId, record, callback);
                    break;
                default:
                    callback.onComplete();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing record: " + e.getMessage());
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
            callback.onComplete();
        }
    }

    private void syncPatient(long patientId, SyncRecord record, SequentialCallback callback) {
        Patient patient = dbHelper.getPatientById(patientId);
        if (patient == null) {
            dbHelper.deleteSyncRecord(record.getId());
            callback.onComplete();
            return;
        }

        try {
            JSONObject params;
            String dataJson = record.getDataJson();

            if (dataJson != null && !dataJson.isEmpty()) {
                params = new JSONObject(dataJson);
                Log.d(TAG, "Using existing JSON for patient sync");
                // Ensure critical fields are present
                if (!params.has("local_id")) {
                    params.put("local_id", patient.getLocalId());
                }
                if (!params.has("asha_id")) {
                    params.put("asha_id", SessionManager.getInstance(this).getUserId());
                }
            } else {
                params = new JSONObject();
                params.put("local_id", patient.getLocalId());
                params.put("asha_id", SessionManager.getInstance(this).getUserId());
                params.put("name", patient.getName());
                params.put("age", patient.getAge());
                params.put("dob", patient.getDob());
                params.put("gender", patient.getGender());
                params.put("phone", patient.getPhone());
                params.put("address", patient.getAddress());
                params.put("category", patient.getCategory());
                params.put("blood_group", patient.getBloodGroup());
                params.put("is_high_risk", patient.isHighRisk() ? 1 : 0);
                params.put("high_risk_reason", patient.getHighRiskReason());
                params.put("medical_notes", patient.getMedicalNotes());
            }

            String baseUrl = SessionManager.getInstance(this).getApiBaseUrl();
            String url = baseUrl + "patients.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            // Try to get ID from multiple possible locations
                            int serverId = 0;
                            if (response.has("id")) {
                                serverId = response.optInt("id", 0);
                            } else if (response.has("data")) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    serverId = data.optInt("id", 0);
                                }
                            }

                            if (serverId > 0) {
                                patient.setServerId(serverId);
                            }
                            patient.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updatePatient(patient, false);
                            updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                        } else {
                            String error = response.optString("message", "Invalid response from server");
                            updateSyncStatus(record, Constants.SYNC_FAILED, error);
                        }
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    } finally {
                        callback.onComplete();
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                    callback.onComplete();
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
            callback.onComplete();
        }
    }

    private void syncPregnancyVisit(long visitId, SyncRecord record, SequentialCallback callback) {
        PregnancyVisit visit = dbHelper.getPregnancyVisitById(visitId);
        if (visit == null) {
            dbHelper.deleteSyncRecord(record.getId());
            callback.onComplete();
            return;
        }

        // Get parent patient to get server side ID
        Patient patient = dbHelper.getPatientById(visit.getPatientId());
        if (patient == null || patient.getServerId() == 0) {
            updateSyncStatus(record, Constants.SYNC_FAILED, "Parent patient must be synced first");
            callback.onComplete();
            return;
        }

        try {
            JSONObject params;
            String dataJson = record.getDataJson();
            String baseUrl = SessionManager.getInstance(this).getApiBaseUrl();
            String url;

            if (dataJson != null && !dataJson.isEmpty()) {
                params = new JSONObject(dataJson);
                // Important: Update with the actual server-side patient ID
                params.put("patient_id", patient.getServerId());
                // Add local_id for tracing
                if (!params.has("local_id")) {
                    params.put("local_id", visit.getLocalId());
                }

                // Pregnancy data might go to pregnancy.php (initial) or pregnancy_visits.php
                // (follow-up)
                // AddPatientActivity uses pregnancy.php for the first record
                url = baseUrl + (params.has("lmp_date") ? "pregnancy.php" : "pregnancy_visits.php");
                Log.d(TAG, "Using existing JSON for pregnancy sync. URL: " + url);
            } else {
                params = new JSONObject();
                params.put("local_id", visit.getLocalId());
                params.put("patient_id", patient.getServerId());
                params.put("visit_date", visit.getVisitDate());
                params.put("gestational_weeks", visit.getGestationalWeeks());
                params.put("weight", visit.getWeight());
                params.put("blood_pressure", visit.getBloodPressure());
                params.put("hemoglobin", visit.getHemoglobin());
                params.put("fetal_heart_rate", visit.getFetalHeartRate());
                params.put("urine_protein", visit.getUrineProtein());
                params.put("urine_sugar", visit.getUrineSugar());
                params.put("is_high_risk", visit.isHighRisk() ? 1 : 0);
                params.put("high_risk_reason", visit.getHighRiskReason());
                params.put("notes", visit.getNotes());
                url = baseUrl + "pregnancy_visits.php";
            }

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            // Try to get ID from multiple possible locations
                            int serverId = 0;
                            if (response.has("id")) {
                                serverId = response.optInt("id", 0);
                            } else if (response.has("data")) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    serverId = data.optInt("id", 0);
                                }
                            }

                            if (serverId > 0) {
                                visit.setServerId(serverId);
                            }
                            visit.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updatePregnancyVisit(visit, false);
                            updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                        } else {
                            String error = response.optString("message", "Invalid response from server");
                            updateSyncStatus(record, Constants.SYNC_FAILED, error);
                        }
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    } finally {
                        callback.onComplete();
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                    callback.onComplete();
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
            callback.onComplete();
        }
    }

    private void syncChildGrowth(long growthId, SyncRecord record, SequentialCallback callback) {
        ChildGrowth growth = dbHelper.getChildGrowthById(growthId);
        if (growth == null) {
            dbHelper.deleteSyncRecord(record.getId());
            callback.onComplete();
            return;
        }

        // Get parent patient
        Patient patient = dbHelper.getPatientById(growth.getPatientId());
        if (patient == null || patient.getServerId() == 0) {
            updateSyncStatus(record, Constants.SYNC_FAILED, "Parent patient must be synced first");
            callback.onComplete();
            return;
        }

        try {
            JSONObject params;
            String dataJson = record.getDataJson();

            if (dataJson != null && !dataJson.isEmpty()) {
                params = new JSONObject(dataJson);
                // Important: Update with the actual server-side patient ID
                params.put("patient_id", patient.getServerId());
                // Add local_id for tracing
                if (!params.has("local_id")) {
                    params.put("local_id", growth.getLocalId());
                }
                Log.d(TAG, "Using existing JSON for child growth sync");
            } else {
                params = new JSONObject();
                params.put("local_id", growth.getLocalId());
                params.put("patient_id", patient.getServerId());
                params.put("record_date", growth.getRecordDate());
                params.put("age_months", growth.getAgeMonths());
                params.put("weight", growth.getWeight());
                params.put("height", growth.getHeight());
                params.put("head_circumference", growth.getHeadCircumference());
                params.put("muac", growth.getMuac());
                params.put("growth_status", growth.getGrowthStatus());
                params.put("notes", growth.getNotes());
            }

            String baseUrl = SessionManager.getInstance(this).getApiBaseUrl();
            String url = baseUrl + "child_growth.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            // Try to get ID from multiple possible locations
                            int serverId = 0;
                            if (response.has("id")) {
                                serverId = response.optInt("id", 0);
                            } else if (response.has("data")) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    serverId = data.optInt("id", 0);
                                }
                            }

                            if (serverId > 0) {
                                growth.setServerId(serverId);
                            }
                            growth.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updateChildGrowth(growth, false);
                            updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                        } else {
                            String error = response.optString("message", "Operation failed on server");
                            updateSyncStatus(record, Constants.SYNC_FAILED, error);
                        }
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    } finally {
                        callback.onComplete();
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                    callback.onComplete();
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
            callback.onComplete();
        }
    }

    private void syncVaccination(long vaccineId, SyncRecord record, SequentialCallback callback) {
        Vaccination vaccination = dbHelper.getVaccinationById(vaccineId);
        if (vaccination == null) {
            dbHelper.deleteSyncRecord(record.getId());
            callback.onComplete();
            return;
        }

        // Get parent patient
        Patient patient = dbHelper.getPatientById(vaccination.getPatientId());
        if (patient == null || patient.getServerId() == 0) {
            updateSyncStatus(record, Constants.SYNC_FAILED, "Parent patient must be synced first");
            callback.onComplete();
            return;
        }

        try {
            // ... (params setup)
            JSONObject params = new JSONObject();
            params.put("local_id", vaccination.getLocalId());
            params.put("patient_id", patient.getServerId());
            params.put("vaccine_name", vaccination.getVaccineName());
            params.put("due_date", vaccination.getDueDate());
            params.put("given_date", vaccination.getGivenDate());
            params.put("status", vaccination.getStatus());
            params.put("batch_number", vaccination.getBatchNumber());
            params.put("notes", vaccination.getNotes());

            String baseUrl = SessionManager.getInstance(this).getApiBaseUrl();
            String url = baseUrl + "vaccinations.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            // Try to get ID from multiple possible locations
                            int serverId = 0;
                            if (response.has("id")) {
                                serverId = response.optInt("id", 0);
                            } else if (response.has("data")) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    serverId = data.optInt("id", 0);
                                }
                            }

                            if (serverId > 0) {
                                vaccination.setServerId(serverId);
                            }
                            vaccination.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updateVaccination(vaccination, false);
                            updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                        } else {
                            String error = response.optString("message", "Operation failed on server");
                            updateSyncStatus(record, Constants.SYNC_FAILED, error);
                        }
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    } finally {
                        callback.onComplete();
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                    callback.onComplete();
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
            callback.onComplete();
        }
    }

    private void syncVisit(long visitId, SyncRecord record, SequentialCallback callback) {
        Visit visit = dbHelper.getVisitById(visitId);
        if (visit == null) {
            dbHelper.deleteSyncRecord(record.getId());
            callback.onComplete();
            return;
        }

        // Get parent patient
        Patient patient = dbHelper.getPatientById(visit.getPatientId());
        if (patient == null || patient.getServerId() == 0) {
            updateSyncStatus(record, Constants.SYNC_FAILED, "Parent patient must be synced first");
            callback.onComplete();
            return;
        }

        try {
            JSONObject params;
            String dataJson = record.getDataJson();
            String baseUrl = SessionManager.getInstance(this).getApiBaseUrl();
            String url;

            if (dataJson != null && !dataJson.isEmpty()) {
                params = new JSONObject(dataJson);
                // Important: Update with the actual server-side patient ID
                params.put("patient_id", patient.getServerId());
                // Add local_id for tracing
                if (!params.has("local_id")) {
                    params.put("local_id", visit.getLocalId());
                }

                // General adult data might go to general_adult.php (initial/specialized) or
                // visits.php (generic)
                url = baseUrl + (params.has("tobacco_use") ? "general_adult.php" : "visits.php");
                Log.d(TAG, "Using existing JSON for visit sync. URL: " + url);
            } else {
                params = new JSONObject();
                params.put("local_id", visit.getLocalId());
                params.put("patient_id", patient.getServerId());
                params.put("visit_type", visit.getVisitType());
                params.put("visit_date", visit.getVisitDate());
                params.put("purpose", visit.getPurpose());
                params.put("findings", visit.getFindings());
                params.put("recommendations", visit.getRecommendations());
                params.put("next_visit_date", visit.getNextVisitDate());
                params.put("notes", visit.getNotes());
                url = baseUrl + "visits.php";
            }

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            // Try to get ID from multiple possible locations
                            int serverId = 0;
                            if (response.has("id")) {
                                serverId = response.optInt("id", 0);
                            } else if (response.has("data")) {
                                JSONObject data = response.optJSONObject("data");
                                if (data != null) {
                                    serverId = data.optInt("id", 0);
                                }
                            }

                            if (serverId > 0) {
                                visit.setServerId(serverId);
                            }
                            visit.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updateVisit(visit, false);
                            updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                        } else {
                            String error = response.optString("message", "Operation failed on server");
                            updateSyncStatus(record, Constants.SYNC_FAILED, error);
                        }
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    } finally {
                        callback.onComplete();
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                    callback.onComplete();
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
            callback.onComplete();
        }
    }

    private void updateSyncStatus(SyncRecord record, String status, String errorMessage) {
        record.setSyncStatus(status);
        record.setErrorMessage(errorMessage);
        record.setLastSyncAttempt(System.currentTimeMillis());

        if ("SYNCED".equals(status)) {
            // Delete ALL sync records for this entity after successful sync to avoid
            // duplicates
            dbHelper.deleteSyncRecordsForEntity(record.getTableName(), record.getRecordId());
            Log.d(TAG, "Deleted all sync records for " + record.getTableName() + " ID " + record.getRecordId()
                    + " after successful sync");
        } else {
            // Only update if failed/pending
            int rows = dbHelper.updateSyncRecord(record);
            Log.d(TAG, "Updated sync record " + record.getId() + " to " + status + ". Rows affected: " + rows);
        }

        Log.d(TAG, "Updated sync record " + record.getId() + " to status: " + status);

        // Notify UI of the update with specific record info
        Intent intent = new Intent("com.simats.ashasmartcare.SYNC_UPDATE");
        intent.putExtra("record_id", record.getId());
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SyncService destroyed");
    }
}
