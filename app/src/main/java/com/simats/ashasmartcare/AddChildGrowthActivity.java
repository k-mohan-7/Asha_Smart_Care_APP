package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.ChildGrowth;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.utils.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddChildGrowthActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputLayout tilDate, tilAgeMonths, tilWeight, tilHeight;
    private TextInputLayout tilHeadCircumference, tilMUAC, tilNutritionalStatus, tilMilestones, tilNotes;

    private TextInputEditText etDate, etAgeMonths, etWeight, etHeight;
    private TextInputEditText etHeadCircumference, etMUAC, etMilestones, etNotes;
    private AutoCompleteTextView spinnerNutritionalStatus;

    private Button btnSave;

    private DatabaseHelper dbHelper;
    private long patientId;
    private Patient patient;
    private ChildGrowth editGrowth = null;
    private boolean isEditMode = false;

    private Calendar dateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_growth);

        patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupSpinners();
        setupListeners();
        checkEditMode();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        tilDate = findViewById(R.id.tilDate);
        tilAgeMonths = findViewById(R.id.tilAgeMonths);
        tilWeight = findViewById(R.id.tilWeight);
        tilHeight = findViewById(R.id.tilHeight);
        tilHeadCircumference = findViewById(R.id.tilHeadCircumference);
        tilMUAC = findViewById(R.id.tilMUAC);
        tilNutritionalStatus = findViewById(R.id.tilNutritionalStatus);
        tilMilestones = findViewById(R.id.tilMilestones);
        tilNotes = findViewById(R.id.tilNotes);

        etDate = findViewById(R.id.etDate);
        etAgeMonths = findViewById(R.id.etAgeMonths);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etHeadCircumference = findViewById(R.id.etHeadCircumference);
        etMUAC = findViewById(R.id.etMUAC);
        etMilestones = findViewById(R.id.etMilestones);
        etNotes = findViewById(R.id.etNotes);
        spinnerNutritionalStatus = findViewById(R.id.spinnerNutritionalStatus);

        btnSave = findViewById(R.id.btnSave);

        dbHelper = DatabaseHelper.getInstance(this);
        patient = dbHelper.getPatientById(patientId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(dateCalendar.getTime()));
    }

    private void setupSpinners() {
        String[] statuses = {"Normal", "Underweight", "Severe Underweight", "Stunting", "Severe Stunting", "Wasting", "Overweight"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        spinnerNutritionalStatus.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                dateCalendar.set(Calendar.YEAR, year);
                dateCalendar.set(Calendar.MONTH, month);
                dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etDate.setText(sdf.format(dateCalendar.getTime()));
            }, dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                saveGrowth();
            }
        });
    }

    private void checkEditMode() {
        long growthId = getIntent().getLongExtra("growth_id", -1);
        if (growthId != -1) {
            isEditMode = true;
            editGrowth = dbHelper.getChildGrowthById(growthId);
            if (editGrowth != null) {
                populateForm(editGrowth);
                btnSave.setText("Update Record");
            }
        }
    }

    private void populateForm(ChildGrowth growth) {
        etDate.setText(growth.getMeasurementDate());
        etAgeMonths.setText(String.valueOf(growth.getAgeMonths()));
        etWeight.setText(String.valueOf(growth.getWeight()));
        etHeight.setText(String.valueOf(growth.getHeight()));
        etHeadCircumference.setText(String.valueOf(growth.getHeadCircumference()));
        etMUAC.setText(String.valueOf(growth.getMuac()));
        spinnerNutritionalStatus.setText(growth.getNutritionalStatus(), false);
        etMilestones.setText(growth.getMilestones());
        etNotes.setText(growth.getNotes());
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (etDate.getText().toString().isEmpty()) {
            tilDate.setError("Date is required");
            isValid = false;
        } else {
            tilDate.setError(null);
        }

        if (etAgeMonths.getText().toString().isEmpty()) {
            tilAgeMonths.setError("Age is required");
            isValid = false;
        } else {
            tilAgeMonths.setError(null);
        }

        if (etWeight.getText().toString().isEmpty()) {
            tilWeight.setError("Weight is required");
            isValid = false;
        } else {
            tilWeight.setError(null);
        }

        if (etHeight.getText().toString().isEmpty()) {
            tilHeight.setError("Height is required");
            isValid = false;
        } else {
            tilHeight.setError(null);
        }

        return isValid;
    }

    private void saveGrowth() {
        showLoading(true);

        ChildGrowth growth = new ChildGrowth();
        if (isEditMode && editGrowth != null) {
            growth.setId(editGrowth.getId());
            growth.setServerId(editGrowth.getServerId());
        }

        growth.setPatientId(patientId);
        if (patient != null) {
            growth.setPatientServerId(patient.getServerId());
        }
        growth.setMeasurementDate(etDate.getText().toString().trim());

        try {
            growth.setAgeMonths(Integer.parseInt(etAgeMonths.getText().toString().trim()));
        } catch (NumberFormatException e) {
            growth.setAgeMonths(0);
        }

        try {
            growth.setWeight((float) Double.parseDouble(etWeight.getText().toString().trim()));
        } catch (NumberFormatException e) {
            growth.setWeight(0);
        }

        try {
            growth.setHeight((float) Double.parseDouble(etHeight.getText().toString().trim()));
        } catch (NumberFormatException e) {
            growth.setHeight(0);
        }

        try {
            growth.setHeadCircumference((float) Double.parseDouble(etHeadCircumference.getText().toString().trim()));
        } catch (NumberFormatException e) {
            growth.setHeadCircumference(0);
        }

        try {
            growth.setMuac((float) Double.parseDouble(etMUAC.getText().toString().trim()));
        } catch (NumberFormatException e) {
            growth.setMuac(0);
        }

        growth.setNutritionalStatus(spinnerNutritionalStatus.getText().toString().trim());
        growth.setMilestones(etMilestones.getText().toString().trim());
        growth.setNotes(etNotes.getText().toString().trim());

        growth.setSyncStatus(NetworkUtils.isNetworkAvailable(this) ? "SYNCED" : "PENDING");

        long result;
        if (isEditMode) {
            result = dbHelper.updateChildGrowth(growth);
        } else {
            result = dbHelper.insertChildGrowth(growth);
        }

        showLoading(false);

        if (result != -1) {
            String message = isEditMode ? "Record updated" : "Record saved";
            if ("PENDING".equals(growth.getSyncStatus())) {
                message += " (will sync when online)";
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        scrollView.setAlpha(show ? 0.5f : 1.0f);
    }
}
