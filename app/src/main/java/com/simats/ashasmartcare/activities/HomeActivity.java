package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.services.SyncService;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;
import com.simats.ashasmartcare.PatientListActivity;
import com.simats.ashasmartcare.PregnancyListActivity;
import com.simats.ashasmartcare.ChildGrowthListActivity;
import com.simats.ashasmartcare.VaccinationListActivity;
import com.simats.ashasmartcare.VisitHistoryActivity;
import com.simats.ashasmartcare.SyncStatusActivity;
import com.simats.ashasmartcare.ProfileActivity;
import com.simats.ashasmartcare.AIInsightsActivity;
import com.simats.ashasmartcare.PatientAlertsActivity;

/**
 * Home Activity - Main Dashboard
 * Shows dashboard cards for navigation to different modules
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvWelcome, tvLocation, tvSyncStatus, tvPendingCount;
    private ImageView ivProfile;
    private MaterialButton btnSyncNow;
    
    // Dashboard Cards
    private CardView cardPatients, cardPregnancy, cardChildGrowth, cardVaccination;
    private CardView cardHistory, cardSync, cardProfile, cardAIInsights;
    private CardView cardAlerts;

    private LinearLayout syncStatusBar;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSyncStatus();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvLocation = findViewById(R.id.tv_location);
        tvSyncStatus = findViewById(R.id.tv_sync_status);
        tvPendingCount = findViewById(R.id.tv_pending_count);
        ivProfile = findViewById(R.id.iv_profile);
        btnSyncNow = findViewById(R.id.btn_sync_now);
        syncStatusBar = findViewById(R.id.sync_status_bar);

        // Dashboard Cards
        cardPatients = findViewById(R.id.card_patients);
        cardPregnancy = findViewById(R.id.card_pregnancy);
        cardChildGrowth = findViewById(R.id.card_child_growth);
        cardVaccination = findViewById(R.id.card_vaccination);
        cardHistory = findViewById(R.id.card_history);
        cardSync = findViewById(R.id.card_sync);
        cardProfile = findViewById(R.id.card_profile);
        cardAIInsights = findViewById(R.id.card_ai_insights);
        cardAlerts = findViewById(R.id.card_alerts);
    }

    private void initHelpers() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupListeners() {
        cardPatients.setOnClickListener(this);
        cardPregnancy.setOnClickListener(this);
        cardChildGrowth.setOnClickListener(this);
        cardVaccination.setOnClickListener(this);
        cardHistory.setOnClickListener(this);
        cardSync.setOnClickListener(this);
        cardProfile.setOnClickListener(this);
        cardAIInsights.setOnClickListener(this);
        cardAlerts.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        
        btnSyncNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSync();
            }
        });
    }

    private void loadUserData() {
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Welcome, " + userName);
        } else {
            tvWelcome.setText(getString(R.string.welcome_asha));
        }

        String location = sessionManager.getUserLocation();
        if (location != null && !location.isEmpty()) {
            tvLocation.setText(location);
        } else {
            tvLocation.setText("Location not set");
        }
    }

    private void updateSyncStatus() {
        int pendingCount = dbHelper.getTotalPendingRecords();
        
        if (pendingCount > 0) {
            syncStatusBar.setVisibility(View.VISIBLE);
            tvPendingCount.setText(pendingCount + " records pending sync");
            tvSyncStatus.setText(NetworkUtils.isNetworkAvailable(this) ? "Online" : "Offline");
            tvSyncStatus.setTextColor(getResources().getColor(
                    NetworkUtils.isNetworkAvailable(this) ? R.color.status_synced : R.color.status_pending));
        } else {
            syncStatusBar.setVisibility(View.GONE);
        }

        // Update card counts
        updateDashboardCounts();
    }

    private void updateDashboardCounts() {
        // Update patient count in card
        int patientCount = dbHelper.getPatientCount();
        TextView tvPatientCount = cardPatients.findViewById(R.id.tv_card_count);
        if (tvPatientCount != null) {
            tvPatientCount.setText(String.valueOf(patientCount));
        }
    }

    private void performSync() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.sync_in_progress), Toast.LENGTH_SHORT).show();
        
        // Start sync service
        Intent syncIntent = new Intent(this, SyncService.class);
        startService(syncIntent);
        
        // Navigate to sync status
        startActivity(new Intent(this, SyncStatusActivity.class));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.card_patients) {
            startActivity(new Intent(this, PatientListActivity.class));
        } else if (id == R.id.card_pregnancy) {
            startActivity(new Intent(this, PregnancyListActivity.class));
        } else if (id == R.id.card_child_growth) {
            startActivity(new Intent(this, ChildGrowthListActivity.class));
        } else if (id == R.id.card_vaccination) {
            startActivity(new Intent(this, VaccinationListActivity.class));
        } else if (id == R.id.card_history) {
            startActivity(new Intent(this, VisitHistoryActivity.class));
        } else if (id == R.id.card_sync) {
            startActivity(new Intent(this, SyncStatusActivity.class));
        } else if (id == R.id.card_profile || id == R.id.iv_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.card_ai_insights) {
            startActivity(new Intent(this, AIInsightsActivity.class));
        } else if (id == R.id.card_alerts) {
            startActivity(new Intent(this, PatientAlertsActivity.class));
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
