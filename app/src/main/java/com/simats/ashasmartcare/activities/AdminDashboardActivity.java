package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;

import org.json.JSONObject;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardAddWorker;
    private CardView cardWorkers;
    private CardView cardPatients;
    private CardView cardHighRisk;
    private CardView cardPendingSync;
    
    // Stats TextViews
    private TextView tvTotalWorkers;
    private TextView tvTotalPatients;
    private TextView tvHighRiskCount;
    private TextView tvPendingRequests;

    // Nav items
    private View navPatients;
    private View navAlerts;
    private View navSettings;
    
    private ApiHelper apiHelper;

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
        loadDashboardData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        cardAddWorker = findViewById(R.id.cardAddWorker);
        cardWorkers = findViewById(R.id.cardWorkers);
        cardPatients = findViewById(R.id.cardPatients);
        cardHighRisk = findViewById(R.id.cardHighRisk);
        cardPendingSync = findViewById(R.id.cardPendingSync);
        
        tvTotalWorkers = findViewById(R.id.tvTotalWorkers);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        tvHighRiskCount = findViewById(R.id.tvHighRiskCount);
        tvPendingRequests = findViewById(R.id.tvPendingRequests);

        navPatients = findViewById(R.id.navPatients);
        navAlerts = findViewById(R.id.navAlerts);
        navSettings = findViewById(R.id.navSettings);
        
        apiHelper = ApiHelper.getInstance(this);
    }
    
    private void loadDashboardData() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Some data may not be available.", Toast.LENGTH_SHORT).show();
            setDefaultValues();
            return;
        }
        
        // Fetch dashboard statistics from API
        apiHelper.getAdminDashboardStats(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONObject data = response.getJSONObject("data");
                        
                        // Update UI with real data - using correct field names from API
                        int totalWorkers = data.optInt("total_employees", 0);
                        int totalPatients = data.optInt("total_patients", 0);
                        int highRiskPatients = data.optInt("high_risk_alerts", 0);
                        int pendingApprovals = data.optInt("pending_requests", 0);
                        
                        if (tvTotalWorkers != null) tvTotalWorkers.setText(String.valueOf(totalWorkers));
                        if (tvTotalPatients != null) tvTotalPatients.setText(String.valueOf(totalPatients));
                        if (tvHighRiskCount != null) tvHighRiskCount.setText(String.valueOf(highRiskPatients));
                        if (tvPendingRequests != null) tvPendingRequests.setText(String.valueOf(pendingApprovals));
                    } else {
                        setDefaultValues();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setDefaultValues();
                }
            }

            @Override
            public void onError(String errorMessage) {
                setDefaultValues();
            }
        });
    }
    
    private void setDefaultValues() {
        // Set zeros if API fails
        if (tvTotalWorkers != null) tvTotalWorkers.setText("0");
        if (tvTotalPatients != null) tvTotalPatients.setText("0");
        if (tvHighRiskCount != null) tvHighRiskCount.setText("0");
        if (tvPendingRequests != null) tvPendingRequests.setText("0");
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
