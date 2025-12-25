package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.adapters.PatientAdapter;

import java.util.ArrayList;
import java.util.List;

public class PatientAlertsActivity extends AppCompatActivity implements PatientAdapter.OnPatientClickListener {

    private ImageView ivBack;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvAlertCount;

    private PatientAdapter adapter;
    private List<Patient> highRiskPatients;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_alerts);

        initViews();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        recyclerView = findViewById(R.id.recyclerView);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        tvAlertCount = findViewById(R.id.tvAlertCount);

        dbHelper = DatabaseHelper.getInstance(this);
        highRiskPatients = new ArrayList<>();

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new PatientAdapter(this, highRiskPatients, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        highRiskPatients.clear();

        List<Patient> allPatients = dbHelper.getAllPatients();
        for (Patient patient : allPatients) {
            if (patient.isHighRisk()) {
                highRiskPatients.add(patient);
            }
        }

        tvAlertCount.setText(highRiskPatients.size() + " high-risk patients");
        adapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);

        if (highRiskPatients.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPatientClick(Patient patient) {
        Intent intent = new Intent(this, PatientProfileActivity.class);
        intent.putExtra("patient_id", patient.getId());
        startActivity(intent);
    }

    @Override
    public void onCallClick(Patient patient) {
        // Handle call
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
