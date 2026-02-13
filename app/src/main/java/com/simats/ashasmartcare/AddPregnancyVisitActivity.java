package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.PregnancyVisit;
import com.simats.ashasmartcare.services.NetworkMonitorService;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPregnancyVisitActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputLayout tilVisitDate, tilWeeks, tilWeight, tilBPSystolic, tilBPDiastolic;
    private TextInputLayout tilHemoglobin, tilFetalHeartRate, tilFundalHeight;
    private TextInputLayout tilUrineSugar, tilUrineAlbumin, tilComplaints, tilAdvice;
    private TextInputLayout tilNextVisitDate, tilRiskFactors, tilNotes;

    private TextInputEditText etVisitDate, etWeeks, etWeight, etBPSystolic, etBPDiastolic;
    private TextInputEditText etHemoglobin, etFetalHeartRate, etFundalHeight;
    private TextInputEditText etUrineSugar, etUrineAlbumin, etComplaints, etAdvice;
    private TextInputEditText etNextVisitDate, etRiskFactors, etNotes;

    private CheckBox cbHighRisk;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    private long patientId;
    private Patient patient;
    private PregnancyVisit editVisit = null;
    private boolean isEditMode = false;

    private Calendar visitDateCalendar = Calendar.getInstance();
    private Calendar nextVisitDateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pregnancy_visit);

        patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        checkEditMode();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        tilVisitDate = findViewById(R.id.tilVisitDate);
        tilWeeks = findViewById(R.id.tilWeeks);
        tilWeight = findViewById(R.id.tilWeight);
        tilBPSystolic = findViewById(R.id.tilBPSystolic);
        tilBPDiastolic = findViewById(R.id.tilBPDiastolic);
        tilHemoglobin = findViewById(R.id.tilHemoglobin);
        tilFetalHeartRate = findViewById(R.id.tilFetalHeartRate);
        tilFundalHeight = findViewById(R.id.tilFundalHeight);
        tilUrineSugar = findViewById(R.id.tilUrineSugar);
        tilUrineAlbumin = findViewById(R.id.tilUrineAlbumin);
        tilComplaints = findViewById(R.id.tilComplaints);
        tilAdvice = findViewById(R.id.tilAdvice);
        tilNextVisitDate = findViewById(R.id.tilNextVisitDate);
        tilRiskFactors = findViewById(R.id.tilRiskFactors);
        tilNotes = findViewById(R.id.tilNotes);

        etVisitDate = findViewById(R.id.etVisitDate);
        etWeeks = findViewById(R.id.etWeeks);
        etWeight = findViewById(R.id.etWeight);
        etBPSystolic = findViewById(R.id.etBPSystolic);
        etBPDiastolic = findViewById(R.id.etBPDiastolic);
        etHemoglobin = findViewById(R.id.etHemoglobin);
        etFetalHeartRate = findViewById(R.id.etFetalHeartRate);
        etFundalHeight = findViewById(R.id.etFundalHeight);
        etUrineSugar = findViewById(R.id.etUrineSugar);
        etUrineAlbumin = findViewById(R.id.etUrineAlbumin);
        etComplaints = findViewById(R.id.etComplaints);
        etAdvice = findViewById(R.id.etAdvice);
        etNextVisitDate = findViewById(R.id.etNextVisitDate);
        etRiskFactors = findViewById(R.id.etRiskFactors);
        etNotes = findViewById(R.id.etNotes);

        cbHighRisk = findViewById(R.id.cbHighRisk);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        patient = dbHelper.getPatientById(patientId);

        // Set default visit date to today
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etVisitDate.setText(sdf.format(visitDateCalendar.getTime()));
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        etVisitDate.setOnClickListener(v -> showDatePicker(true));
        etNextVisitDate.setOnClickListener(v -> showDatePicker(false));

        cbHighRisk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilRiskFactors.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                saveVisit();
            }
        });
    }

    private void showDatePicker(boolean isVisitDate) {
        Calendar calendar = isVisitDate ? visitDateCalendar : nextVisitDateCalendar;

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            if (isVisitDate) {
                etVisitDate.setText(sdf.format(calendar.getTime()));
            } else {
                etNextVisitDate.setText(sdf.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void checkEditMode() {
        long visitId = getIntent().getLongExtra("visit_id", -1);
        if (visitId != -1) {
            isEditMode = true;
            editVisit = dbHelper.getPregnancyVisitById(visitId);
            if (editVisit != null) {
                populateForm(editVisit);
                btnSave.setText("Update Visit");
            }
        }
    }

    private void populateForm(PregnancyVisit visit) {
        etVisitDate.setText(visit.getVisitDate());
        etWeeks.setText(String.valueOf(visit.getWeeksPregnant()));
        etWeight.setText(String.valueOf(visit.getWeight()));
        etBPSystolic.setText(String.valueOf(visit.getBloodPressureSystolic()));
        etBPDiastolic.setText(String.valueOf(visit.getBloodPressureDiastolic()));
        etHemoglobin.setText(String.valueOf(visit.getHemoglobin()));
        etFetalHeartRate.setText(String.valueOf(visit.getFetalHeartRate()));
        etFundalHeight.setText(String.valueOf(visit.getFundalHeight()));
        etUrineSugar.setText(visit.getUrineSugar());
        etUrineAlbumin.setText(visit.getUrineAlbumin());
        etComplaints.setText(visit.getComplaints());
        etAdvice.setText(visit.getAdvice());
        etNextVisitDate.setText(visit.getNextVisitDate());
        cbHighRisk.setChecked(visit.isHighRisk());
        etRiskFactors.setText(visit.getRiskFactors());
        etNotes.setText(visit.getNotes());

        if (visit.isHighRisk()) {
            tilRiskFactors.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Visit date
        if (etVisitDate.getText().toString().isEmpty()) {
            tilVisitDate.setError("Visit date is required");
            isValid = false;
        } else {
            tilVisitDate.setError(null);
        }

        // Weeks pregnant
        String weeksStr = etWeeks.getText().toString().trim();
        if (weeksStr.isEmpty()) {
            tilWeeks.setError("Weeks is required");
            isValid = false;
        } else {
            try {
                int weeks = Integer.parseInt(weeksStr);
                if (weeks < 1 || weeks > 45) {
                    tilWeeks.setError("Enter valid weeks (1-45)");
                    isValid = false;
                } else {
                    tilWeeks.setError(null);
                }
            } catch (NumberFormatException e) {
                tilWeeks.setError("Enter valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveVisit() {
        showLoading(true);

        PregnancyVisit visit = new PregnancyVisit();
        if (isEditMode && editVisit != null) {
            visit.setId(editVisit.getId());
            visit.setServerId(editVisit.getServerId());
        }

        visit.setPatientId(patientId);
        if (patient != null) {
            visit.setPatientServerId(String.valueOf(patient.getServerId()));
        }
        visit.setVisitDate(etVisitDate.getText().toString().trim());

        try {
            visit.setWeeksPregnant(Integer.parseInt(etWeeks.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setWeeksPregnant(0);
        }

        try {
            visit.setWeight(Double.parseDouble(etWeight.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setWeight(0);
        }

        try {
            visit.setBloodPressureSystolic(Integer.parseInt(etBPSystolic.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setBloodPressureSystolic(0);
        }

        try {
            visit.setBloodPressureDiastolic(Integer.parseInt(etBPDiastolic.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setBloodPressureDiastolic(0);
        }

        try {
            visit.setHemoglobin(Double.parseDouble(etHemoglobin.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setHemoglobin(0);
        }

        try {
            visit.setFetalHeartRate(Integer.parseInt(etFetalHeartRate.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setFetalHeartRate(0);
        }

        try {
            visit.setFundalHeight(Double.parseDouble(etFundalHeight.getText().toString().trim()));
        } catch (NumberFormatException e) {
            visit.setFundalHeight(0);
        }

        visit.setUrineSugar(etUrineSugar.getText().toString().trim());
        visit.setUrineAlbumin(etUrineAlbumin.getText().toString().trim());
        visit.setComplaints(etComplaints.getText().toString().trim());
        visit.setAdvice(etAdvice.getText().toString().trim());
        visit.setNextVisitDate(etNextVisitDate.getText().toString().trim());
        visit.setHighRisk(cbHighRisk.isChecked());
        visit.setRiskFactors(etRiskFactors.getText().toString().trim());
        visit.setNotes(etNotes.getText().toString().trim());

        if (NetworkMonitorService.isNetworkConnected(this)) {
            // ONLINE: Direct backend POST only (NO local database)
            visit.setSyncStatus("SYNCED");
            saveToServerOnly(visit);
        } else {
            // OFFLINE: Save to local database + sync queue
            visit.setSyncStatus("PENDING");
            saveLocally(visit);
        }

        // Auto-update Patient Risk Status if this visit is High Risk
        if (visit.isHighRisk() && patient != null) {
            boolean changed = false;
            if (!patient.isHighRisk()) {
                patient.setHighRisk(true);
                changed = true;
            }

            // Append reason if not present
            String currentReason = patient.getHighRiskReason();
            if (currentReason == null)
                currentReason = "";

            String newReason = visit.getRiskFactors();
            if (newReason != null && !newReason.isEmpty() && !currentReason.contains(newReason)) {
                if (!currentReason.isEmpty())
                    currentReason += ", ";
                currentReason += newReason;
                patient.setHighRiskReason(currentReason);
                changed = true;
            } else if (currentReason.isEmpty()) {
                patient.setHighRiskReason("Pregnancy Visit Risk Factors");
                changed = true;
            }

            if (changed) {
                dbHelper.updatePatient(patient);
            }
        }
    }

    private void saveToServerOnly(PregnancyVisit visit) {
        showLoading(true);
        ApiHelper.ApiCallback callback = new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                showLoading(false);
                try {
                    if (response.getBoolean("success")) {
                        // ONLINE MODE: Backend success, NO local storage
                        runOnUiThread(() -> {
                            String message = isEditMode ? "Visit updated successfully!" : "Visit recorded successfully!";
                            Toast.makeText(AddPregnancyVisitActivity.this, "✓ " + message, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        String msg = response.optString("message", "Failed to save visit");
                        runOnUiThread(() -> {
                            Toast.makeText(AddPregnancyVisitActivity.this, "Backend error: " + msg, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddPregnancyVisitActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                runOnUiThread(() -> {
                    Toast.makeText(AddPregnancyVisitActivity.this, "Network error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        };

        if (isEditMode) {
            apiHelper.updatePregnancyVisit(visit, callback);
        } else {
            apiHelper.addPregnancyVisit(visit, callback);
        }
    }

    private void saveLocally(PregnancyVisit visit) {
        long result;
        if (isEditMode) {
            result = dbHelper.updatePregnancyVisit(visit);
        } else {
            result = dbHelper.insertPregnancyVisit(visit);
        }

        showLoading(false);

        if (result != -1) {
            String message = isEditMode ? "Visit updated" : "Visit recorded";
            if ("PENDING".equals(visit.getSyncStatus())) {
                message += " (will sync when online)";
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save visit", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        scrollView.setAlpha(show ? 0.5f : 1.0f);
    }
}
