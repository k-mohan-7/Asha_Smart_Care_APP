package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.Constants;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class AddPatientActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    // Form fields
    private TextInputLayout tilName, tilAge, tilPhone, tilAddress, tilVillage, tilDistrict, tilState;
    private TextInputLayout tilCategory, tilBloodGroup, tilEmergencyContact, tilMedicalHistory;
    private TextInputEditText etName, etAge, etPhone, etAddress, etVillage, etDistrict;
    private TextInputEditText etEmergencyContact, etMedicalHistory;
    private AutoCompleteTextView spinnerState, spinnerCategory, spinnerBloodGroup;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    private Patient editPatient = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        initViews();
        setupSpinners();
        setupListeners();
        checkEditMode();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        tilName = findViewById(R.id.tilName);
        tilAge = findViewById(R.id.tilAge);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        tilVillage = findViewById(R.id.tilVillage);
        tilDistrict = findViewById(R.id.tilDistrict);
        tilState = findViewById(R.id.tilState);
        tilCategory = findViewById(R.id.tilCategory);
        tilBloodGroup = findViewById(R.id.tilBloodGroup);
        tilEmergencyContact = findViewById(R.id.tilEmergencyContact);
        tilMedicalHistory = findViewById(R.id.tilMedicalHistory);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etVillage = findViewById(R.id.etVillage);
        etDistrict = findViewById(R.id.etDistrict);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);

        spinnerState = findViewById(R.id.spinnerState);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);

        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);

        btnSave = findViewById(R.id.btnSave);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupSpinners() {
        // State spinner
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.INDIAN_STATES);
        spinnerState.setAdapter(stateAdapter);

        // Category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.PATIENT_CATEGORIES);
        spinnerCategory.setAdapter(categoryAdapter);

        // Blood group spinner
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, bloodGroups);
        spinnerBloodGroup.setAdapter(bloodAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                savePatient();
            }
        });
    }

    private void checkEditMode() {
        long patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId != -1) {
            isEditMode = true;
            editPatient = dbHelper.getPatientById(patientId);
            if (editPatient != null) {
                populateForm(editPatient);
                btnSave.setText("Update Patient");
            }
        }
    }

    private void populateForm(Patient patient) {
        etName.setText(patient.getName());
        etAge.setText(String.valueOf(patient.getAge()));
        etPhone.setText(patient.getPhone());
        etAddress.setText(patient.getAddress());
        etVillage.setText(patient.getVillage());
        etDistrict.setText(patient.getDistrict());
        spinnerState.setText(patient.getState(), false);
        spinnerCategory.setText(patient.getCategory(), false);
        spinnerBloodGroup.setText(patient.getBloodGroup(), false);
        etEmergencyContact.setText(patient.getEmergencyContact());
        etMedicalHistory.setText(patient.getMedicalHistory());

        // Set gender
        String gender = patient.getGender();
        if ("Male".equals(gender)) {
            rbMale.setChecked(true);
        } else if ("Female".equals(gender)) {
            rbFemale.setChecked(true);
        } else {
            rbOther.setChecked(true);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Name validation
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        // Age validation
        String ageStr = etAge.getText().toString().trim();
        if (ageStr.isEmpty()) {
            tilAge.setError("Age is required");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 0 || age > 120) {
                    tilAge.setError("Enter valid age");
                    isValid = false;
                } else {
                    tilAge.setError(null);
                }
            } catch (NumberFormatException e) {
                tilAge.setError("Enter valid age");
                isValid = false;
            }
        }

        // Phone validation
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            tilPhone.setError("Phone is required");
            isValid = false;
        } else if (phone.length() != 10) {
            tilPhone.setError("Enter 10 digit phone number");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        // Village validation
        String village = etVillage.getText().toString().trim();
        if (village.isEmpty()) {
            tilVillage.setError("Village is required");
            isValid = false;
        } else {
            tilVillage.setError(null);
        }

        // Category validation
        String category = spinnerCategory.getText().toString().trim();
        if (category.isEmpty()) {
            tilCategory.setError("Category is required");
            isValid = false;
        } else {
            tilCategory.setError(null);
        }

        return isValid;
    }

    private void savePatient() {
        showLoading(true);

        Patient patient = new Patient();
        if (isEditMode && editPatient != null) {
            patient.setId(editPatient.getId());
            patient.setServerId(editPatient.getServerId());
        }

        patient.setName(etName.getText().toString().trim());
        patient.setAge(Integer.parseInt(etAge.getText().toString().trim()));
        patient.setPhone(etPhone.getText().toString().trim());
        patient.setAddress(etAddress.getText().toString().trim());
        patient.setVillage(etVillage.getText().toString().trim());
        patient.setDistrict(etDistrict.getText().toString().trim());
        patient.setState(spinnerState.getText().toString().trim());
        patient.setCategory(spinnerCategory.getText().toString().trim());
        patient.setBloodGroup(spinnerBloodGroup.getText().toString().trim());
        patient.setEmergencyContact(etEmergencyContact.getText().toString().trim());
        patient.setMedicalHistory(etMedicalHistory.getText().toString().trim());
        patient.setAshaId(String.valueOf(sessionManager.getUserId()));

        // Get gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rbMale) {
            patient.setGender("Male");
        } else if (selectedGenderId == R.id.rbFemale) {
            patient.setGender("Female");
        } else {
            patient.setGender("Other");
        }

        // Set registration date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        patient.setRegistrationDate(sdf.format(Calendar.getInstance().getTime()));

        if (NetworkUtils.isNetworkAvailable(this)) {
            // Online: Try to save to server first
            savePatientToServer(patient);
        } else {
            // Offline: Save locally with pending status
            patient.setSyncStatus("PENDING");
            savePatientLocally(patient);
        }
    }

    private void savePatientToServer(Patient patient) {
        ApiHelper.ApiCallback callback = new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        String serverId = response.optString("patient_id", "");
                        try {
                            patient.setServerId(Integer.parseInt(serverId));
                        } catch (NumberFormatException e) {
                            patient.setServerId(0);
                        }
                        patient.setSyncStatus("SYNCED");
                        savePatientLocally(patient);
                    } else {
                        String message = response.optString("message", "Failed to save patient");
                        Toast.makeText(AddPatientActivity.this, message, Toast.LENGTH_SHORT).show();
                        // Save locally with pending status
                        patient.setSyncStatus("PENDING");
                        savePatientLocally(patient);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    patient.setSyncStatus("PENDING");
                    savePatientLocally(patient);
                }
            }

            @Override
            public void onError(String error) {
                // Network error, save locally with pending status
                patient.setSyncStatus("PENDING");
                savePatientLocally(patient);
            }
        };

        if (isEditMode) {
            apiHelper.updatePatient(patient, callback);
        } else {
            apiHelper.addPatient(patient, callback);
        }
    }

    private void savePatientLocally(Patient patient) {
        long result;
        if (isEditMode) {
            result = dbHelper.updatePatient(patient);
        } else {
            result = dbHelper.insertPatient(patient);
        }

        showLoading(false);

        if (result != -1) {
            String message = isEditMode ? "Patient updated successfully" : "Patient added successfully";
            if ("PENDING".equals(patient.getSyncStatus())) {
                message += " (will sync when online)";
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save patient", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        scrollView.setAlpha(show ? 0.5f : 1.0f);
    }
}
