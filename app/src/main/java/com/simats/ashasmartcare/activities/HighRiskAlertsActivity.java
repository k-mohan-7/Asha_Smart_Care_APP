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

import com.android.volley.Request;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.HighRiskAlertAdapter;
import com.simats.ashasmartcare.models.HighRiskAlert;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HighRiskAlertsActivity extends AppCompatActivity {

    private ImageView ivBack, ivRefresh;
    private RecyclerView rvAlerts;
    private View navDashboard, navPatients, navAlerts, navSettings;

    private HighRiskAlertAdapter alertAdapter;
    private List<HighRiskAlert> alertList;
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

        setContentView(R.layout.activity_high_risk_alerts);

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupRecyclerView();
        loadHighRiskPatients();
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

    private void loadHighRiskPatients() {
        alertList.clear();
        
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load from backend API - get high-risk patients from all ASHA workers
        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php?action=get_high_risk_patients";

        apiHelper.makeRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray patients = response.optJSONArray("data");
                                if (patients != null) {
                                    alertList.clear();
                                    for (int i = 0; i < patients.length(); i++) {
                                        JSONObject patientObj = patients.getJSONObject(i);
                                        
                                        int id = patientObj.optInt("id", 0);
                                        String name = patientObj.optString("name", "");
                                        String address = patientObj.optString("address", "");
                                        String severity = patientObj.optString("severity", "low");
                                        String ashaWorker = patientObj.optString("asha_worker", "Unknown");
                                        int alertCount = patientObj.optInt("alert_count", 0);
                                        
                                        // Create alert with severity as alert type
                                        String alertType = severity.toUpperCase() + " Risk (" + alertCount + " alerts)";
                                        String location = address + " - ASHA: " + ashaWorker;
                                        
                                        HighRiskAlert alert = new HighRiskAlert(id, name, location, alertType);
                                        alertList.add(alert);
                                    }
                                    
                                    runOnUiThread(() -> {
                                        alertAdapter.notifyDataSetChanged();
                                        if (alertList.isEmpty()) {
                                            Toast.makeText(HighRiskAlertsActivity.this,
                                                    "No high-risk patients", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(HighRiskAlertsActivity.this,
                                        "Error loading alerts", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(HighRiskAlertsActivity.this,
                                    "Failed to load alerts: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing alerts...", Toast.LENGTH_SHORT).show();
            loadHighRiskPatients();
        });

        // Bottom navigation
        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        navPatients.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminPatientsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        navAlerts.setOnClickListener(v -> {
            // Already on alerts screen
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminSettingsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }
}
