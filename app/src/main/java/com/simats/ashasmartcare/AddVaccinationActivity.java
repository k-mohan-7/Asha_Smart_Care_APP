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

        patientId = getIntent().getLongExtra("patient_id", -1);
        vaccinationId = getIntent().getLongExtra("vaccination_id", -1);

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

        patient = dbHelper.getPatientById(patientId);
    }

    private void setupSpinners() {
        ArrayAdapter<String> vaccineAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, VACCINES);
        vaccineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVaccine.setAdapter(vaccineAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, STATUS);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
            existingVaccination = dbHelper.getVaccinationById(vaccinationId);
            if (existingVaccination != null) {
                populateForm(existingVaccination);
            }
        } else {
            tvTitle.setText("Add Vaccination");
            etScheduledDate.setText(dateFormat.format(scheduledCalendar.getTime()));
        }
    }

    private void populateForm(Vaccination v) {
        // Set vaccine
        for (int i = 0; i < VACCINES.length; i++) {
            if (VACCINES[i].equals(v.getVaccineName())) {
                spinnerVaccine.setSelection(i);
                break;
            }
        }

        // Set status
        for (int i = 0; i < STATUS.length; i++) {
            if (STATUS[i].equals(v.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        if (v.getScheduledDate() != null && !v.getScheduledDate().isEmpty()) {
            try {
                scheduledCalendar.setTime(dbDateFormat.parse(v.getScheduledDate()));
                etScheduledDate.setText(dateFormat.format(scheduledCalendar.getTime()));
            } catch (Exception e) {
                etScheduledDate.setText(v.getScheduledDate());
            }
        }

        if (v.getGivenDate() != null && !v.getGivenDate().isEmpty()) {
            try {
                givenCalendar.setTime(dbDateFormat.parse(v.getGivenDate()));
                etGivenDate.setText(dateFormat.format(givenCalendar.getTime()));
            } catch (Exception e) {
                etGivenDate.setText(v.getGivenDate());
            }
        }

        etBatchNumber.setText(v.getBatchNumber());
        etSideEffects.setText(v.getSideEffects());
        etNotes.setText(v.getNotes());
    }

    private void saveVaccination() {
        if (!validateInput()) return;

        showLoading(true);

        Vaccination vaccination = new Vaccination();
        if (existingVaccination != null) {
            vaccination.setId(existingVaccination.getId());
            vaccination.setServerId(existingVaccination.getServerId());
        }

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

        if (NetworkUtils.isNetworkAvailable(this)) {
            saveToServer(vaccination);
        } else {
            saveLocally(vaccination, Constants.SYNC_PENDING);
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

    private void saveToServer(Vaccination vaccination) {
        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", vaccination.getPatientId());
            params.put("vaccine_name", vaccination.getVaccineName());
            params.put("scheduled_date", vaccination.getScheduledDate());
            params.put("given_date", vaccination.getGivenDate() != null ? vaccination.getGivenDate() : "");
            params.put("status", vaccination.getStatus());
            params.put("batch_number", vaccination.getBatchNumber());
            params.put("side_effects", vaccination.getSideEffects());
            params.put("notes", vaccination.getNotes());
            params.put("asha_id", sessionManager.getUserId());

            String url = Constants.API_BASE_URL + "vaccinations.php";
            int method = Request.Method.POST;

            if (vaccination.getServerId() > 0) {
                params.put("id", vaccination.getServerId());
                method = Request.Method.PUT;
            }

            apiHelper.makeRequest(method, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            vaccination.setServerId((int) response.getLong("id"));
                        }
                        saveLocally(vaccination, Constants.SYNC_SYNCED);
                    } catch (Exception e) {
                        saveLocally(vaccination, Constants.SYNC_SYNCED);
                    }
                }

                @Override
                public void onError(String error) {
                    saveLocally(vaccination, Constants.SYNC_PENDING);
                }
            });
        } catch (Exception e) {
            saveLocally(vaccination, Constants.SYNC_PENDING);
        }
    }

    private void saveLocally(Vaccination vaccination, String syncStatus) {
        vaccination.setSyncStatus(syncStatus);

        long id;
        if (vaccination.getId() > 0) {
            dbHelper.updateVaccination(vaccination);
            id = vaccination.getId();
        } else {
            id = dbHelper.insertVaccination(vaccination);
        }

        showLoading(false);

        if (id > 0) {
            String message = Constants.SYNC_PENDING.equals(syncStatus) ?
                    "Saved offline. Will sync when online." : "Vaccination saved successfully!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
