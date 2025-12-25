package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.PregnancyVisit;

public class PregnancyDetailActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvTitle, tvSubtitle;
    private TextView tvVisitDate, tvGestationalWeeks, tvWeight, tvBloodPressure;
    private TextView tvHemoglobin, tvFetalHeartRate, tvNotes, tvHighRiskReason;
    private CardView cardRiskStatus;
    private Button btnEdit;

    private DatabaseHelper dbHelper;
    private PregnancyVisit visit;
    private long visitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy_detail);

        visitId = getIntent().getLongExtra("visit_id", -1);
        if (visitId == -1) {
            Toast.makeText(this, "Invalid visit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadVisit();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvVisitDate = findViewById(R.id.tvVisitDate);
        tvGestationalWeeks = findViewById(R.id.tvGestationalWeeks);
        tvWeight = findViewById(R.id.tvWeight);
        tvBloodPressure = findViewById(R.id.tvBloodPressure);
        tvHemoglobin = findViewById(R.id.tvHemoglobin);
        tvFetalHeartRate = findViewById(R.id.tvFetalHeartRate);
        tvNotes = findViewById(R.id.tvNotes);
        tvHighRiskReason = findViewById(R.id.tvHighRiskReason);
        cardRiskStatus = findViewById(R.id.cardRiskStatus);
        btnEdit = findViewById(R.id.btnEdit);

        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPregnancyVisitActivity.class);
            intent.putExtra("visit_id", visitId);
            intent.putExtra("patient_id", visit.getPatientId());
            intent.putExtra("mode", "edit");
            startActivity(intent);
        });
    }

    private void loadVisit() {
        visit = dbHelper.getPregnancyVisitById(visitId);
        
        if (visit == null) {
            Toast.makeText(this, "Visit not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayVisitDetails();
    }

    private void displayVisitDetails() {
        tvVisitDate.setText(visit.getVisitDate() != null ? visit.getVisitDate() : "-");
        tvGestationalWeeks.setText(visit.getGestationalWeeks() + " weeks");
        tvWeight.setText(visit.getWeight() + " kg");
        tvBloodPressure.setText(visit.getBloodPressure() != null ? visit.getBloodPressure() + " mmHg" : "-");
        tvHemoglobin.setText(visit.getHemoglobin() > 0 ? visit.getHemoglobin() + " g/dL" : "-");
        tvFetalHeartRate.setText(visit.getFetalHeartRate() > 0 ? visit.getFetalHeartRate() + " bpm" : "-");
        
        String notes = visit.getNotes();
        tvNotes.setText(notes != null && !notes.isEmpty() ? notes : "No notes");

        // Show high risk status if applicable
        if (visit.isHighRisk()) {
            cardRiskStatus.setVisibility(View.VISIBLE);
            String reason = visit.getHighRiskReason();
            tvHighRiskReason.setText(reason != null && !reason.isEmpty() ? reason : "Reason not specified");
        } else {
            cardRiskStatus.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVisit();
    }
}
