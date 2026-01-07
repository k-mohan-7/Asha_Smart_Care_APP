package com.simats.ashasmartcare.activities;

import android.content.Intent;
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
public class HomeActivity extends AppCompatActivity {

    private TextView tvUserName, tvVillage, tvConnectionStatus;
    private TextView tvVisitsToday, tvHighRisk, tvPendingSync;
    private CardView badgeConnection;
    private CardView cardNewPatient, cardPatients, cardVaccinations;
    private CardView cardAIInsights, cardSync, cardSettings;
    private BottomNavigationView bottomNavigation;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initHelpers();
        setupListeners();
        loadUserData();
        checkOnlineMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
        checkOnlineMode();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvVillage = findViewById(R.id.tv_village);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        badgeConnection = findViewById(R.id.badge_connection);

        tvVisitsToday = findViewById(R.id.tv_visits_today);
        tvHighRisk = findViewById(R.id.tv_high_risk);
        tvPendingSync = findViewById(R.id.tv_pending_sync);

        cardNewPatient = findViewById(R.id.card_new_patient);
        cardPatients = findViewById(R.id.card_patients);
        cardVaccinations = findViewById(R.id.card_vaccinations);
        cardAIInsights = findViewById(R.id.card_ai_insights);
        cardSync = findViewById(R.id.card_sync);
        cardSettings = findViewById(R.id.card_settings);

        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void initHelpers() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
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

        bottomNavigation
                .setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.nav_home) {
                            return true;
                        } else if (id == R.id.nav_profile) {
                            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                            return true;
                        } else if (id == R.id.nav_visits) {
                            startActivity(new Intent(HomeActivity.this, VisitHistoryActivity.class));
                            return true;
                        } else if (id == R.id.nav_alerts) {
                            startActivity(new Intent(HomeActivity.this, PatientAlertsActivity.class));
                            return true;
                        }

                        return false;
                    }
                });

        // Set home as selected
        bottomNavigation.setSelectedItemId(R.id.nav_home);
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

        // Update pending sync count
        int pendingCount = dbHelper.getTotalPendingRecords();
        tvPendingSync.setText(String.valueOf(pendingCount));
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
