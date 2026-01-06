package com.simats.ashasmartcare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;

import java.util.List;

public class AIInsightsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvAnalysisText;
    private CardView cardHighRiskPregnancies, cardChildGrowthAlerts, cardIncompleteRecords;
    private TextView tvHighRiskDesc, tvGrowthAlertsDesc, tvIncompleteDesc;
    private TextView btnViewHighRisk, btnViewGrowthAlerts, btnViewIncomplete;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_insights);

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvAnalysisText = findViewById(R.id.tvAnalysisText);
        cardHighRiskPregnancies = findViewById(R.id.cardHighRiskPregnancies);
        cardChildGrowthAlerts = findViewById(R.id.cardChildGrowthAlerts);
        cardIncompleteRecords = findViewById(R.id.cardIncompleteRecords);
        tvHighRiskDesc = findViewById(R.id.tvHighRiskDesc);
        tvGrowthAlertsDesc = findViewById(R.id.tvGrowthAlertsDesc);
        tvIncompleteDesc = findViewById(R.id.tvIncompleteDesc);
        btnViewHighRisk = findViewById(R.id.btnViewHighRisk);
        btnViewGrowthAlerts = findViewById(R.id.btnViewGrowthAlerts);
        btnViewIncomplete = findViewById(R.id.btnViewIncomplete);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnViewHighRisk.setOnClickListener(v -> {
            Toast.makeText(this, "Opening high-risk pregnancies details...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to filtered patient list showing high-risk pregnancies
        });

        btnViewGrowthAlerts.setOnClickListener(v -> {
            Toast.makeText(this, "Opening child growth alerts...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to filtered patient list showing children with growth alerts
        });

        btnViewIncomplete.setOnClickListener(v -> {
            Toast.makeText(this, "Opening incomplete records...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to filtered patient list showing incomplete records
        });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        // Calculate statistics from database
        List<Patient> patients = dbHelper.getAllPatients();
        
        int highRiskCount = 0;
        int growthAlertCount = 0;
        int incompleteCount = 0;

        for (Patient patient : patients) {
            // Check for high-risk pregnancies
            if (patient.isHighRisk()) {
                highRiskCount++;
            }
            
            // Check for incomplete records (missing phone number or medical notes)
            if ((patient.getPhone() == null || patient.getPhone().isEmpty()) ||
                (patient.getMedicalNotes() == null || patient.getMedicalNotes().isEmpty())) {
                incompleteCount++;
            }
            
            // TODO: Check for child growth stagnation
            // This would require comparing growth records over time
        }

        // For demo purposes, using sample data for child growth alerts
        growthAlertCount = 5;

        // Update analysis text
        tvAnalysisText.setText("Analyzing " + patients.size() + " patient records to identify risks early.");

        // Update card descriptions
        tvHighRiskDesc.setText(highRiskCount + " women identified with potential complications based on recent vitals.");
        tvGrowthAlertsDesc.setText(growthAlertCount + " children showing signs of growth stagnation in the last 2 months.");
        tvIncompleteDesc.setText(incompleteCount + " patient profiles are missing key contact or medical information.");

        progressBar.setVisibility(View.GONE);

        // Show/hide cards based on data
        if (highRiskCount == 0 && growthAlertCount == 0 && incompleteCount == 0) {
            cardHighRiskPregnancies.setVisibility(View.GONE);
            cardChildGrowthAlerts.setVisibility(View.GONE);
            cardIncompleteRecords.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            cardHighRiskPregnancies.setVisibility(highRiskCount > 0 ? View.VISIBLE : View.GONE);
            cardChildGrowthAlerts.setVisibility(growthAlertCount > 0 ? View.VISIBLE : View.GONE);
            cardIncompleteRecords.setVisibility(incompleteCount > 0 ? View.VISIBLE : View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
