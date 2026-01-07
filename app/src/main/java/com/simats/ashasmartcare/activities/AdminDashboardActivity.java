package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.utils.AddChildVisitActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardAddWorker;
    private CardView cardWorkers;
    private CardView cardPatients;
    private CardView cardHighRisk;
    private CardView cardPendingSync;

    // Nav items
    private View navPatients;
    private View navAlerts;
    private View navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupListeners();
    }

    private void initViews() {
        cardAddWorker = findViewById(R.id.cardAddWorker);
        cardWorkers = findViewById(R.id.cardWorkers);
        cardPatients = findViewById(R.id.cardPatients);
        cardHighRisk = findViewById(R.id.cardHighRisk);
        cardPendingSync = findViewById(R.id.cardPendingSync);

        navPatients = findViewById(R.id.navPatients);
        navAlerts = findViewById(R.id.navAlerts);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupListeners() {
        cardAddWorker.setOnClickListener(v -> {
            startActivity(new Intent(this, AddAshaWorkerActivity.class));
        });

        cardWorkers.setOnClickListener(v -> {
            // Navigate to ASHA Workers Activity
            Intent intent = new Intent(this, AshaWorkersActivity.class);
            startActivity(intent);
        });

        cardPatients.setOnClickListener(v -> {
            // Navigate to Admin Patients Activity
            Intent intent = new Intent(this, AdminPatientsActivity.class);
            startActivity(intent);
        });

        cardHighRisk.setOnClickListener(v -> {
            Intent intent = new Intent(this, HighRiskAlertsActivity.class);
            startActivity(intent);
        });

        cardPendingSync.setOnClickListener(v -> {
            Intent intent = new Intent(this, SyncStatusActivity.class);
            startActivity(intent);
        });

        navPatients.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPatientsActivity.class);
            startActivity(intent);
        });

        navAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(this, HighRiskAlertsActivity.class);
            startActivity(intent);
        });

        navSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Nav: Settings", Toast.LENGTH_SHORT).show();
        });
    }
}
