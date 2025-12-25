package com.simats.ashasmartcare.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

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

    private void startSync() {
        isSyncing = true;
        Log.d(TAG, "Starting sync...");

        new Thread(() -> {
            try {
                // Get all pending sync records
                List<SyncRecord> pendingRecords = dbHelper.getPendingSyncRecords();
                Log.d(TAG, "Found " + pendingRecords.size() + " pending records");

                for (SyncRecord record : pendingRecords) {
                    if (!NetworkUtils.isNetworkAvailable(this)) {
                        Log.d(TAG, "Network lost during sync");
                        break;
                    }

                    syncRecord(record);
                    
                    // Small delay between syncs
                    Thread.sleep(500);
                }

            } catch (Exception e) {
                Log.e(TAG, "Sync error: " + e.getMessage());
            } finally {
                isSyncing = false;
                stopSelf();
            }
        }).start();
    }

    private void syncRecord(SyncRecord record) {
        try {
            String tableName = record.getTableName();
            long recordId = record.getRecordId();

            switch (tableName) {
                case "patients":
                    syncPatient(recordId, record);
                    break;
                case "pregnancy_visits":
                    syncPregnancyVisit(recordId, record);
                    break;
                case "child_growth":
                    syncChildGrowth(recordId, record);
                    break;
                case "vaccinations":
                    syncVaccination(recordId, record);
                    break;
                case "visits":
                    syncVisit(recordId, record);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing record: " + e.getMessage());
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
        }
    }

    private void syncPatient(long patientId, SyncRecord record) {
        Patient patient = dbHelper.getPatientById(patientId);
        if (patient == null) {
            dbHelper.deleteSyncRecord(record.getId());
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("name", patient.getName());
            params.put("age", patient.getAge());
            params.put("gender", patient.getGender());
            params.put("phone", patient.getPhone());
            params.put("address", patient.getAddress());
            params.put("village", patient.getVillage());
            params.put("district", patient.getDistrict());
            params.put("state", patient.getState());
            params.put("category", patient.getCategory());
            params.put("blood_group", patient.getBloodGroup());
            params.put("emergency_contact", patient.getEmergencyContact());
            params.put("is_high_risk", patient.isHighRisk() ? 1 : 0);
            params.put("high_risk_reason", patient.getHighRiskReason());

            String url = Constants.API_BASE_URL + "patients.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            patient.setServerId((int) response.getLong("id"));
                            patient.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updatePatient(patient);
                        }
                        updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
        }
    }

    private void syncPregnancyVisit(long visitId, SyncRecord record) {
        PregnancyVisit visit = dbHelper.getPregnancyVisitById(visitId);
        if (visit == null) {
            dbHelper.deleteSyncRecord(record.getId());
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", visit.getPatientId());
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

            String url = Constants.API_BASE_URL + "pregnancy_visits.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            visit.setServerId((int) response.getLong("id"));
                            visit.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updatePregnancyVisit(visit);
                        }
                        updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
        }
    }

    private void syncChildGrowth(long growthId, SyncRecord record) {
        ChildGrowth growth = dbHelper.getChildGrowthById(growthId);
        if (growth == null) {
            dbHelper.deleteSyncRecord(record.getId());
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", growth.getPatientId());
            params.put("record_date", growth.getRecordDate());
            params.put("age_months", growth.getAgeMonths());
            params.put("weight", growth.getWeight());
            params.put("height", growth.getHeight());
            params.put("head_circumference", growth.getHeadCircumference());
            params.put("muac", growth.getMuac());
            params.put("nutritional_status", growth.getNutritionalStatus());
            params.put("milestones", growth.getMilestones());
            params.put("notes", growth.getNotes());

            String url = Constants.API_BASE_URL + "child_growth.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            growth.setServerId((int) response.getLong("id"));
                            growth.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updateChildGrowth(growth);
                        }
                        updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
        }
    }

    private void syncVaccination(long vaccineId, SyncRecord record) {
        Vaccination vaccination = dbHelper.getVaccinationById(vaccineId);
        if (vaccination == null) {
            dbHelper.deleteSyncRecord(record.getId());
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", vaccination.getPatientId());
            params.put("vaccine_name", vaccination.getVaccineName());
            params.put("scheduled_date", vaccination.getScheduledDate());
            params.put("given_date", vaccination.getGivenDate());
            params.put("status", vaccination.getStatus());
            params.put("batch_number", vaccination.getBatchNumber());
            params.put("side_effects", vaccination.getSideEffects());
            params.put("notes", vaccination.getNotes());

            String url = Constants.API_BASE_URL + "vaccinations.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            vaccination.setServerId((int) response.getLong("id"));
                            vaccination.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updateVaccination(vaccination);
                        }
                        updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
        }
    }

    private void syncVisit(long visitId, SyncRecord record) {
        Visit visit = dbHelper.getVisitById(visitId);
        if (visit == null) {
            dbHelper.deleteSyncRecord(record.getId());
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", visit.getPatientId());
            params.put("visit_type", visit.getVisitType());
            params.put("visit_date", visit.getVisitDate());
            params.put("purpose", visit.getPurpose());
            params.put("findings", visit.getFindings());
            params.put("recommendations", visit.getRecommendations());
            params.put("next_visit_date", visit.getNextVisitDate());
            params.put("notes", visit.getNotes());

            String url = Constants.API_BASE_URL + "visits.php";

            apiHelper.makeRequest(Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            visit.setServerId((int) response.getLong("id"));
                            visit.setSyncStatus(Constants.SYNC_SYNCED);
                            dbHelper.updateVisit(visit);
                        }
                        updateSyncStatus(record, Constants.SYNC_SYNCED, null);
                    } catch (Exception e) {
                        updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
                    }
                }

                @Override
                public void onError(String error) {
                    updateSyncStatus(record, Constants.SYNC_FAILED, error);
                }
            });
        } catch (Exception e) {
            updateSyncStatus(record, Constants.SYNC_FAILED, e.getMessage());
        }
    }

    private void updateSyncStatus(SyncRecord record, String status, String errorMessage) {
        record.setSyncStatus(status);
        record.setErrorMessage(errorMessage);
        record.setLastSyncAttempt(System.currentTimeMillis());
        dbHelper.updateSyncRecord(record);
        Log.d(TAG, "Updated sync record " + record.getId() + " to status: " + status);
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
