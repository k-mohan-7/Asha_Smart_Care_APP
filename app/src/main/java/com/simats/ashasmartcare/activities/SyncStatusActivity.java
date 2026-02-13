package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.SyncRecordAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.SyncRecord;
import com.simats.ashasmartcare.services.SyncService;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SyncStatusActivity extends AppCompatActivity implements SyncRecordAdapter.OnDeleteClickListener {

    private ImageView ivBack;
    private TextView tvLastSynced, tvPendingCount, btnClearAll;
    private LinearLayout btnSyncNow;
    private RecyclerView rvPending;
    private ProgressBar pbSyncing;
    private ImageView ivSyncIcon;
    private TextView tvSyncButtonText;

    private SyncRecordAdapter pendingAdapter;
    private List<SyncRecord> pendingList;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private java.util.Set<Long> recentlySyncedIds = new java.util.HashSet<>();
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable loadDataRunnable = this::loadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_status);

        initViews();
        setupRecyclerViews();
        loadData();
        setupListeners();
        setupSyncReceiver();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvLastSynced = findViewById(R.id.tvLastSynced);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        rvPending = findViewById(R.id.rvPending);

        btnClearAll = findViewById(R.id.btnClearAll);
        pbSyncing = findViewById(R.id.pbSyncing);
        ivSyncIcon = findViewById(R.id.ivSyncIcon);
        tvSyncButtonText = findViewById(R.id.tvSyncButtonText);

        sessionManager = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void setupRecyclerViews() {
        pendingList = new ArrayList<>();
        pendingAdapter = new SyncRecordAdapter(this, pendingList, this);
        // syncedList removed

        rvPending.setLayoutManager(new LinearLayoutManager(this));
        rvPending.setAdapter(pendingAdapter);

        // Disable nested scrolling to allow ScrollView to handle it
        rvPending.setNestedScrollingEnabled(false);
    }

    private void setupSyncReceiver() {
        syncReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, android.content.Intent intent) {
                String action = intent.getAction();
                if ("com.simats.ashasmartcare.SYNC_FINISHED".equals(action) || 
                    "com.simats.ashasmartcare.SYNC_UPDATE".equals(action)) {
                    // Refresh the list when sync completes or updates
                    runOnUiThread(() -> {
                        throttledLoadData();
                        android.util.Log.d("SyncStatusActivity", "Refreshed sync status after broadcast: " + action);
                    });
                }
            }
        };

        android.content.IntentFilter filter = new android.content.IntentFilter();
        filter.addAction("com.simats.ashasmartcare.SYNC_FINISHED");
        filter.addAction("com.simats.ashasmartcare.SYNC_UPDATE");
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(syncReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(syncReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncReceiver != null) {
            try {
                unregisterReceiver(syncReceiver);
            } catch (Exception e) {
                // Receiver might not be registered
            }
        }
    }

    private void loadData() {
        // Clear existing list
        pendingList.clear();

        // Get ALL sync records from database
        List<SyncRecord> allRecords = dbHelper.getAllSyncRecords();

        // Filter for PENDING/FAILED only, but include RECENTLY SYNCED
        // Filter for PENDING/FAILED only, but include RECENTLY SYNCED
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000);

        for (SyncRecord record : allRecords) {
            boolean isSynced = "SYNCED".equalsIgnoreCase(record.getSyncStatus());

            // Check if synced recently (within last 5 minutes) via timestamp
            boolean isRecentlySyncedTime = false;
            try {
                if (isSynced && record.getLastUpdated() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(record.getLastUpdated());
                    if (date != null && date.getTime() > fiveMinutesAgo) {
                        isRecentlySyncedTime = true;
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors
            }

            boolean isRecentlySynced = recentlySyncedIds.contains(record.getLocalId()) || isRecentlySyncedTime;

            if (!isSynced || isRecentlySynced) {
                // Populate UI-specific fields on the original record object
                record.setTitle(formatRecordTitle(record));
                record.setTimestamp(formatTimestamp(record.getCreatedAt()));

                // Ensure syncStatus is correctly set for the adapter
                if (isRecentlySynced) {
                    record.setSyncStatus("SYNCED");
                    record.setSynced(true); // Update boolean field for adapter
                } else {
                    // Update boolean field based on syncStatus string
                    record.setSynced("SYNCED".equalsIgnoreCase(record.getSyncStatus()));
                }

                pendingList.add(record);
            }
        }

        pendingAdapter.notifyDataSetChanged();

        updateSummaryInternal();
    }

    private void throttledLoadData() {
        uiHandler.removeCallbacks(loadDataRunnable);
        uiHandler.postDelayed(loadDataRunnable, 500); // Max once every 500ms
    }

    private void updateSummaryInternal() {
        int actualPending = 0;
        for (SyncRecord r : pendingList) {
            if (!"SYNCED".equalsIgnoreCase(r.getSyncStatus())) {
                actualPending++;
            }
        }

        tvLastSynced.setText("Check History for past syncs");
        tvPendingCount.setText(actualPending + " records pending upload");

        // Show/Hide Clear All button
        btnClearAll.setVisibility(pendingList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDeleteClick(SyncRecord record) {
        String _message = "Are you sure you want to remove this record from the sync queue? The data will remain on your phone but will NOT be uploaded to the server.";
        String _positiveButtonText = "Remove from Queue";

        // If it's a failed insertion, offer full deletion
        if ("INSERT".equalsIgnoreCase(record.getAction()) || record.getTableName() != null) {
            _message = "Do you want to permanently remove this " + getReadableTableName(record.getTableName())
                    + " record? This will delete it from both the sync queue and your local database.";
            _positiveButtonText = "Delete Permanently";
        }

        final String finalMessage = _message;
        final String finalPositiveButtonText = _positiveButtonText;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage(finalMessage)
                .setPositiveButton(finalPositiveButtonText, (dialog, which) -> {
                    boolean fullyDeleted = false;
                    if (finalPositiveButtonText.equals("Delete Permanently")) {
                        fullyDeleted = deleteActualRecord(record);
                    }

                    if (!fullyDeleted) {
                        dbHelper.deleteSyncRecord(record.getLocalId());
                    }

                    Toast.makeText(this, "Record removed permanently", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getReadableTableName(String tableName) {
        if (tableName == null)
            return "record";
        switch (tableName) {
            case "patients":
                return "Patient";
            case "pregnancy_visits":
                return "Pregnancy Visit";
            case "child_growth":
                return "Child Growth";
            case "vaccinations":
                return "Vaccination";
            case "visits":
                return "Visit";
            default:
                return "Record";
        }
    }

    private boolean deleteActualRecord(SyncRecord record) {
        String tableName = record.getTableName();
        long recordId = record.getRecordId();

        if (tableName == null || recordId <= 0)
            return false;

        switch (tableName) {
            case "patients":
                dbHelper.deletePatient(recordId);
                return true;
            case "pregnancy_visits":
                dbHelper.deletePregnancyVisit(recordId);
                return true;
            case "child_growth":
                dbHelper.deleteChildGrowth(recordId);
                return true;
            case "vaccinations":
                dbHelper.deleteVaccinationRecord(recordId);
                return true;
            case "visits":
                dbHelper.deleteVisit(recordId);
                return true;
            default:
                return false;
        }
    }

    private String formatRecordTitle(SyncRecord record) {
        // ... (Existing implementation kept as is)
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
        if (dataJson == null || dataJson.isEmpty()) {
            return null;
        }

        try {
            int nameIndex = dataJson.indexOf("\"name\"");
            if (nameIndex != -1) {
                int colonIndex = dataJson.indexOf(":", nameIndex);
                int startQuote = dataJson.indexOf("\"", colonIndex);
                int endQuote = dataJson.indexOf("\"", startQuote + 1);

                if (startQuote != -1 && endQuote != -1 && endQuote > startQuote) {
                    return dataJson.substring(startQuote + 1, endQuote);
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private String formatTimestamp(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Unknown time";
        }
        return createdAt;
    }

    private android.content.BroadcastReceiver syncReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.simats.ashasmartcare.SYNC_UPDATE".equals(action)) {
                long recordId = intent.getLongExtra("record_id", -1);
                String status = intent.getStringExtra("status");

                if (recordId != -1 && "SYNCED".equalsIgnoreCase(status)) {
                    recentlySyncedIds.add(recordId);
                    runOnUiThread(() -> throttledLoadData());
                } else {
                    runOnUiThread(() -> throttledLoadData());
                }
            } else if ("com.simats.ashasmartcare.SYNC_STARTED".equals(action)) {
                runOnUiThread(() -> setSyncingState(true));
            } else if ("com.simats.ashasmartcare.SYNC_FINISHED".equals(action)) {
                runOnUiThread(() -> {
                    setSyncingState(false);
                    loadData();
                });
            }
        }
    };

    private void setSyncingState(boolean isSyncing) {
        if (isSyncing) {
            pbSyncing.setVisibility(View.VISIBLE);
            ivSyncIcon.setVisibility(View.GONE);
            tvSyncButtonText.setText("Syncing...");
            btnSyncNow.setEnabled(false);
            btnSyncNow.setAlpha(0.7f);
        } else {
            pbSyncing.setVisibility(View.GONE);
            ivSyncIcon.setVisibility(View.VISIBLE);
            tvSyncButtonText.setText("Sync Now");
            btnSyncNow.setEnabled(true);
            btnSyncNow.setAlpha(1.0f);
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSyncNow.setOnClickListener(v -> {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start sync service
            Intent syncIntent = new Intent(this, SyncService.class);
            startService(syncIntent);

            Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show();
            loadData();
        });

        btnClearAll.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Clear All Pending Records")
                    .setMessage(
                            "Are you sure you want to remove ALL records from the sync queue? The data will remain on your phone but will NOT be uploaded to the server.")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        dbHelper.clearAllPendingRecords();
                        Toast.makeText(this, "Sync queue cleared", Toast.LENGTH_SHORT).show();
                        loadData();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register receiver
        android.content.IntentFilter filter = new android.content.IntentFilter();
        filter.addAction("com.simats.ashasmartcare.SYNC_UPDATE");
        filter.addAction("com.simats.ashasmartcare.SYNC_STARTED");
        filter.addAction("com.simats.ashasmartcare.SYNC_FINISHED");

        // For Android 14 (API 34) and above, specify RECEIVER_NOT_EXPORTED for internal
        // broadcasts
        if (android.os.Build.VERSION.SDK_INT >= 34) { // 34 is UPSIDE_DOWN_CAKE
            registerReceiver(syncReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(syncReceiver, filter);
        }

        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(syncReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

}
