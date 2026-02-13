package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simats.ashasmartcare.activities.SyncStatusActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.adapters.PatientsAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.services.NetworkMonitorService;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etSearch;
    private Chip chipAll, chipPregnant, chipChildren, chipHighRisk;
    private RecyclerView rvPatients;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;
    private PatientsAdapter adapter;
    private List<Patient> allPatients;
    private List<Patient> filteredPatients;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        initViews();

        // Handle filter intent extra
        String filterExtra = getIntent().getStringExtra("filter");
        if (filterExtra != null && !filterExtra.isEmpty()) {
            currentFilter = filterExtra;
            // Determine which chip to check based on filter
            if ("pregnant".equals(currentFilter))
                chipPregnant.setChecked(true);
            else if ("children".equals(currentFilter))
                chipChildren.setChecked(true);
            else if ("high_risk".equals(currentFilter))
                chipHighRisk.setChecked(true);
            else
                chipAll.setChecked(true);
        }

        setupRecyclerView();
        setupListeners();
        loadPatients();
    }

    private com.google.android.material.chip.ChipGroup chipGroupFilters;

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etSearch = findViewById(R.id.et_search);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        chipAll = findViewById(R.id.chip_all);
        chipPregnant = findViewById(R.id.chip_pregnant);
        chipChildren = findViewById(R.id.chip_children);
        chipHighRisk = findViewById(R.id.chip_high_risk);
        rvPatients = findViewById(R.id.rv_patients);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        fabAdd = findViewById(R.id.fab_add);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        allPatients = new ArrayList<>();
        filteredPatients = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new PatientsAdapter(this, filteredPatients, patient -> {
            // Open patient profile (unified view)
            Intent intent = new Intent(PatientsActivity.this, PatientProfileActivity.class);
            intent.putExtra("patient_id", patient.getServerId());
            startActivity(intent);
        });
        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        rvPatients.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPatientActivity.class));
        });

        swipeRefresh.setOnRefreshListener(() -> {
            loadPatients();
            swipeRefresh.setRefreshing(false);
        });

        // Filter chips using ChipGroup listener
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentFilter = "all";
            } else if (checkedId == R.id.chip_pregnant) {
                currentFilter = "pregnant";
            } else if (checkedId == R.id.chip_children) {
                currentFilter = "children";
            } else if (checkedId == R.id.chip_high_risk) {
                currentFilter = "high_risk";
            }
            filterPatients();
        });

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Check internet connection
        if (NetworkMonitorService.isNetworkConnected(this)) {
            // ONLINE: Fetch from backend API only (NO local DB)
            fetchPatientsFromBackend();
        } else {
            // OFFLINE: Show no internet message
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "⚠️ No internet connection. Cannot load patients.", Toast.LENGTH_LONG).show();
            allPatients.clear();
            filteredPatients.clear();
            adapter.notifyDataSetChanged();
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void fetchPatientsFromBackend() {
        String ashaId = String.valueOf(sessionManager.getUserId());
        String apiBaseUrl = sessionManager.getApiBaseUrl();

        // Use GET request instead of POST (POST is broken on backend)
        String endpoint = "patients.php?asha_id=" + ashaId;

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Backend returns either "success":true or "status":"success"
                    boolean isSuccess = (response.optBoolean("success", false) ||
                            "success".equals(response.optString("status", "")));

                    if (isSuccess) {
                        // Backend returns either "data" or "patients" array
                        JSONArray patientsArray = response.has("patients") ? response.getJSONArray("patients")
                                : response.getJSONArray("data");
                        allPatients.clear();

                        for (int i = 0; i < patientsArray.length(); i++) {
                            JSONObject patientObj = patientsArray.getJSONObject(i);
                            Patient patient = new Patient();
                            patient.setServerId(patientObj.getInt("id"));
                            patient.setName(patientObj.getString("name"));
                            patient.setAge(patientObj.getInt("age"));
                            patient.setGender(patientObj.getString("gender"));
                            patient.setCategory(patientObj.optString("category", "General"));
                            patient.setAddress(patientObj.optString("address", ""));
                            patient.setPhone(patientObj.optString("phone", ""));
                            patient.setHighRisk(patientObj.optInt("is_high_risk", 0) == 1);
                            patient.setHighRiskReason(patientObj.optString("high_risk_reason", ""));
                            patient.setSyncStatus("SYNCED"); // Loaded from API
                            allPatients.add(patient);
                            // ONLINE MODE: NO local database storage
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            filterPatients();
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PatientsActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
                            allPatients.clear();
                            filterPatients();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PatientsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        allPatients.clear();
                        filterPatients();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    // Show detailed error message with retry option
                    String detailedMessage = error;
                    if (error.contains("timeout") || error.contains("not responding")) {
                        detailedMessage += "\\n\\nPossible causes:\\n• Backend server not running\\n• Wrong IP address (current: "
                                +
                                sessionManager.getApiBaseUrl() + ")\\n• Network delay or firewall blocking";
                    } else if (error.contains("Unable to reach server")) {
                        detailedMessage += "\\n\\nMake sure your backend PHP server is running at:\\n" +
                                sessionManager.getApiBaseUrl();
                    }

                    Toast.makeText(PatientsActivity.this, detailedMessage, Toast.LENGTH_LONG).show();
                    allPatients.clear();
                    filterPatients();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void filterPatients() {
        String searchText = etSearch.getText().toString().toLowerCase().trim();
        filteredPatients.clear();

        for (Patient patient : allPatients) {
            // Apply category filter
            boolean matchesCategory = false;
            switch (currentFilter) {
                case "all":
                    matchesCategory = true;
                    break;
                case "pregnant":
                    matchesCategory = "Pregnant Woman".equalsIgnoreCase(patient.getCategory()) ||
                            "Pregnant".equalsIgnoreCase(patient.getCategory());
                    break;
                case "children":
                    matchesCategory = "Child (0-5 years)".equalsIgnoreCase(patient.getCategory()) ||
                            "Child".equalsIgnoreCase(patient.getCategory());
                    break;
                case "high_risk":
                    // Check if patient has high risk indicators
                    matchesCategory = isHighRisk(patient);
                    break;
            }

            // Apply search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    patient.getName().toLowerCase().contains(searchText) ||
                    (patient.getPhone() != null && patient.getPhone().contains(searchText));

            if (matchesCategory && matchesSearch) {
                filteredPatients.add(patient);
            }
        }

        // Update UI
        adapter.notifyDataSetChanged();
        layoutEmpty.setVisibility(filteredPatients.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean isHighRisk(Patient patient) {
        // Check if patient is pregnant category (basic high risk indicator)
        if ("Pregnant Woman".equalsIgnoreCase(patient.getCategory()) ||
                "Pregnant".equalsIgnoreCase(patient.getCategory())) {
            return true;
        }
        // Additional high risk checks can be added here
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }
}
