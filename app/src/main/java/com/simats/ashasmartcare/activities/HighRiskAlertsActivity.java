package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.HighRiskAlertAdapter;
import com.simats.ashasmartcare.models.HighRiskAlert;

import java.util.ArrayList;
import java.util.List;

public class HighRiskAlertsActivity extends AppCompatActivity {

    private ImageView ivBack, ivRefresh;
    private RecyclerView rvAlerts;
    private View navDashboard, navPatients, navAlerts, navSettings;

    private HighRiskAlertAdapter alertAdapter;
    private List<HighRiskAlert> alertList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_high_risk_alerts);

        initViews();
        setupRecyclerView();
        loadSampleData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivRefresh = findViewById(R.id.ivRefresh);
        rvAlerts = findViewById(R.id.rvAlerts);
        navDashboard = findViewById(R.id.navDashboard);
        navPatients = findViewById(R.id.navPatients);
        navAlerts = findViewById(R.id.navAlerts);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupRecyclerView() {
        alertList = new ArrayList<>();
        alertAdapter = new HighRiskAlertAdapter(this, alertList);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvAlerts.setAdapter(alertAdapter);
    }

    private void loadSampleData() {
        // Sample data matching the design
        alertList.add(new HighRiskAlert("Priya Sharma", "Rampur Village", "High BP"));
        alertList.add(new HighRiskAlert("Anjali Devi", "Sujanpur Village", "Malnutrition"));
        alertList.add(new HighRiskAlert("Rajesh Kumar", "Madhavpur", "High BP"));

        // Reviewed alert
        HighRiskAlert reviewed = new HighRiskAlert("Sunita Singh", "Devgarh Village", "Malnutrition");
        reviewed.setReviewed(true);
        alertList.add(reviewed);

        alertAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing alerts...", Toast.LENGTH_SHORT).show();
            // TODO: Implement actual refresh logic
        });

        // Bottom navigation
        navDashboard.setOnClickListener(v -> {
            finish(); // Go back to dashboard
        });

        navPatients.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPatientsActivity.class);
            startActivity(intent);
        });

        navAlerts.setOnClickListener(v -> {
            // Already on alerts screen
        });

        navSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
