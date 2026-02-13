package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.Vaccination;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.Constants;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddVaccinationActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvTitle;
    private Spinner spinnerVaccine, spinnerStatus;
    private EditText etScheduledDate, etGivenDate, etBatchNumber, etSideEffects, etNotes;
    private Button btnSave;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;

    private long patientId;
    private long vaccinationId = -1;
    private Vaccination existingVaccination;
    private Patient patient;

    private Calendar scheduledCalendar = Calendar.getInstance();
    private Calendar givenCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final String[] VACCINES = {
            "BCG", "OPV-0", "Hepatitis B Birth Dose",
            "OPV-1", "Pentavalent-1", "Rotavirus-1", "fIPV-1", "PCV-1",
            "OPV-2", "Pentavalent-2", "Rotavirus-2",
            "OPV-3", "Pentavalent-3", "Rotavirus-3", "fIPV-2", "PCV-2",
            "Measles-1", "Vitamin A - 1st Dose", "JE-1",
            "MR-1", "JE-2", "DPT Booster-1", "OPV Booster", "PCV Booster",
            "DPT Booster-2", "Vitamin A (2-5 years)",
            "TT-1", "TT-2", "TT Booster"
    };

    private static final String[] STATUS = {"Scheduled", "Given", "Missed", "Delayed"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vaccination);

        patientId = getIntent().getIntExtra("patient_id", -1);
        vaccinationId = getIntent().getIntExtra("vaccination_id", -1);

        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupSpinners();
        setupDatePickers();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        spinnerVaccine = findViewById(R.id.spinnerVaccine);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        etScheduledDate = findViewById(R.id.etScheduledDate);
        etGivenDate = findViewById(R.id.etGivenDate);
        etBatchNumber = findViewById(R.id.etBatchNumber);
        etSideEffects = findViewById(R.id.etSideEffects);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
    }

    private void setupSpinners() {
        ArrayAdapter<String> vaccineAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, VACCINES);
        vaccineAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerVaccine.setAdapter(vaccineAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, STATUS);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupDatePickers() {
        etScheduledDate.setOnClickListener(v -> showScheduledDatePicker());
        etGivenDate.setOnClickListener(v -> showGivenDatePicker());
    }

    private void showScheduledDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    scheduledCalendar.set(year, month, dayOfMonth);
                    etScheduledDate.setText(dateFormat.format(scheduledCalendar.getTime()));
                },
                scheduledCalendar.get(Calendar.YEAR),
                scheduledCalendar.get(Calendar.MONTH),
                scheduledCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showGivenDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    givenCalendar.set(year, month, dayOfMonth);
                    etGivenDate.setText(dateFormat.format(givenCalendar.getTime()));
                },
                givenCalendar.get(Calendar.YEAR),
                givenCalendar.get(Calendar.MONTH),
                givenCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveVaccination());
    }

    private void loadData() {
        if (vaccinationId != -1) {
            tvTitle.setText("Edit Vaccination");
            // ONLINE-FIRST: Load from API instead of local DB
            if (NetworkUtils.isNetworkAvailable(this)) {
                loadVaccinationFromServer();
            } else {
                Toast.makeText(this, "No internet connection. Cannot load vaccination data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            tvTitle.setText("Add Vaccination");
            etScheduledDate.setText(dateFormat.format(scheduledCalendar.getTime()));
        }
    }

    private void loadVaccinationFromServer() {
        showLoading(true);
        String url = Constants.API_BASE_URL + "vaccinations.php?id=" + vaccinationId;
        
        apiHelper.makeGetRequest(url, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    try {
                        // Handle both response formats
                        JSONObject vaccinationData = null;
                        if (response.has("data")) {
                            vaccinationData = response.getJSONObject("data");
                        } else if (response.has("vaccination")) {
                            vaccinationData = response.getJSONObject("vaccination");
                        }
                        
                        if (vaccinationData != null) {
                            populateFormFromJson(vaccinationData);
                        } else {
                            Toast.makeText(AddVaccinationActivity.this, "Failed to load vaccination", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(AddVaccinationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(AddVaccinationActivity.this, "Error loading: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void populateFormFromJson(JSONObject data) throws Exception {
        // Set vaccine
        String vaccineName = data.optString("vaccine_name", "");
        for (int i = 0; i < VACCINES.length; i++) {
            if (VACCINES[i].equals(vaccineName)) {
                spinnerVaccine.setSelection(i);
                break;
            }
        }
        
        // Set status
        String status = data.optString("status", "Scheduled");
        for (int i = 0; i < STATUS.length; i++) {
            if (STATUS[i].equalsIgnoreCase(status)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
        
        // Set dates
        String scheduledDate = data.optString("scheduled_date", "");
        if (!scheduledDate.isEmpty()) {
            try {
                scheduledCalendar.setTime(dbDateFormat.parse(scheduledDate));
                etScheduledDate.setText(dateFormat.format(scheduledCalendar.getTime()));
            } catch (Exception e) {
                etScheduledDate.setText(scheduledDate);
            }
        }
        
        String givenDate = data.optString("given_date", "");
        if (!givenDate.isEmpty() && !givenDate.equals("null")) {
            try {
                givenCalendar.setTime(dbDateFormat.parse(givenDate));
                etGivenDate.setText(dateFormat.format(givenCalendar.getTime()));
            } catch (Exception e) {
                etGivenDate.setText(givenDate);
            }
        }
        
        etBatchNumber.setText(data.optString("batch_number", ""));
        etSideEffects.setText(data.optString("side_effects", ""));
        etNotes.setText(data.optString("notes", ""));
    }

    private void saveVaccination() {
        if (!validateInput()) return;

        showLoading(true);

        // ONLINE-FIRST: Check network first
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Save to server ONLY
            saveToServerOnlineFirst();
        } else {
            // Save to local DB with sync_pending status
            saveToLocalOffline();
        }
    }

    private boolean validateInput() {
        if (etScheduledDate.getText().toString().trim().isEmpty()) {
            etScheduledDate.setError("Select scheduled date");
            etScheduledDate.requestFocus();
            return false;
        }

        String status = spinnerStatus.getSelectedItem().toString();
        if ("Given".equals(status) && etGivenDate.getText().toString().trim().isEmpty()) {
            etGivenDate.setError("Given date required for given status");
            etGivenDate.requestFocus();
            return false;
        }

        return true;
    }

    private void saveToServerOnlineFirst() {
        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", patientId);
            params.put("vaccine_name", spinnerVaccine.getSelectedItem().toString());
            params.put("scheduled_date", dbDateFormat.format(scheduledCalendar.getTime()));
            
            String status = spinnerStatus.getSelectedItem().toString();
            params.put("status", status);
            
            if ("Given".equals(status) && !etGivenDate.getText().toString().isEmpty()) {
                params.put("given_date", dbDateFormat.format(givenCalendar.getTime()));
            } else {
                params.put("given_date", "");
            }
            
            params.put("batch_number", etBatchNumber.getText().toString().trim());
            params.put("side_effects", etSideEffects.getText().toString().trim());
            params.put("notes", etNotes.getText().toString().trim());
            params.put("asha_id", sessionManager.getUserId());
            
            String url = Constants.API_BASE_URL + "vaccinations.php";
            int method = Request.Method.POST;
            
            if (vaccinationId != -1) {
                params.put("id", vaccinationId);
                method = Request.Method.PUT;
            }
            
            apiHelper.makeRequest(method, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (!success && response.has("status")) {
                                success = "success".equals(response.optString("status", ""));
                            }
                            
                            if (success) {
                                Toast.makeText(AddVaccinationActivity.this, 
                                    "Vaccination saved successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                String message = response.optString("message", "Failed to save");
                                Toast.makeText(AddVaccinationActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(AddVaccinationActivity.this, 
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(AddVaccinationActivity.this, 
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToLocalOffline() {
        // Save to local database with sync_pending status for later sync
        Vaccination vaccination = new Vaccination();
        vaccination.setPatientId(patientId);
        vaccination.setVaccineName(spinnerVaccine.getSelectedItem().toString());
        vaccination.setScheduledDate(dbDateFormat.format(scheduledCalendar.getTime()));
        
        String status = spinnerStatus.getSelectedItem().toString();
        vaccination.setStatus(status);
        
        if ("Given".equals(status) && !etGivenDate.getText().toString().isEmpty()) {
            vaccination.setGivenDate(dbDateFormat.format(givenCalendar.getTime()));
        }
        
        vaccination.setBatchNumber(etBatchNumber.getText().toString().trim());
        vaccination.setSideEffects(etSideEffects.getText().toString().trim());
        vaccination.setNotes(etNotes.getText().toString().trim());
        vaccination.setSyncStatus(Constants.SYNC_PENDING);
        
        long id = dbHelper.insertVaccination(vaccination);
        
        showLoading(false);
        
        if (id > 0) {
            Toast.makeText(this, 
                "No internet. Saved offline. Will sync when online.", 
                Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save vaccination", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
