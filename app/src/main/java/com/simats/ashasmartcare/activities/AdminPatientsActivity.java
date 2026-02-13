package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.GroupedPatientAdapter;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminPatientsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etSearch;
    private TextView tvTotalCount;
    private RecyclerView rvPatients;
    private View navDashboard, navPatients, navAlerts, navSettings;

    private GroupedPatientAdapter patientAdapter;
    private List<Object> groupedItems; // Mix of headers and patients
    
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

        setContentView(R.layout.activity_admin_patients);

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupRecyclerView();
        loadPatients();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etSearch = findViewById(R.id.etSearch);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        rvPatients = findViewById(R.id.rvPatients);
        navDashboard = findViewById(R.id.navDashboard);
        navPatients = findViewById(R.id.navPatients);
        navAlerts = findViewById(R.id.navAlerts);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupRecyclerView() {
        groupedItems = new ArrayList<>();
        patientAdapter = new GroupedPatientAdapter(this);
        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        rvPatients.setAdapter(patientAdapter);
    }

    private void loadPatients() {
        groupedItems.clear();
        
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load from backend API - get all patients grouped by workers
        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php?action=get_patients";

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
                                JSONArray workerGroups = response.optJSONArray("data");
                                if (workerGroups != null) {
                                    groupedItems.clear();
                                    int totalPatients = 0;
                                    
                                    // Loop through each worker group
                                    for (int i = 0; i < workerGroups.length(); i++) {
                                        JSONObject workerGroup = workerGroups.getJSONObject(i);
                                        String workerName = workerGroup.optString("worker_name", "Unknown");
                                        int patientCount = workerGroup.optInt("patient_count", 0);
                                        JSONArray patients = workerGroup.optJSONArray("patients");
                                        
                                        // Add worker header
                                        groupedItems.add(new GroupedPatientAdapter.WorkerHeader(workerName, patientCount));
                                        
                                        // Loop through patients for this worker
                                        if (patients != null) {
                                            for (int j = 0; j < patients.length(); j++) {
                                                JSONObject patientObj = patients.getJSONObject(j);
                                                
                                                int id = patientObj.optInt("id", 0);
                                                String name = patientObj.optString("name", "");
                                                int age = patientObj.optInt("age", 0);
                                                String category = patientObj.optString("category", "");
                                                String address = patientObj.optString("address", "");
                                                
                                                // Add patient item
                                                groupedItems.add(new GroupedPatientAdapter.PatientItem(
                                                    id, name, age, category, address
                                                ));
                                                totalPatients++;
                                            }
                                        }
                                    }
                                    
                                    final int finalTotal = totalPatients;
                                    runOnUiThread(() -> {
                                        tvTotalCount.setText("Total: " + finalTotal);
                                        patientAdapter.setData(groupedItems);
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(AdminPatientsActivity.this,
                                        "Error loading patients", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminPatientsActivity.this,
                                    "Failed to load patients: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Implement search filtering for grouped patients
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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
            // Already on patients screen
        });

        navAlerts.setOnClickListener(v -> {
            startActivity(new Intent(this, HighRiskAlertsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminSettingsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }
}
