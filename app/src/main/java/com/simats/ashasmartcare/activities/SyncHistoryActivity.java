package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.SyncRecordAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.SyncRecord;

import java.util.ArrayList;
import java.util.List;

public class SyncHistoryActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView rvHistory;
    private SyncRecordAdapter historyAdapter;
    private List<SyncRecord> historyList;
    private DatabaseHelper dbHelper;
    private android.widget.LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_history);

        initViews();
        setupRecyclerView();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvHistory = findViewById(R.id.rvHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        dbHelper = DatabaseHelper.getInstance(this);

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        historyList = new ArrayList<>();
        historyAdapter = new SyncRecordAdapter(this, historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadData() {
        try {
            historyList.clear();
            List<SyncRecord> allRecords = dbHelper.getAllSyncRecords();

            // Critical Diagnostic Info
            String debugInfo = "DB Records: " + allRecords.size();
            android.util.Log.d("SyncHistory", debugInfo);

            for (SyncRecord record : allRecords) {
                String status = record.getSyncStatus();
                if (status != null)
                    status = status.trim();

                // Show synced records
                if ("SYNCED".equalsIgnoreCase(status)) {
                    record.setTitle(formatRecordTitle(record));
                    String syncTime = record.getLastUpdated();
                    record.setTimestamp(syncTime != null ? syncTime : record.getCreatedAt());
                    historyList.add(record);
                }
            }

            android.util.Log.d("SyncHistory", "Filtered to " + historyList.size() + " synced records");

            // Show diagnostic toast that persists longer
            android.widget.Toast.makeText(this, "Total: " + allRecords.size() + " | Synced: " + historyList.size(),
                    android.widget.Toast.LENGTH_LONG).show();

            if (historyList.isEmpty()) {
                rvHistory.setVisibility(android.view.View.GONE);
                layoutEmpty.setVisibility(android.view.View.VISIBLE);

                // SUPER DEBUG: If DB has records but history is empty, show them briefly to see
                // what's wrong
                if (!allRecords.isEmpty()) {
                    SyncRecord first = allRecords.get(0);
                    android.widget.Toast.makeText(this, "First record status: [" + first.getSyncStatus() + "]",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            } else {
                rvHistory.setVisibility(android.view.View.VISIBLE);
                layoutEmpty.setVisibility(android.view.View.GONE);
            }

            historyAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            String errorMsg = "Load Error: " + e.getMessage();
            android.util.Log.e("SyncHistory", errorMsg, e);
            android.widget.Toast.makeText(this, errorMsg, android.widget.Toast.LENGTH_LONG).show();
            rvHistory.setVisibility(android.view.View.GONE);
            layoutEmpty.setVisibility(android.view.View.VISIBLE);
        }
    }

    // Duplicated from SyncStatusActivity - consider moving to a helper
    private String formatRecordTitle(SyncRecord record) {
        String tableName = record.getTableName() != null ? record.getTableName() : "Unknown";
        String action = record.getAction() != null ? record.getAction() : "Update";
        long recordId = record.getRecordId();
        String patientName = null;

        try {
            if ("patients".equalsIgnoreCase(tableName)) {
                com.simats.ashasmartcare.models.Patient patient = dbHelper.getPatientById(recordId);
                if (patient != null) {
                    patientName = patient.getName();
                } else {
                    patientName = extractPatientNameFromJson(record.getDataJson());
                }
            } else if ("pregnancy_visits".equalsIgnoreCase(tableName)) {
                com.simats.ashasmartcare.models.PregnancyVisit visit = dbHelper.getPregnancyVisitById(recordId);
                if (visit != null) {
                    com.simats.ashasmartcare.models.Patient patient = dbHelper.getPatientById(visit.getPatientId());
                    if (patient != null)
                        patientName = patient.getName();
                }
            } else if ("child_growth".equalsIgnoreCase(tableName)) {
                com.simats.ashasmartcare.models.ChildGrowth growth = dbHelper.getChildGrowthById(recordId);
                if (growth != null) {
                    com.simats.ashasmartcare.models.Patient patient = dbHelper.getPatientById(growth.getPatientId());
                    if (patient != null)
                        patientName = patient.getName();
                }
            } else if ("vaccinations".equalsIgnoreCase(tableName)) {
                com.simats.ashasmartcare.models.Vaccination vaccination = dbHelper.getVaccinationById(recordId);
                if (vaccination != null) {
                    com.simats.ashasmartcare.models.Patient patient = dbHelper
                            .getPatientById(vaccination.getPatientId());
                    if (patient != null)
                        patientName = patient.getName();
                }
            } else if ("visits".equalsIgnoreCase(tableName)) {
                com.simats.ashasmartcare.models.Visit visit = dbHelper.getVisitById(recordId);
                if (visit != null) {
                    com.simats.ashasmartcare.models.Patient patient = dbHelper.getPatientById(visit.getPatientId());
                    if (patient != null)
                        patientName = patient.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (patientName != null && !patientName.isEmpty()) {
            String friendlyType = "Record";
            if ("patients".equalsIgnoreCase(tableName))
                friendlyType = "Patient";
            else if ("pregnancy_visits".equalsIgnoreCase(tableName))
                friendlyType = "Pregnancy Visit";
            else if ("child_growth".equalsIgnoreCase(tableName))
                friendlyType = "Child Growth";
            else if ("vaccinations".equalsIgnoreCase(tableName))
                friendlyType = "Vaccination";
            else if ("visits".equalsIgnoreCase(tableName))
                friendlyType = "General Visit";

            return friendlyType + ": " + patientName + " (" + action + ")";
        }

        return tableName + " - " + action;
    }

    private String extractPatientNameFromJson(String dataJson) {
        if (dataJson == null || dataJson.isEmpty())
            return null;
        try {
            org.json.JSONObject json = new org.json.JSONObject(dataJson);
            return json.optString("name", null);
        } catch (org.json.JSONException e) {
            return null;
        }
    }
}
