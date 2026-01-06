package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.simats.ashasmartcare.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {

    // Header
    private ImageView ivBack;

    // Basic Personal Details (always visible)
    private EditText etFullName, etAge, etVillage, etPhone, etAbhaId;
    private Spinner spinnerGender, spinnerCategory;

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
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        initViews();
        setupSpinners();
        setupListeners();
        setupDatePickers();
    }

    private void initViews() {
        // Header
        ivBack = findViewById(R.id.iv_back);

        // Basic fields
        etFullName = findViewById(R.id.et_full_name);
        etAge = findViewById(R.id.et_age);
        etVillage = findViewById(R.id.et_village);
        etPhone = findViewById(R.id.et_phone);
        etAbhaId = findViewById(R.id.et_abha_id);
        spinnerGender = findViewById(R.id.spinner_gender);
        spinnerCategory = findViewById(R.id.spinner_category);

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

        // Initialize helpers
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupSpinners() {
        // Gender spinner
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Category spinner
        String[] categories = {"Select Category", "Pregnant Woman", "Child (0-5 years)", "General Adult"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Last Vaccine spinner
        String[] vaccines = {"Select Vaccine", "BCG", "OPV 0", "Hepatitis B Birth Dose", "OPV 1", 
                "Pentavalent 1", "Rotavirus 1", "IPV 1", "OPV 2", "Pentavalent 2", "Rotavirus 2", 
                "OPV 3", "Pentavalent 3", "Rotavirus 3", "IPV 2", "PCV 1", "PCV 2", "PCV Booster", 
                "Measles 1", "JE 1", "Vitamin A (9 months)", "DPT Booster 1", "Measles 2", "JE 2", 
                "OPV Booster", "DPT Booster 2"};
        ArrayAdapter<String> vaccineAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, vaccines);
        vaccineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        // Village validation
        String village = etVillage.getText().toString().trim();
        if (village.isEmpty()) {
            etVillage.setError("Village name is required");
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
            String village = etVillage.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String abhaId = etAbhaId.getText().toString().trim();

            // Get current user's phone
            String ashaPhone = sessionManager.getUserPhone();
            
            // Get current date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = sdf.format(Calendar.getInstance().getTime());

            // Insert patient into database
            long patientId = dbHelper.addPatient(name, age, gender, selectedCategory, village, 
                                                  phone, abhaId, ashaPhone, currentDate);

            if (patientId > 0) {
                // Save category-specific data
                if ("Pregnant Woman".equals(selectedCategory)) {
                    savePregnancyData(patientId);
                } else if ("Child (0-5 years)".equals(selectedCategory)) {
                    saveChildData(patientId);
                } else if ("General Adult".equals(selectedCategory)) {
                    saveGeneralVisitData(patientId);
                }

                Toast.makeText(this, "Patient saved successfully (Offline)", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and return to previous screen
            } else {
                Toast.makeText(this, "Failed to save patient", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePregnancyData(long patientId) {
        try {
            String lmpDate = etLmpDate.getText().toString().trim();
            String edd = tvEdd.getText().toString().trim();
            String bpSys = etBpSystolic.getText().toString().trim();
            String bpDia = etBpDiastolic.getText().toString().trim();
            String weight = etPregnancyWeight.getText().toString().trim();
            String hemoglobin = etHemoglobin.getText().toString().trim();
            
            // Collect danger signs
            StringBuilder dangerSigns = new StringBuilder();
            if (chipHeadache.isChecked()) dangerSigns.append("Severe Headache,");
            if (chipSwelling.isChecked()) dangerSigns.append("Swelling,");
            if (chipBleeding.isChecked()) dangerSigns.append("Bleeding,");
            if (chipBlurredVision.isChecked()) dangerSigns.append("Blurred Vision,");
            if (chipReducedMovement.isChecked()) dangerSigns.append("Reduced Movement,");

            // Collect medicines
            StringBuilder medicines = new StringBuilder();
            if (cbIronTablets.isChecked()) medicines.append("Iron Tablets,");
            if (cbCalciumTablets.isChecked()) medicines.append("Calcium Tablets,");
            if (cbTetanusInjection.isChecked()) medicines.append("Tetanus Injection,");

            String nextVisitDate = etNextVisitDate.getText().toString().trim();

            // Insert into pregnancy_visits table
            dbHelper.addPregnancyVisit(patientId, lmpDate, edd, bpSys + "/" + bpDia, 
                                        weight, hemoglobin, dangerSigns.toString(), 
                                        medicines.toString(), nextVisitDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveChildData(long patientId) {
        try {
            String weight = etChildWeight.getText().toString().trim();
            String height = etChildHeight.getText().toString().trim();
            String muac = etChildMuac.getText().toString().trim();
            String temperature = etChildTemperature.getText().toString().trim();

            // Nutrition status
            String breastfeeding = rgBreastfeeding.getCheckedRadioButtonId() == R.id.rb_breastfeeding_yes ? "Yes" : "No";
            String complementaryFeeding = rgComplementaryFeeding.getCheckedRadioButtonId() == R.id.rb_complementary_yes ? "Yes" : "No";
            String appetite = rgAppetite.getCheckedRadioButtonId() == R.id.rb_appetite_good ? "Good" : "Poor";

            // Collect symptoms
            StringBuilder symptoms = new StringBuilder();
            if (cbFever.isChecked()) symptoms.append("Fever,");
            if (cbDiarrhea.isChecked()) symptoms.append("Diarrhea,");
            if (cbCoughCold.isChecked()) symptoms.append("Cough/Cold,");
            if (cbVomiting.isChecked()) symptoms.append("Vomiting,");
            if (cbWeakness.isChecked()) symptoms.append("Weakness,");

            // Immunization
            String lastVaccine = spinnerLastVaccine.getSelectedItem().toString();
            String nextVaccineDate = etNextVaccineDate.getText().toString().trim();

            // Insert into child_growth table
            dbHelper.addChildGrowth(patientId, weight, height, muac, temperature,
                                     breastfeeding, complementaryFeeding, appetite,
                                     symptoms.toString(), lastVaccine, nextVaccineDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveGeneralVisitData(long patientId) {
        try {
            String bpSys = etGeneralBpSystolic.getText().toString().trim();
            String bpDia = etGeneralBpDiastolic.getText().toString().trim();
            String weight = etGeneralWeight.getText().toString().trim();
            String sugar = etSugar.getText().toString().trim();

            // Collect symptoms
            StringBuilder symptoms = new StringBuilder();
            if (cbGeneralFever.isChecked()) symptoms.append("Fever,");
            if (cbBodyPain.isChecked()) symptoms.append("Body Pain,");
            if (cbBreathlessness.isChecked()) symptoms.append("Breathlessness,");
            if (cbDizziness.isChecked()) symptoms.append("Dizziness,");
            if (cbChestPain.isChecked()) symptoms.append("Chest Pain,");

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

            // Insert into visits table
            dbHelper.addGeneralVisit(patientId, bpSys + "/" + bpDia, weight, sugar,
                                      symptoms.toString(), tobacco, alcohol, physicalActivity,
                                      referral, followUpDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
