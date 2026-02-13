package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.volley.Request;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONObject;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardAddWorker;
    private CardView cardWorkers;
    private CardView cardPatients;
    private CardView cardHighRisk;
    private CardView cardPendingApproval;

    // Count TextViews
    private TextView tvWorkersCount;
    private TextView tvPatientsCount;
    private TextView tvHighRiskCount;
    private TextView tvPendingApprovalCount;

    // Nav items
    private View navPatients;
    private View navAlerts;
    private View navSettings;
    private ImageView ivProfile;

    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_admin_dashboard);

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupListeners();
        loadDashboardStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void initViews() {
        cardAddWorker = findViewById(R.id.cardAddWorker);
        cardWorkers = findViewById(R.id.cardWorkers);
        cardPatients = findViewById(R.id.cardPatients);
        cardHighRisk = findViewById(R.id.cardHighRisk);
        cardPendingApproval = findViewById(R.id.cardPendingApproval);

        tvWorkersCount = findViewById(R.id.tvWorkersCount);
        tvPatientsCount = findViewById(R.id.tvPatientsCount);
        tvHighRiskCount = findViewById(R.id.tvHighRiskCount);
        tvPendingApprovalCount = findViewById(R.id.tvPendingApprovalCount);

        navPatients = findViewById(R.id.navPatients);
        navAlerts = findViewById(R.id.navAlerts);
        navSettings = findViewById(R.id.navSettings);
        ivProfile = findViewById(R.id.ivProfile);
    }

    private void setupListeners() {
        cardAddWorker.setOnClickListener(v -> {
            startActivity(new Intent(this, AddAshaWorkerActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        cardWorkers.setOnClickListener(v -> {
            // Navigate to ASHA Workers Activity
            Intent intent = new Intent(this, AshaWorkersActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        cardPatients.setOnClickListener(v -> {
            // Navigate to Admin Patients Activity
            Intent intent = new Intent(this, AdminPatientsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        cardHighRisk.setOnClickListener(v -> {
            Intent intent = new Intent(this, HighRiskAlertsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        cardPendingApproval.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPendingApprovalsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        navPatients.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPatientsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        navAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(this, HighRiskAlertsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminSettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    /**
     * Load dashboard statistics from backend API
     */
    private void loadDashboardStats() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            // Show default counts if offline
            tvWorkersCount.setText("--");
            tvPatientsCount.setText("--");
            tvHighRiskCount.setText("--");
            tvPendingApprovalCount.setText("--");
            return;
        }

        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php?action=dashboard_stats";

        apiHelper.makeRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (success) {
                                // Backend returns counts directly in response, not in a "data" object
                                final int workersCount = response.optInt("workers_count", 0);
                                final int patientsCount = response.optInt("patients_count", 0);
                                final int highRiskCount = response.optInt("high_risk_count", 0);
                                final int pendingApprovalCount = response.optInt("pending_approval_count", 0);

                                runOnUiThread(() -> {
                                    tvWorkersCount.setText(String.valueOf(workersCount));
                                    tvPatientsCount.setText(String.valueOf(patientsCount));
                                    tvHighRiskCount.setText(String.valueOf(highRiskCount));
                                    tvPendingApprovalCount.setText(String.valueOf(pendingApprovalCount));
                                });
                            } else {
                                runOnUiThread(() -> {
                                    tvWorkersCount.setText("0");
                                    tvPatientsCount.setText("0");
                                    tvHighRiskCount.setText("0");
                                    tvPendingApprovalCount.setText("0");
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                tvWorkersCount.setText("0");
                                tvPatientsCount.setText("0");
                                tvHighRiskCount.setText("0");
                                tvPendingApprovalCount.setText("0");
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            tvWorkersCount.setText("0");
                            tvPatientsCount.setText("0");
                            tvHighRiskCount.setText("0");
                            tvPendingApprovalCount.setText("0");
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Failed to load stats", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
}
