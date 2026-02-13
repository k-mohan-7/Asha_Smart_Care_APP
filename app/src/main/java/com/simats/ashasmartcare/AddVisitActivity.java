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
import com.simats.ashasmartcare.models.Visit;
import com.simats.ashasmartcare.services.NetworkMonitorService;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.Constants;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddVisitActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvTitle;
    private Spinner spinnerVisitType;
    private EditText etVisitDate, etPurpose, etFindings, etRecommendations, etNextVisitDate, etNotes;
    private Button btnSave;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;

    private long patientId;
    private long visitId = -1;
    private Visit existingVisit;
    private Patient patient;

    private Calendar visitCalendar = Calendar.getInstance();
    private Calendar nextVisitCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final String[] VISIT_TYPES = {
            "Home Visit", "ANC Checkup", "PNC Checkup", "Child Health Checkup",
            "Vaccination", "Follow-up", "Emergency", "Counseling", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit);

        patientId = getIntent().getLongExtra("patient_id", -1);
        visitId = getIntent().getLongExtra("visit_id", -1);

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
        spinnerVisitType = findViewById(R.id.spinnerVisitType);
        etVisitDate = findViewById(R.id.etVisitDate);
        etPurpose = findViewById(R.id.etPurpose);
        etFindings = findViewById(R.id.etFindings);
        etRecommendations = findViewById(R.id.etRecommendations);
        etNextVisitDate = findViewById(R.id.etNextVisitDate);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);

        patient = dbHelper.getPatientById(patientId);

        // Set next visit to 1 week later by default
        nextVisitCalendar.add(Calendar.DAY_OF_MONTH, 7);
    }

    private void setupSpinners() {
        ArrayAdapter<String> visitTypeAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, VISIT_TYPES);
        visitTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerVisitType.setAdapter(visitTypeAdapter);
    }

    private void setupDatePickers() {
        etVisitDate.setOnClickListener(v -> showVisitDatePicker());
        etNextVisitDate.setOnClickListener(v -> showNextVisitDatePicker());
    }

    private void showVisitDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    visitCalendar.set(year, month, dayOfMonth);
                    etVisitDate.setText(dateFormat.format(visitCalendar.getTime()));
                },
                visitCalendar.get(Calendar.YEAR),
                visitCalendar.get(Calendar.MONTH),
                visitCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showNextVisitDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    nextVisitCalendar.set(year, month, dayOfMonth);
                    etNextVisitDate.setText(dateFormat.format(nextVisitCalendar.getTime()));
                },
                nextVisitCalendar.get(Calendar.YEAR),
                nextVisitCalendar.get(Calendar.MONTH),
                nextVisitCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveVisit());
    }

    private void loadData() {
        etVisitDate.setText(dateFormat.format(visitCalendar.getTime()));
        etNextVisitDate.setText(dateFormat.format(nextVisitCalendar.getTime()));

        if (visitId != -1) {
            tvTitle.setText("Edit Visit");
            existingVisit = dbHelper.getVisitById(visitId);
            if (existingVisit != null) {
                populateForm(existingVisit);
            }
        } else {
            tvTitle.setText("Add Visit");
        }
    }

    private void populateForm(Visit v) {
        // Set visit type
        for (int i = 0; i < VISIT_TYPES.length; i++) {
            if (VISIT_TYPES[i].equals(v.getVisitType())) {
                spinnerVisitType.setSelection(i);
                break;
            }
        }

        if (v.getVisitDate() != null && !v.getVisitDate().isEmpty()) {
            try {
                visitCalendar.setTime(dbDateFormat.parse(v.getVisitDate()));
                etVisitDate.setText(dateFormat.format(visitCalendar.getTime()));
            } catch (Exception e) {
                etVisitDate.setText(v.getVisitDate());
            }
        }

        if (v.getNextVisitDate() != null && !v.getNextVisitDate().isEmpty()) {
            try {
                nextVisitCalendar.setTime(dbDateFormat.parse(v.getNextVisitDate()));
                etNextVisitDate.setText(dateFormat.format(nextVisitCalendar.getTime()));
            } catch (Exception e) {
                etNextVisitDate.setText(v.getNextVisitDate());
            }
        }

        etPurpose.setText(v.getPurpose());
        etFindings.setText(v.getFindings());
        etRecommendations.setText(v.getRecommendations());
        etNotes.setText(v.getNotes());
    }

    private void saveVisit() {
        if (!validateInput()) return;

        showLoading(true);

        Visit visit = new Visit();
        if (existingVisit != null) {
            visit.setId(existingVisit.getId());
            visit.setServerId(existingVisit.getServerId());
        }

        visit.setPatientId(patientId);
        visit.setVisitType(spinnerVisitType.getSelectedItem().toString());
        visit.setVisitDate(dbDateFormat.format(visitCalendar.getTime()));
        visit.setPurpose(etPurpose.getText().toString().trim());
        visit.setFindings(etFindings.getText().toString().trim());
        visit.setRecommendations(etRecommendations.getText().toString().trim());
        visit.setNextVisitDate(dbDateFormat.format(nextVisitCalendar.getTime()));
        visit.setNotes(etNotes.getText().toString().trim());

        if (NetworkMonitorService.isNetworkConnected(this)) {
            // ONLINE: Direct backend POST only (NO local database)
            visit.setSyncStatus(Constants.SYNC_SYNCED);
            saveToBackendOnly(visit);
        } else {
            // OFFLINE: Save to local database + sync queue
            visit.setSyncStatus(Constants.SYNC_PENDING);
            saveLocally(visit, Constants.SYNC_PENDING);
        }
    }

    private boolean validateInput() {
        if (etVisitDate.getText().toString().trim().isEmpty()) {
            etVisitDate.setError("Select visit date");
            etVisitDate.requestFocus();
            return false;
        }

        if (etPurpose.getText().toString().trim().isEmpty()) {
            etPurpose.setError("Enter visit purpose");
            etPurpose.requestFocus();
            return false;
        }

        return true;
    }

    private void saveToBackendOnly(Visit visit) {
        showLoading(true);
        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", visit.getPatientId());
            params.put("visit_type", visit.getVisitType());
            params.put("visit_date", visit.getVisitDate());
            params.put("purpose", visit.getPurpose());
            params.put("findings", visit.getFindings());
            params.put("recommendations", visit.getRecommendations());
            params.put("next_visit_date", visit.getNextVisitDate());
            params.put("notes", visit.getNotes());
            params.put("asha_id", sessionManager.getUserId());

            String url = Constants.API_BASE_URL + "visits.php";
            int method = Request.Method.POST;

            if (visit.getServerId() > 0) {
                params.put("id", visit.getServerId());
                method = Request.Method.PUT;
            }

            apiHelper.makeRequest(method, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    showLoading(false);
                    runOnUiThread(() -> {
                        Toast.makeText(AddVisitActivity.this, "✓ Visit saved successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    runOnUiThread(() -> {
                        Toast.makeText(AddVisitActivity.this, "Network error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToServer(Visit visit) {
        try {
            JSONObject params = new JSONObject();
            params.put("patient_id", visit.getPatientId());
            params.put("visit_type", visit.getVisitType());
            params.put("visit_date", visit.getVisitDate());
            params.put("purpose", visit.getPurpose());
            params.put("findings", visit.getFindings());
            params.put("recommendations", visit.getRecommendations());
            params.put("next_visit_date", visit.getNextVisitDate());
            params.put("notes", visit.getNotes());
            params.put("asha_id", sessionManager.getUserId());

            String url = Constants.API_BASE_URL + "visits.php";
            int method = Request.Method.POST;

            if (visit.getServerId() > 0) {
                params.put("id", visit.getServerId());
                method = Request.Method.PUT;
            }

            apiHelper.makeRequest(method, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("id")) {
                            visit.setServerId((int) response.getLong("id"));
                        }
                        saveLocally(visit, Constants.SYNC_SYNCED);
                    } catch (Exception e) {
                        saveLocally(visit, Constants.SYNC_SYNCED);
                    }
                }

                @Override
                public void onError(String error) {
                    saveLocally(visit, Constants.SYNC_PENDING);
                }
            });
        } catch (Exception e) {
            saveLocally(visit, Constants.SYNC_PENDING);
        }
    }

    private void saveLocally(Visit visit, String syncStatus) {
        visit.setSyncStatus(syncStatus);

        long id;
        if (visit.getId() > 0) {
            dbHelper.updateVisit(visit);
            id = visit.getId();
        } else {
            id = dbHelper.insertVisit(visit);
        }

        showLoading(false);

        if (id > 0) {
            String message = Constants.SYNC_PENDING.equals(syncStatus) ?
                    "Saved offline. Will sync when online." : "Visit saved successfully!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save visit", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
