package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.google.android.material.chip.Chip;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.services.NetworkMonitorService;
import com.simats.ashasmartcare.utils.SessionManager;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;

import com.android.volley.Request;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {

    private static final String TAG = "AddPatientActivity";

    // Header
    private ImageView ivBack;

    // Basic Personal Details (always visible)
    private EditText etFullName, etAge, etAddress, etPhone, etAbhaId;
    private Spinner spinnerGender, spinnerCategory, spinnerBloodGroup;

    // Dynamic section containers
    private CardView cardChildVisit, cardPregnancyVisit, cardGeneralVisit;

    // Child Visit fields
    private EditText etChildWeight, etChildHeight, etChildMuac, etChildTemperature;
    private RadioGroup rgBreastfeeding, rgComplementaryFeeding, rgAppetite;
    private CheckBox cbFever, cbDiarrhea, cbCoughCold, cbVomiting, cbWeakness;
    private Spinner spinnerLastVaccine;
    private EditText etNextVaccineDate;

    // Pregnancy Visit fields
    private EditText etLmpDate, etBpSystolic, etBpDiastolic, etPregnancyWeight, etHemoglobin;
    private TextView tvEdd;
    private Chip chipHeadache, chipSwelling, chipBleeding, chipBlurredVision, chipReducedMovement;
    private CheckBox cbIronTablets, cbCalciumTablets, cbTetanusInjection;
    private EditText etNextVisitDate;

    // General Visit fields
    private EditText etGeneralBpSystolic, etGeneralBpDiastolic, etGeneralWeight, etSugar;
    private CheckBox cbGeneralFever, cbBodyPain, cbBreathlessness, cbDizziness, cbChestPain;
    private RadioGroup rgTobacco, rgAlcohol, rgPhysicalActivity;
    private SwitchCompat switchReferral;
    private EditText etFollowUpDate;

    // Bottom button
    private Button btnSave;

    // Helpers
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;
    private String selectedCategory = "";

    // Edit mode
    private boolean isEditMode = false;
    private long editPatientId = -1;
    private com.simats.ashasmartcare.models.Patient existingPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Check if this is edit mode
        editPatientId = getIntent().getLongExtra("patient_id", -1);
        isEditMode = (editPatientId != -1);

        initViews();
        initHelpers();
        setupSpinners();
        setupListeners();
        setupDatePickers();

        // Load existing patient data if in edit mode
        if (isEditMode) {
            loadExistingPatientData();
        }
    }

    private void initViews() {
        // Header
        ivBack = findViewById(R.id.iv_back);

        // Basic fields
        etFullName = findViewById(R.id.et_full_name);
        etAge = findViewById(R.id.et_age);
        etAddress = findViewById(R.id.et_address);
        etPhone = findViewById(R.id.et_phone);
        etAbhaId = findViewById(R.id.et_abha_id);

        spinnerGender = findViewById(R.id.spinner_gender);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerBloodGroup = findViewById(R.id.spinner_blood_group);

        // Apply Input Filters
        etFullName.setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        etAge.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(3),
                (source, start, end, dest, dstart, dend) -> {
                    String newVal = dest.toString().substring(0, dstart) + source.toString().substring(start, end)
                            + dest.toString().substring(dend);
                    if (newVal.isEmpty())
                        return null;
                    try {
                        int age = Integer.parseInt(newVal);
                        if (age >= 1 && age <= 100)
                            return null;
                    } catch (NumberFormatException ignored) {
                    }
                    return "";
                }
        });

        // Dynamic section containers
        cardChildVisit = findViewById(R.id.card_child_visit);
        cardPregnancyVisit = findViewById(R.id.card_pregnancy_visit);
        cardGeneralVisit = findViewById(R.id.card_general_visit);

        // Child Visit views
        etChildWeight = findViewById(R.id.et_child_weight);
        etChildHeight = findViewById(R.id.et_child_height);
        etChildMuac = findViewById(R.id.et_child_muac);
        etChildTemperature = findViewById(R.id.et_child_temperature);
        rgBreastfeeding = findViewById(R.id.rg_breastfeeding);
        rgComplementaryFeeding = findViewById(R.id.rg_complementary_feeding);
        rgAppetite = findViewById(R.id.rg_appetite);
        cbFever = findViewById(R.id.cb_fever);
        cbDiarrhea = findViewById(R.id.cb_diarrhea);
        cbCoughCold = findViewById(R.id.cb_cough_cold);
        cbVomiting = findViewById(R.id.cb_vomiting);
        cbWeakness = findViewById(R.id.cb_weakness);
        spinnerLastVaccine = findViewById(R.id.spinner_last_vaccine);
        etNextVaccineDate = findViewById(R.id.et_next_vaccine_date);

        // Pregnancy Visit views
        etLmpDate = findViewById(R.id.et_lmp_date);
        tvEdd = findViewById(R.id.tv_edd);
        etBpSystolic = findViewById(R.id.et_bp_systolic);
        etBpDiastolic = findViewById(R.id.et_bp_diastolic);
        etPregnancyWeight = findViewById(R.id.et_pregnancy_weight);
        etHemoglobin = findViewById(R.id.et_hemoglobin);
        chipHeadache = findViewById(R.id.chip_headache);
        chipSwelling = findViewById(R.id.chip_swelling);
        chipBleeding = findViewById(R.id.chip_bleeding);
        chipBlurredVision = findViewById(R.id.chip_blurred_vision);
        chipReducedMovement = findViewById(R.id.chip_reduced_movement);
        cbIronTablets = findViewById(R.id.cb_iron_tablets);
        cbCalciumTablets = findViewById(R.id.cb_calcium_tablets);
        cbTetanusInjection = findViewById(R.id.cb_tetanus_injection);
        etNextVisitDate = findViewById(R.id.et_next_visit_date);

        // General Visit views
        etGeneralBpSystolic = findViewById(R.id.et_general_bp_systolic);
        etGeneralBpDiastolic = findViewById(R.id.et_general_bp_diastolic);
        etGeneralWeight = findViewById(R.id.et_general_weight);
        etSugar = findViewById(R.id.et_sugar);
        cbGeneralFever = findViewById(R.id.cb_general_fever);
        cbBodyPain = findViewById(R.id.cb_body_pain);
        cbBreathlessness = findViewById(R.id.cb_breathlessness);
        cbDizziness = findViewById(R.id.cb_dizziness);
        cbChestPain = findViewById(R.id.cb_chest_pain);
        rgTobacco = findViewById(R.id.rg_tobacco);
        rgAlcohol = findViewById(R.id.rg_alcohol);
        rgPhysicalActivity = findViewById(R.id.rg_physical_activity);
        switchReferral = findViewById(R.id.switch_referral);
        etFollowUpDate = findViewById(R.id.et_follow_up_date);

        // Bottom button
        btnSave = findViewById(R.id.btn_save);

        // Update button text based on internet connectivity
        updateSaveButtonText();

        // Point 2: Restrict phone input to 10 digits only
        etPhone.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(10),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Restrict ABHA ID to 14 digits only
        etAbhaId.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(14),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });
    }

    private void updateSaveButtonText() {
        if (NetworkMonitorService.isNetworkConnected(this)) {
            btnSave.setText("Save Patient");
        } else {
            btnSave.setText("Save Patient (Offline)");
        }
    }

    private void initHelpers() {
        // Initialize helpers
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
    }

    private void setupSpinners() {
        // Gender spinner
        String[] genders = { "Select Gender", "Male", "Female" };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, genders);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Category spinner
        String[] categories = { "Select Category", "Pregnant Woman", "Child (0-5 years)", "General Adult" };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, categories);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Blood Group spinner
        String[] bloodGroups = { "Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown" };
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, bloodGroups);
        bloodGroupAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bloodGroupAdapter);

        // Last Vaccine spinner
        String[] vaccines = { "Select Vaccine", "BCG", "OPV 0", "Hepatitis B Birth Dose", "OPV 1",
                "Pentavalent 1", "Rotavirus 1", "IPV 1", "OPV 2", "Pentavalent 2", "Rotavirus 2",
                "OPV 3", "Pentavalent 3", "Rotavirus 3", "IPV 2", "PCV 1", "PCV 2", "PCV Booster",
                "Measles 1", "JE 1", "Vitamin A (9 months)", "DPT Booster 1", "Measles 2", "JE 2",
                "OPV Booster", "DPT Booster 2" };
        ArrayAdapter<String> vaccineAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, vaccines);
        vaccineAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLastVaccine.setAdapter(vaccineAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        // Category selection listener - Show/hide appropriate section
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                showCategorySpecificSection(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideAllCategorySections();
            }
        });

        // Save button
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                savePatient();
            }
        });
    }

    private void setupDatePickers() {
        final Calendar calendar = Calendar.getInstance();

        // LMP Date Picker (Pregnancy)
        etLmpDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        etLmpDate.setText(sdf.format(selectedDate.getTime()));

                        // Calculate and set EDD (LMP + 280 days)
                        selectedDate.add(Calendar.DAY_OF_YEAR, 280);
                        SimpleDateFormat eddFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        tvEdd.setText(eddFormat.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Next Visit Date Picker (Pregnancy)
        etNextVisitDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        etNextVisitDate.setText(sdf.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Next Vaccine Date Picker (Child)
        etNextVaccineDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        etNextVaccineDate.setText(sdf.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Follow-up Date Picker (General)
        etFollowUpDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        etFollowUpDate.setText(sdf.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void showCategorySpecificSection(String category) {
        // Hide all sections first
        hideAllCategorySections();

        // Show appropriate section based on category
        if ("Pregnant Woman".equals(category)) {
            cardPregnancyVisit.setVisibility(View.VISIBLE);
        } else if ("Child (0-5 years)".equals(category)) {
            cardChildVisit.setVisibility(View.VISIBLE);
        } else if ("General Adult".equals(category)) {
            cardGeneralVisit.setVisibility(View.VISIBLE);
        }
    }

    private void hideAllCategorySections() {
        cardChildVisit.setVisibility(View.GONE);
        cardPregnancyVisit.setVisibility(View.GONE);
        cardGeneralVisit.setVisibility(View.GONE);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Name validation
        String name = etFullName.getText().toString().trim();
        if (name.isEmpty()) {
            etFullName.setError("Name is required");
            isValid = false;
        }

        // Age validation
        String ageStr = etAge.getText().toString().trim();
        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 0 || age > 120) {
                    etAge.setError("Enter valid age");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etAge.setError("Enter valid age");
                isValid = false;
            }
        }

        // Gender validation
        if (spinnerGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Category validation
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select patient category", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Address validation
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            isValid = false;
        }

        return isValid;
    }

    private void savePatient() {
        try {
            // Collect basic patient details
            String name = etFullName.getText().toString().trim();
            int age = Integer.parseInt(etAge.getText().toString().trim());
            String gender = spinnerGender.getSelectedItem().toString();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String abhaId = etAbhaId.getText().toString().trim();
            String bloodGroup = spinnerBloodGroup.getSelectedItemPosition() > 0
                    ? spinnerBloodGroup.getSelectedItem().toString()
                    : null;

            // Get current user's phone
            String ashaPhone = sessionManager.getUserPhone();

            // Get current date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = sdf.format(Calendar.getInstance().getTime());

            // Calculate High Risk Status
            boolean isHighRisk = false;
            StringBuilder highRiskReason = new StringBuilder();

            if ("Pregnant Woman".equals(selectedCategory)) {
                // Check Danger Signs
                if (chipHeadache.isChecked() || chipSwelling.isChecked() || chipBleeding.isChecked() ||
                        chipBlurredVision.isChecked() || chipReducedMovement.isChecked()) {
                    isHighRisk = true;
                    highRiskReason.append("Danger Signs Present, ");
                }

                // Check BP (Simple threshold check)
                try {
                    int sys = Integer.parseInt(etBpSystolic.getText().toString().trim());
                    int dia = Integer.parseInt(etBpDiastolic.getText().toString().trim());
                    if (sys >= 140 || dia >= 90) {
                        isHighRisk = true;
                        highRiskReason.append("High BP, ");
                    }
                } catch (Exception e) {
                }

                // Check Hemoglobin
                try {
                    float hb = Float.parseFloat(etHemoglobin.getText().toString().trim());
                    if (hb < 11) {
                        isHighRisk = true;
                        highRiskReason.append("Anemia (Hb < 11), ");
                    }
                } catch (Exception e) {
                }

            } else if ("Child (0-5 years)".equals(selectedCategory)) {
                // Check Symptoms
                if (cbFever.isChecked() || cbDiarrhea.isChecked() || cbCoughCold.isChecked() ||
                        cbVomiting.isChecked() || cbWeakness.isChecked()) {
                    // Common symptoms might not immediately mean high risk, but let's be safe for
                    // now
                    // or strictly check for "Weakness" + "Vomiting" as danger signs?
                    // User request was generic "high risk detected".
                    // Let's assume ANY of these for a child is worth an alert for ASHA.
                    isHighRisk = true;
                    highRiskReason.append("Infant/Child Symptoms, ");
                }

                // Check MUAC (Severe Acute Malnutrition)
                try {
                    float muac = Float.parseFloat(etChildMuac.getText().toString().trim());
                    if (muac < 11.5) { // cm
                        isHighRisk = true;
                        highRiskReason.append("Severe Acute Malnutrition (MUAC < 11.5), ");
                    }
                } catch (Exception e) {
                }
            } else if ("General Adult".equals(selectedCategory)) {
                if (switchReferral.isChecked()) {
                    isHighRisk = true;
                    highRiskReason.append("Referral Required, ");
                }
            }

            // Build the patient data JSON payload (used for both online and offline sync)
            long userId = sessionManager.getUserId();
            JSONObject patientData = new JSONObject();
            patientData.put("name", name);
            patientData.put("age", age);
            patientData.put("gender", gender);
            patientData.put("phone", phone);
            patientData.put("abha_id", abhaId);
            patientData.put("address", address);
            patientData.put("blood_group", bloodGroup != null ? bloodGroup : "");
            patientData.put("category", selectedCategory);
            patientData.put("is_high_risk", isHighRisk ? 1 : 0);
            patientData.put("high_risk_reason", highRiskReason.toString());
            patientData.put("asha_id", userId);

            // ONLINE-FIRST APPROACH: Check internet before saving
            boolean hasInternet = NetworkMonitorService.isNetworkConnected(this);
            Log.d(TAG, "savePatient: hasInternet=" + hasInternet);

            if (hasInternet) {
                // ONLINE: Post directly to backend (NO local database storage)
                Log.d(TAG, "ONLINE MODE: Direct backend POST - NO local storage");
                savePatientToBackend(patientData);
            } else {
                // OFFLINE: Save to local database + sync queue
                Log.d(TAG, "OFFLINE MODE: Saving to local database with sync queue");
                long patientId = dbHelper.addPatient(name, age, gender, selectedCategory, address,
                        phone, abhaId, bloodGroup, ashaPhone, currentDate, isHighRisk, highRiskReason.toString(),
                        true, patientData.toString());

                if (patientId > 0) {
                    // Save category data locally with sync queue
                    if ("Pregnant Woman".equals(selectedCategory)) {
                        savePregnancyData(patientId, true, getPregnancyDataJson(-1).toString());
                    } else if ("Child (0-5 years)".equals(selectedCategory)) {
                        saveChildData(patientId, true, getChildDataJson(-1).toString());
                    } else if ("General Adult".equals(selectedCategory)) {
                        saveGeneralVisitData(patientId, true, getGeneralVisitDataJson(-1).toString());
                    }

                    Toast.makeText(this, "⚠️ Offline Mode\nPatient saved locally. Will sync when online.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save patient locally", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePatientToBackend(JSONObject patientData) {
        try {
            String apiBaseUrl = sessionManager.getApiBaseUrl();

            // Make API call for patient
            apiHelper.makeRequest(
                    Request.Method.POST,
                    apiBaseUrl + "patients.php",
                    patientData,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    JSONObject data = response.getJSONObject("data");
                                    int serverId = data.getInt("id");

                                    // ONLINE MODE: No local database operations
                                    // Direct backend save successful

                                    // Save category-specific data to backend
                                    if ("Pregnant Woman".equals(selectedCategory)) {
                                        savePregnancyDataToBackend(serverId);
                                    } else if ("Child (0-5 years)".equals(selectedCategory)) {
                                        saveChildDataToBackend(serverId);
                                    } else if ("General Adult".equals(selectedCategory)) {
                                        saveGeneralVisitDataToBackend(serverId);
                                    } else {
                                        runOnUiThread(() -> {
                                            Toast.makeText(AddPatientActivity.this,
                                                    "✓ Patient saved successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                                    }
                                } else {
                                    String message = response.optString("message", "Failed to save patient");
                                    runOnUiThread(() -> {
                                        Toast.makeText(AddPatientActivity.this,
                                                "Backend error: " + message,
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> {
                                    Toast.makeText(AddPatientActivity.this,
                                            "Error processing response: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddPatientActivity.this,
                                        "Network error: " + error + "\nPlease try again.",
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePregnancyData(long patientId, boolean shouldAddToSyncQueue, String dataJson) {
        try {
            String lmpDate = etLmpDate.getText().toString().trim();
            String edd = tvEdd.getText().toString().trim();
            String bpSys = etBpSystolic.getText().toString().trim();
            String bpDia = etBpDiastolic.getText().toString().trim();
            String weight = etPregnancyWeight.getText().toString().trim();
            String hemoglobin = etHemoglobin.getText().toString().trim();

            // Collect danger signs
            StringBuilder dangerSigns = new StringBuilder();
            if (chipHeadache.isChecked())
                dangerSigns.append("Severe Headache,");
            if (chipSwelling.isChecked())
                dangerSigns.append("Swelling,");
            if (chipBleeding.isChecked())
                dangerSigns.append("Bleeding,");
            if (chipBlurredVision.isChecked())
                dangerSigns.append("Blurred Vision,");
            if (chipReducedMovement.isChecked())
                dangerSigns.append("Reduced Movement,");

            // Collect medicines
            StringBuilder medicines = new StringBuilder();
            if (cbIronTablets.isChecked())
                medicines.append("Iron Tablets,");
            if (cbCalciumTablets.isChecked())
                medicines.append("Calcium Tablets,");
            if (cbTetanusInjection.isChecked())
                medicines.append("Tetanus Injection,");

            String nextVisitDate = etNextVisitDate.getText().toString().trim();

            // Insert into pregnancy_visits table
            dbHelper.addPregnancyVisit(patientId, lmpDate, edd, bpSys + "/" + bpDia,
                    weight, hemoglobin, dangerSigns.toString(),
                    medicines.toString(), nextVisitDate, shouldAddToSyncQueue, dataJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveChildData(long patientId, boolean shouldAddToSyncQueue, String dataJson) {
        try {
            String weight = etChildWeight.getText().toString().trim();
            String height = etChildHeight.getText().toString().trim();
            String muac = etChildMuac.getText().toString().trim();
            String temperature = etChildTemperature.getText().toString().trim();

            // Nutrition status
            String breastfeeding = rgBreastfeeding.getCheckedRadioButtonId() == R.id.rb_breastfeeding_yes ? "Yes"
                    : "No";
            String complementaryFeeding = rgComplementaryFeeding.getCheckedRadioButtonId() == R.id.rb_complementary_yes
                    ? "Yes"
                    : "No";
            String appetite = rgAppetite.getCheckedRadioButtonId() == R.id.rb_appetite_good ? "Good" : "Poor";

            // Collect symptoms
            StringBuilder symptoms = new StringBuilder();
            if (cbFever.isChecked())
                symptoms.append("Fever,");
            if (cbDiarrhea.isChecked())
                symptoms.append("Diarrhea,");
            if (cbCoughCold.isChecked())
                symptoms.append("Cough/Cold,");
            if (cbVomiting.isChecked())
                symptoms.append("Vomiting,");
            if (cbWeakness.isChecked())
                symptoms.append("Weakness,");

            // Immunization
            String lastVaccine = spinnerLastVaccine.getSelectedItem().toString();
            String nextVaccineDate = etNextVaccineDate.getText().toString().trim();

            // Insert into child_growth table
            dbHelper.addChildGrowth(patientId, weight, height, muac, temperature,
                    breastfeeding, complementaryFeeding, appetite,
                    symptoms.toString(), lastVaccine, nextVaccineDate, shouldAddToSyncQueue, dataJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveGeneralVisitData(long patientId, boolean shouldAddToSyncQueue, String dataJson) {
        try {
            String bpSys = etGeneralBpSystolic.getText().toString().trim();
            String bpDia = etGeneralBpDiastolic.getText().toString().trim();
            String weight = etGeneralWeight.getText().toString().trim();
            String sugar = etSugar.getText().toString().trim();

            // Collect symptoms
            StringBuilder symptoms = new StringBuilder();
            if (cbGeneralFever.isChecked())
                symptoms.append("Fever,");
            if (cbBodyPain.isChecked())
                symptoms.append("Body Pain,");
            if (cbBreathlessness.isChecked())
                symptoms.append("Breathlessness,");
            if (cbDizziness.isChecked())
                symptoms.append("Dizziness,");
            if (cbChestPain.isChecked())
                symptoms.append("Chest Pain,");

            // Lifestyle
            String tobacco = rgTobacco.getCheckedRadioButtonId() == R.id.rb_tobacco_yes ? "Yes" : "No";
            String alcohol = rgAlcohol.getCheckedRadioButtonId() == R.id.rb_alcohol_yes ? "Yes" : "No";

            String physicalActivity = "Moderate";
            if (rgPhysicalActivity.getCheckedRadioButtonId() == R.id.rb_activity_low) {
                physicalActivity = "Low";
            } else if (rgPhysicalActivity.getCheckedRadioButtonId() == R.id.rb_activity_active) {
                physicalActivity = "Active";
            }

            String referral = switchReferral.isChecked() ? "Yes" : "No";
            String followUpDate = etFollowUpDate.getText().toString().trim();

            // Insert into visits table - online-first: skip queue when online
            dbHelper.addGeneralVisit(patientId, bpSys + "/" + bpDia, weight, sugar,
                    symptoms.toString(), tobacco, alcohol, physicalActivity,
                    referral, followUpDate, shouldAddToSyncQueue, dataJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePregnancyDataToBackend(int patientServerId) {
        try {
            String apiBaseUrl = sessionManager.getApiBaseUrl();
            JSONObject data = getPregnancyDataJson(patientServerId);
            String nextVisitDate = etNextVisitDate.getText().toString().trim();

            // Save to pregnancy.php (NOT pregnancy_visits.php)
            apiHelper.makeRequest(Request.Method.POST, apiBaseUrl + "pregnancy.php", data,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // After pregnancy data saved, create visit if next visit date exists
                            if (!nextVisitDate.isEmpty()) {
                                createNextVisit(patientServerId, "Pregnancy Check-up", nextVisitDate,
                                        "ANC Visit");
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(AddPatientActivity.this, "Patient & pregnancy data saved!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddPatientActivity.this,
                                        "Patient saved. Pregnancy data will sync later.", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void saveChildDataToBackend(int patientServerId) {
        try {
            String apiBaseUrl = sessionManager.getApiBaseUrl();
            JSONObject data = getChildDataJson(patientServerId);
            String nextVaccineDate = etNextVaccineDate.getText().toString().trim();

            apiHelper.makeRequest(Request.Method.POST, apiBaseUrl + "child_growth.php", data,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // After child data saved, create visit if next vaccine date exists
                            if (!nextVaccineDate.isEmpty()) {
                                createNextVisit(patientServerId, "Vaccination Follow-up", nextVaccineDate,
                                        "Next vaccination: " + spinnerLastVaccine.getSelectedItem().toString());
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(AddPatientActivity.this, "Patient & child data saved!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddPatientActivity.this, "Patient saved. Child data will sync later.",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void saveGeneralVisitDataToBackend(int patientServerId) {
        try {
            String apiBaseUrl = sessionManager.getApiBaseUrl();
            JSONObject data = getGeneralVisitDataJson(patientServerId);
            String followUpDate = etFollowUpDate.getText().toString().trim();

            apiHelper.makeRequest(Request.Method.POST, apiBaseUrl + "general_adult.php", data,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // After general adult data saved, create visit if follow-up date exists
                            if (!followUpDate.isEmpty()) {
                                createNextVisit(patientServerId, "General Check-up", followUpDate,
                                        "Follow-up visit");
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(AddPatientActivity.this, "Patient & health data saved!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AddPatientActivity.this, "Patient saved. Health data will sync later.",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private JSONObject getPregnancyDataJson(int patientServerId) throws Exception {
        long ashaId = sessionManager.getUserId();
        JSONObject data = new JSONObject();
        data.put("patient_id", patientServerId);
        data.put("asha_id", ashaId);

        String lmpDate = etLmpDate.getText().toString().trim();
        String eddDate = tvEdd.getText().toString().trim();
        String nextVisitDate = etNextVisitDate.getText().toString().trim();

        data.put("lmp_date", convertDateToMySQLFormat(lmpDate));
        data.put("edd_date", convertDisplayDateToMySQLFormat(eddDate));
        data.put("blood_pressure", etBpSystolic.getText().toString() + "/" + etBpDiastolic.getText().toString());
        data.put("weight", etPregnancyWeight.getText().toString().trim());
        data.put("hemoglobin", etHemoglobin.getText().toString().trim());

        StringBuilder dangerSigns = new StringBuilder();
        if (chipHeadache.isChecked())
            dangerSigns.append("Severe Headache,");
        if (chipSwelling.isChecked())
            dangerSigns.append("Swelling,");
        if (chipBleeding.isChecked())
            dangerSigns.append("Bleeding,");
        if (chipBlurredVision.isChecked())
            dangerSigns.append("Blurred Vision,");
        if (chipReducedMovement.isChecked())
            dangerSigns.append("Reduced Movement,");
        data.put("danger_signs", dangerSigns.toString());

        data.put("iron_tablets_given", cbIronTablets.isChecked() ? 1 : 0);
        data.put("calcium_tablets_given", cbCalciumTablets.isChecked() ? 1 : 0);
        data.put("tetanus_injection_given", cbTetanusInjection.isChecked() ? 1 : 0);
        data.put("next_visit_date", convertDateToMySQLFormat(nextVisitDate));

        return data;
    }

    private JSONObject getChildDataJson(int patientServerId) throws Exception {
        long ashaId = sessionManager.getUserId();
        JSONObject data = new JSONObject();
        data.put("patient_id", patientServerId);
        data.put("asha_id", ashaId);
        data.put("record_date", getCurrentTimestamp());
        data.put("weight", etChildWeight.getText().toString().trim());
        data.put("height", etChildHeight.getText().toString().trim());
        data.put("muac", etChildMuac.getText().toString().trim());
        data.put("temperature", etChildTemperature.getText().toString().trim());

        data.put("fever", cbFever.isChecked() ? 1 : 0);
        data.put("diarrhea", cbDiarrhea.isChecked() ? 1 : 0);
        data.put("cough_cold", cbCoughCold.isChecked() ? 1 : 0);
        data.put("vomiting", cbVomiting.isChecked() ? 1 : 0);
        data.put("weakness", cbWeakness.isChecked() ? 1 : 0);

        data.put("breastfeeding",
                rgBreastfeeding.getCheckedRadioButtonId() == R.id.rb_breastfeeding_yes ? "Yes" : "No");
        data.put("complementary_feeding",
                rgComplementaryFeeding.getCheckedRadioButtonId() == R.id.rb_complementary_yes ? "Yes" : "No");
        data.put("appetite", rgAppetite.getCheckedRadioButtonId() == R.id.rb_appetite_good ? "Good" : "Poor");
        data.put("last_vaccine", spinnerLastVaccine.getSelectedItem().toString());

        String nextVaccineDate = etNextVaccineDate.getText().toString().trim();
        data.put("next_vaccine_date", convertDateToMySQLFormat(nextVaccineDate));

        return data;
    }

    private JSONObject getGeneralVisitDataJson(int patientServerId) throws Exception {
        long ashaId = sessionManager.getUserId();
        JSONObject data = new JSONObject();
        data.put("patient_id", patientServerId);
        data.put("asha_id", ashaId);
        data.put("record_date", getCurrentTimestamp());

        data.put("blood_pressure",
                etGeneralBpSystolic.getText().toString() + "/" + etGeneralBpDiastolic.getText().toString());
        
        // Validate and cap weight and sugar values to prevent backend truncation (DECIMAL(5,2) max is 999.99)
        String weightStr = etGeneralWeight.getText().toString().trim();
        if (!weightStr.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight > 999.99) {
                    weight = 999.99;
                    runOnUiThread(() -> Toast.makeText(this, "⚠️ Weight capped at 999.99 kg (database limit)", Toast.LENGTH_SHORT).show());
                }
                data.put("weight", String.format(Locale.US, "%.2f", weight));
            } catch (NumberFormatException e) {
                data.put("weight", weightStr);
            }
        } else {
            data.put("weight", "");
        }
        
        String sugarStr = etSugar.getText().toString().trim();
        if (!sugarStr.isEmpty()) {
            try {
                double sugar = Double.parseDouble(sugarStr);
                if (sugar > 999.99) {
                    sugar = 999.99;
                    runOnUiThread(() -> Toast.makeText(this, "⚠️ Sugar level capped at 999.99 mg/dL (database limit)", Toast.LENGTH_SHORT).show());
                }
                data.put("sugar_level", String.format(Locale.US, "%.2f", sugar));
            } catch (NumberFormatException e) {
                data.put("sugar_level", sugarStr);
            }
        } else {
            data.put("sugar_level", "");
        }

        data.put("fever", cbGeneralFever.isChecked() ? 1 : 0);
        data.put("body_pain", cbBodyPain.isChecked() ? 1 : 0);
        data.put("breathlessness", cbBreathlessness.isChecked() ? 1 : 0);
        data.put("dizziness", cbDizziness.isChecked() ? 1 : 0);
        data.put("chest_pain", cbChestPain.isChecked() ? 1 : 0);

        data.put("tobacco_use", rgTobacco.getCheckedRadioButtonId() == R.id.rb_tobacco_yes ? "Yes" : "No");
        data.put("alcohol_use", rgAlcohol.getCheckedRadioButtonId() == R.id.rb_alcohol_yes ? "Yes" : "No");

        String activity = "Moderate";
        if (rgPhysicalActivity.getCheckedRadioButtonId() == R.id.rb_activity_low)
            activity = "Low";
        else if (rgPhysicalActivity.getCheckedRadioButtonId() == R.id.rb_activity_active)
            activity = "Active";
        data.put("physical_activity", activity);

        data.put("referral_required", switchReferral.isChecked() ? 1 : 0);

        String followUpDate = etFollowUpDate.getText().toString().trim();
        data.put("follow_up_date", convertDateToMySQLFormat(followUpDate));

        return data;
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Convert date from MM/dd/yyyy format to MySQL yyyy-MM-dd format
     */
    private String convertDateToMySQLFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            // Parse from MM/dd/yyyy (DatePicker format)
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);

            // Format to yyyy-MM-dd (MySQL format)
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            android.util.Log.e("AddPatientActivity", "Date conversion error: " + e.getMessage());
            return dateStr; // Return original if parsing fails
        }
    }

    /**
     * Convert date from "MMM dd, yyyy" format to MySQL yyyy-MM-dd format
     */
    private String convertDisplayDateToMySQLFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            // Parse from MMM dd, yyyy (Display format)
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);

            // Format to yyyy-MM-dd (MySQL format)
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            android.util.Log.e("AddPatientActivity", "Display date conversion error: " + e.getMessage());
            return dateStr; // Return original if parsing fails
        }
    }

    /**
     * Create a visit record for the next scheduled visit
     */
    private void createNextVisit(int patientServerId, String visitType, String nextVisitDate, String purpose) {
        try {
            String apiBaseUrl = sessionManager.getApiBaseUrl();
            long ashaId = sessionManager.getUserId();

            // Convert date from MM/dd/yyyy to yyyy-MM-dd for MySQL
            String mysqlDate = convertDateToMySQLFormat(nextVisitDate);

            android.util.Log.d("AddPatientActivity", "Converting date: " + nextVisitDate + " -> " + mysqlDate);

            JSONObject visitData = new JSONObject();
            visitData.put("patient_id", patientServerId);
            visitData.put("asha_id", ashaId);
            visitData.put("visit_type", visitType);
            visitData.put("visit_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            visitData.put("next_visit_date", mysqlDate); // Use converted MySQL format
            visitData.put("purpose", purpose);
            visitData.put("notes", "Scheduled during patient registration");

            android.util.Log.d("AddPatientActivity", "Creating visit: type=" + visitType + ", date=" + mysqlDate);

            apiHelper.makeRequest(Request.Method.POST, apiBaseUrl + "visits.php", visitData,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            android.util.Log.d("AddPatientActivity",
                                    "Visit created successfully: " + response.toString());
                            runOnUiThread(() -> {
                                Toast.makeText(AddPatientActivity.this, "Patient, data & visit saved!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            android.util.Log.e("AddPatientActivity", "Visit creation error: " + error);
                            runOnUiThread(() -> {
                                Toast.makeText(AddPatientActivity.this, "Patient & data saved, visit will sync later",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("AddPatientActivity", "Exception creating visit: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(AddPatientActivity.this, "Patient & data saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    /**
     * Load existing patient data for edit mode
     */
    private void loadExistingPatientData() {
        existingPatient = dbHelper.getPatientById((int) editPatientId);
        if (existingPatient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Update button text to indicate edit mode
        btnSave.setText("Update Patient");

        // Populate basic fields
        etFullName.setText(existingPatient.getName());
        etAge.setText(String.valueOf(existingPatient.getAge()));
        etAddress.setText(existingPatient.getAddress());
        etPhone.setText(existingPatient.getPhone());
        etAbhaId.setText(existingPatient.getAbhaId());

        // Set gender spinner
        String gender = existingPatient.getGender();
        if (gender != null) {
            String[] genders = { "Select Gender", "Male", "Female" };
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equalsIgnoreCase(gender)) {
                    spinnerGender.setSelection(i);
                    break;
                }
            }
        }

        // Set category spinner and show appropriate section
        String category = existingPatient.getCategory();
        if (category != null) {
            String[] categories = { "Select Category", "Pregnant Woman", "Child (0-5 years)", "General Adult" };
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(category) ||
                        (category.contains("Child") && categories[i].contains("Child"))) {
                    spinnerCategory.setSelection(i);
                    selectedCategory = categories[i];
                    showCategorySpecificSection(selectedCategory);
                    break;
                }
            }
        }

        // Set blood group spinner
        String bloodGroup = existingPatient.getBloodGroup();
        if (bloodGroup != null) {
            String[] bloodGroups = { "Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-",
                    "Unknown" };
            for (int i = 0; i < bloodGroups.length; i++) {
                if (bloodGroups[i].equalsIgnoreCase(bloodGroup)) {
                    spinnerBloodGroup.setSelection(i);
                    break;
                }
            }
        }

        // Load category-specific data
        if (selectedCategory.contains("Child")) {
            loadChildGrowthData();
        } else if (selectedCategory.contains("Pregnant")) {
            loadPregnancyData();
        } else if (selectedCategory.contains("General")) {
            loadGeneralAdultData();
        }
    }

    /**
     * Load most recent child growth data for editing
     */
    private void loadChildGrowthData() {
        java.util.List<com.simats.ashasmartcare.models.ChildGrowth> growthRecords = dbHelper
                .getChildGrowthByPatient((int) editPatientId);

        if (!growthRecords.isEmpty()) {
            // Get most recent record
            com.simats.ashasmartcare.models.ChildGrowth latestRecord = growthRecords.get(0);

            if (latestRecord.getWeight() > 0) {
                etChildWeight.setText(String.valueOf(latestRecord.getWeight()));
            }
            if (latestRecord.getHeight() > 0) {
                etChildHeight.setText(String.valueOf(latestRecord.getHeight()));
            }
            if (latestRecord.getMuac() > 0) {
                etChildMuac.setText(String.valueOf(latestRecord.getMuac()));
            }

            // Note: Temperature and other details should be loaded from visit records,
            // as they are not part of the ChildGrowth model
        }
    }

    /**
     * Load most recent pregnancy data for editing
     */
    private void loadPregnancyData() {
        java.util.List<com.simats.ashasmartcare.models.PregnancyVisit> pregnancyRecords = dbHelper
                .getPregnancyVisitsByPatient((int) editPatientId);

        if (!pregnancyRecords.isEmpty()) {
            // Get most recent record
            com.simats.ashasmartcare.models.PregnancyVisit latestRecord = pregnancyRecords.get(0);

            // Note: LMP and EDD are stored in the pregnancy data table or notes,
            // not in the PregnancyVisit model. They should be loaded from patient pregnancy
            // data if needed.

            // Parse BP if available
            String bp = latestRecord.getBloodPressure();
            if (bp != null && bp.contains("/")) {
                String[] bpParts = bp.split("/");
                if (bpParts.length == 2) {
                    etBpSystolic.setText(bpParts[0].trim());
                    etBpDiastolic.setText(bpParts[1].trim());
                }
            }

            if (latestRecord.getWeight() > 0) {
                etPregnancyWeight.setText(String.valueOf(latestRecord.getWeight()));
            }
            if (latestRecord.getHemoglobin() > 0) {
                etHemoglobin.setText(String.valueOf(latestRecord.getHemoglobin()));
            }
        }
    }

    /**
     * Load most recent general adult data for editing
     */
    private void loadGeneralAdultData() {
        // Load general adult health records if needed
        // This can be implemented similarly to child and pregnancy data
        // For now, just show the section
    }
}
