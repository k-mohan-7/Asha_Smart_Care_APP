package com.simats.ashasmartcare.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.services.SyncService;
import com.simats.ashasmartcare.services.NetworkMonitorService;
import com.simats.ashasmartcare.utils.ConnectionStatusManager;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;
import com.simats.ashasmartcare.PatientsActivity;
import com.simats.ashasmartcare.VaccinationListActivity;
import com.simats.ashasmartcare.VisitHistoryActivity;

import com.simats.ashasmartcare.ProfileActivity;
import com.simats.ashasmartcare.AIInsightsActivity;
import com.simats.ashasmartcare.PatientAlertsActivity;
import com.simats.ashasmartcare.AddPatientActivity;
import com.simats.ashasmartcare.SettingsActivity;

/**
 * Home Activity - Main Dashboard
 * Modern UI with stats cards and navigation
 */
public class HomeActivity extends BaseActivity {

    @Override
    protected int getNavItemId() {
        return R.id.nav_home;
    }

    private TextView tvUserName, tvVillage, tvConnectionStatus;
    private TextView tvVisitsToday, tvHighRisk;
    private CardView badgeConnection;
    private CardView cardNewPatient, cardPatients, cardVaccinations;
    private CardView cardAIInsights, cardSync, cardSettings;
    private BottomNavigationView bottomNavigation;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ConnectionStatusManager connectionStatusManager;
    private BroadcastReceiver networkReceiver;
    private BroadcastReceiver syncUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initHelpers();
        setupNetworkMonitoring();
        setupListeners();
        loadUserData();
        checkOnlineMode();
        initSyncReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Clean up old synced records to prevent stale warnings
        dbHelper.cleanupOldSyncedRecords();
        
        updateDashboard();
        checkOnlineMode();

        // Register sync receiver
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction("com.simats.ashasmartcare.SYNC_UPDATE");
        syncFilter.addAction("com.simats.ashasmartcare.SYNC_FINISHED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(syncUpdateReceiver, syncFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(syncUpdateReceiver, syncFilter);
        }
    }

    protected void onPause() {
        super.onPause();
        if (syncUpdateReceiver != null) {
            unregisterReceiver(syncUpdateReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvVillage = findViewById(R.id.tv_village);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        badgeConnection = findViewById(R.id.badge_connection);

        tvVisitsToday = findViewById(R.id.tv_visits_today);
        tvHighRisk = findViewById(R.id.tv_high_risk);

        cardNewPatient = findViewById(R.id.card_new_patient);
        cardPatients = findViewById(R.id.card_patients);
        cardVaccinations = findViewById(R.id.card_vaccinations);
        cardAIInsights = findViewById(R.id.card_ai_insights);
        cardSync = findViewById(R.id.card_sync);
        cardSettings = findViewById(R.id.card_settings);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Header stat cards
        findViewById(R.id.card_visits_stat).setOnClickListener(
                v -> startActivity(new Intent(this, VisitHistoryActivity.class)));
        findViewById(R.id.card_high_risk_stat).setOnClickListener(
                v -> startActivity(new Intent(this, PatientAlertsActivity.class)));
    }

    private void initHelpers() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        connectionStatusManager = ConnectionStatusManager.getInstance(this);
    }

    private void setupNetworkMonitoring() {
        // Start network monitoring service
        Intent serviceIntent = new Intent(this, NetworkMonitorService.class);
        startService(serviceIntent);

        // Set initial network state
        boolean isConnected = NetworkMonitorService.isNetworkConnected(this);
        connectionStatusManager.setInitialState(isConnected);

        // Register broadcast receiver for network changes
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = intent.getBooleanExtra(
                        NetworkMonitorService.EXTRA_IS_CONNECTED, false);
                connectionStatusManager.showNetworkStatus(isConnected);
                checkOnlineMode();
            }
        };

        IntentFilter filter = new IntentFilter(NetworkMonitorService.ACTION_NETWORK_CHANGED);

        // Android 13+ requires explicit receiver export flag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(networkReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(networkReceiver, filter);
        }
    }

    private void initSyncReceiver() {
        syncUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("com.simats.ashasmartcare.SYNC_FINISHED".equals(action)) {
                    // Full refresh after sync completes
                    runOnUiThread(() -> {
                        updateDashboard();
                        android.util.Log.d("HomeActivity", "Dashboard refreshed after sync completion");
                    });
                } else if ("com.simats.ashasmartcare.SYNC_UPDATE".equals(action)) {
                    // Incremental update during sync
                    runOnUiThread(() -> updateDashboard());
                }
            }
        };
    }

    private void setupListeners() {
        cardNewPatient.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPatientActivity.class));
        });

        cardPatients.setOnClickListener(v -> {
            startActivity(new Intent(this, PatientsActivity.class));
        });

        cardVaccinations.setOnClickListener(v -> {
            startActivity(new Intent(this, VaccinationListActivity.class));
        });

        cardAIInsights.setOnClickListener(v -> {
            startActivity(new Intent(this, AIInsightsActivity.class));
        });

        cardSync.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                performSync();
            } else {
                startActivity(new Intent(this, SyncStatusActivity.class));
            }
        });

        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Bottom navigation is handled by BaseActivity
    }

    private void loadUserData() {
        // Get user name from database (registered name)
        String userName = dbHelper.getLoggedInUserName();
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText(userName);
        } else {
            // Fallback to session manager
            userName = sessionManager.getUserName();
            if (userName != null && !userName.isEmpty() && !userName.equals("ASHA Worker")) {
                tvUserName.setText(userName);
            } else {
                tvUserName.setText("ASHA Worker");
            }
        }

        // Get village/area from database first
        String area = dbHelper.getLoggedInUserArea();
        if (area != null && !area.isEmpty()) {
            tvVillage.setText("Village: " + area);
        } else {
            // Fallback to session manager
            area = sessionManager.getUserLocation();
            if (area != null && !area.isEmpty()) {
                tvVillage.setText("Village: " + area);
            } else {
                String district = sessionManager.getUserDistrict();
                if (district != null && !district.isEmpty()) {
                    tvVillage.setText("District: " + district);
                } else {
                    tvVillage.setText("Village: Not Set");
                }
            }
        }
    }

    private void checkOnlineMode() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);

        if (isOnline) {
            tvConnectionStatus.setText("Online");
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.status_synced));

            // Clean up old synced records first
            dbHelper.cleanupOldSyncedRecords();
            
            // Auto-sync if there are pending records
            int pendingCount = dbHelper.getTotalPendingRecords();
            if (pendingCount > 0) {
                performSyncInBackground();
            }
        } else {
            tvConnectionStatus.setText("Offline");
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.status_error));
        }
    }

    private void updateDashboard() {
        // Update visits today (mock data - replace with actual logic)
        int visitsToday = dbHelper.getVisitsCountToday();
        tvVisitsToday.setText(String.valueOf(visitsToday));

        // Update high risk count
        int highRiskCount = dbHelper.getHighRiskPatientsCount();
        tvHighRisk.setText(String.valueOf(highRiskCount));
    }

    private void performSync() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Syncing...", Toast.LENGTH_SHORT).show();

        // Start sync service
        Intent syncIntent = new Intent(this, SyncService.class);
        startService(syncIntent);

        // Navigate to sync status
        startActivity(new Intent(this, SyncStatusActivity.class));
    }

    private void performSyncInBackground() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Intent syncIntent = new Intent(this, SyncService.class);
            startService(syncIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
