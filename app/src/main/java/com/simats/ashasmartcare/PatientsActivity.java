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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.adapters.PatientsAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.utils.SessionManager;

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
    private PatientsAdapter adapter;
    private List<Patient> allPatients;
    private List<Patient> filteredPatients;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadPatients();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etSearch = findViewById(R.id.et_search);
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
        allPatients = new ArrayList<>();
        filteredPatients = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new PatientsAdapter(this, filteredPatients, patient -> {
            // Open patient profile
            Intent intent = new Intent(PatientsActivity.this, PatientDetailActivity.class);
            intent.putExtra("patient_id", patient.getLocalId());
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

        // Filter chips
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            filterPatients();
        });

        chipPregnant.setOnClickListener(v -> {
            currentFilter = "pregnant";
            filterPatients();
        });

        chipChildren.setOnClickListener(v -> {
            currentFilter = "children";
            filterPatients();
        });

        chipHighRisk.setOnClickListener(v -> {
            currentFilter = "high_risk";
            filterPatients();
        });

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Load from database
        new Thread(() -> {
            allPatients = dbHelper.getAllPatients();

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                filterPatients();
            });
        }).start();
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
