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

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.AdminPatientAdapter;
import com.simats.ashasmartcare.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class AdminPatientsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etSearch;
    private TextView tvTotalCount;
    private RecyclerView rvPatients;
    private View navDashboard, navPatients, navAlerts, navSettings;

    private AdminPatientAdapter patientAdapter;
    private List<Patient> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_admin_patients);

        initViews();
        setupRecyclerView();
        loadSampleData();
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
        patientList = new ArrayList<>();
        patientAdapter = new AdminPatientAdapter(this, patientList);
        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        rvPatients.setAdapter(patientAdapter);
    }

    private void loadSampleData() {
        // Sample data matching the design
        Patient p1 = new Patient();
        p1.setName("Lakshmi Devi");
        p1.setHighRisk(true);
        p1.setCategory("Pregnant");
        p1.setVillage("Rampur");
        p1.setAge(28);
        patientList.add(p1);

        Patient p2 = new Patient();
        p2.setName("Rohan Kumar");
        p2.setHighRisk(true);
        p2.setCategory("Child");
        p2.setVillage("Sitapur");
        p2.setAge(2);
        patientList.add(p2);

        Patient p3 = new Patient();
        p3.setName("Rajesh kumar");
        p3.setHighRisk(false);
        p3.setCategory("General");
        p3.setVillage("Lakhimpur");
        p3.setAge(45);
        patientList.add(p3);

        Patient p4 = new Patient();
        p4.setName("Meena Devi");
        p4.setHighRisk(false);
        p4.setCategory("General");
        p4.setVillage("Rampur");
        p4.setAge(35);
        patientList.add(p4);

        Patient p5 = new Patient();
        p5.setName("Baby of Priya");
        p5.setHighRisk(true);
        p5.setCategory("Infant");
        p5.setVillage("Rampur");
        p5.setAge(0); // 2 weeks
        patientList.add(p5);

        tvTotalCount.setText("Total: " + patientList.size());
        patientAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                patientAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Bottom navigation
        navDashboard.setOnClickListener(v -> {
            finish(); // Go back to dashboard
        });

        navPatients.setOnClickListener(v -> {
            // Already on patients screen
        });

        navAlerts.setOnClickListener(v -> {
            Toast.makeText(this, "Alerts clicked", Toast.LENGTH_SHORT).show();
        });

        navSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
