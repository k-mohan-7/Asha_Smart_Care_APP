package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.simats.ashasmartcare.adapters.VaccinationAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.Vaccination;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VaccinationListActivity extends AppCompatActivity
        implements VaccinationAdapter.OnVaccinationClickListener {

    private ImageView ivBack;
    private Chip chipAll, chipOverdue, chipDueSoon;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList;
    private List<Vaccination> allVaccinations;
    private String currentFilter = "All";

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddVaccination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination_list);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        chipAll = findViewById(R.id.chipAll);
        chipOverdue = findViewById(R.id.chipOverdue);
        chipDueSoon = findViewById(R.id.chipDueSoon);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddVaccination = findViewById(R.id.fabAddVaccination);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        vaccinationList = new ArrayList<>();
        allVaccinations = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new VaccinationAdapter(this, vaccinationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        chipAll.setOnClickListener(v -> {
            currentFilter = "All";
            filterVaccinations();
        });

        chipOverdue.setOnClickListener(v -> {
            currentFilter = "Overdue";
            filterVaccinations();
        });

        chipDueSoon.setOnClickListener(v -> {
            currentFilter = "Due Soon";
            filterVaccinations();
        });

        swipeRefresh.setOnRefreshListener(this::loadData);

        fabAddVaccination.setOnClickListener(v -> showPatientSelectionDialog());
    }

    private void showPatientSelectionDialog() {
        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "⚠️ No internet connection. Cannot load patients.", Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_patient, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        android.widget.EditText etSearch = dialogView.findViewById(R.id.etSearchPatient);
        RecyclerView rvPatients = dialogView.findViewById(R.id.rvPatients);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBarDialog);
        android.widget.TextView tvEmpty = dialogView.findViewById(R.id.tvEmptyDialog);

        List<Patient> allPatients = new ArrayList<>();
        List<Patient> displayPatients = new ArrayList<>();

        com.simats.ashasmartcare.adapters.PatientsAdapter patientAdapter = new com.simats.ashasmartcare.adapters.PatientsAdapter(
                this, displayPatients, patient -> {
                    Intent intent = new Intent(this, AddVaccinationActivity.class);
                    // Use server ID instead of local ID
                    intent.putExtra("patient_id", patient.getServerId());
                    startActivity(intent);
                    dialog.dismiss();
                });

        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        rvPatients.setAdapter(patientAdapter);

        // ONLINE-FIRST: Load patients from backend API
        progressBar.setVisibility(View.VISIBLE);
        rvPatients.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        String ashaId = String.valueOf(sessionManager.getUserId());
        apiHelper.getPatients(ashaId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    boolean isSuccess = (response.optBoolean("success", false) ||
                            "success".equals(response.optString("status", "")));

                    if (isSuccess) {
                        JSONArray patientsArray = response.getJSONArray("patients");
                        allPatients.clear();
                        displayPatients.clear();

                        for (int i = 0; i < patientsArray.length(); i++) {
                            JSONObject patientObj = patientsArray.getJSONObject(i);
                            Patient patient = parsePatientFromJson(patientObj);
                            allPatients.add(patient);
                            displayPatients.add(patient);
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (displayPatients.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rvPatients.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                rvPatients.setVisibility(View.VISIBLE);
                                patientAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("Failed to load patients");
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Error: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Network error: " + error);
                });
            }
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                displayPatients.clear();
                for (Patient p : allPatients) {
                    if (p.getName().toLowerCase().contains(query)) {
                        displayPatients.add(p);
                    }
                }
                patientAdapter.notifyDataSetChanged();

                if (displayPatients.isEmpty() && !allPatients.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No patients found");
                    rvPatients.setVisibility(View.GONE);
                } else if (!displayPatients.isEmpty()) {
                    tvEmpty.setVisibility(View.GONE);
                    rvPatients.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        dialog.show();
    }

    private Patient parsePatientFromJson(JSONObject obj) throws JSONException {
        Patient patient = new Patient();
        patient.setServerId(obj.optInt("id", 0));
        patient.setName(obj.optString("name", ""));
        patient.setAge(obj.optInt("age", 0));
        patient.setGender(obj.optString("gender", ""));
        patient.setPhone(obj.optString("phone", ""));
        patient.setAddress(obj.optString("address", ""));
        patient.setCategory(obj.optString("category", ""));
        patient.setBloodGroup(obj.optString("blood_group", ""));
        patient.setMedicalHistory(obj.optString("medical_history", ""));
        patient.setAshaId(obj.optString("asha_id", ""));
        patient.setRegistrationDate(obj.optString("registration_date", ""));
        patient.setSyncStatus("SYNCED");
        return patient;
    }

    private void loadData() {
        showLoading(true);

        if (NetworkUtils.isNetworkAvailable(this)) {
            // ONLINE: Fetch from backend API
            fetchVaccinationsFromBackend();
        } else {
            // OFFLINE: Show no internet message
            showLoading(false);
            swipeRefresh.setRefreshing(false);
            Toast.makeText(this, "⚠️ No internet connection. Cannot load vaccinations.", Toast.LENGTH_LONG).show();
            allVaccinations.clear();
            updateUI();
        }
    }

    private void fetchVaccinationsFromBackend() {
        String ashaId = String.valueOf(sessionManager.getUserId());

        apiHelper.makeGetRequest("vaccinations.php?asha_id=" + ashaId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONArray vaccsArray = response.getJSONArray("data");
                        allVaccinations.clear();

                        for (int i = 0; i < vaccsArray.length(); i++) {
                            JSONObject vaccObj = vaccsArray.getJSONObject(i);
                            Vaccination vacc = new Vaccination();
                            vacc.setServerId(vaccObj.getInt("id"));
                            vacc.setPatientId(vaccObj.getInt("patient_id"));
                            vacc.setVaccineName(vaccObj.getString("vaccine_name"));
                            vacc.setDueDate(vaccObj.getString("scheduled_date")); // Backend returns 'scheduled_date'
                            vacc.setGivenDate(vaccObj.optString("given_date", ""));
                            vacc.setStatus(vaccObj.getString("status"));
                            vacc.setBatchNumber(vaccObj.optString("batch_number", ""));
                            vacc.setNotes(vaccObj.optString("notes", ""));
                            vacc.setPatientName(vaccObj.optString("patient_name", ""));
                            allVaccinations.add(vacc);
                        }

                        runOnUiThread(() -> {
                            filterVaccinations(); // Filter data into vaccinationList based on current filter
                            showLoading(false);
                            swipeRefresh.setRefreshing(false);
                        });
                    } else {
                        runOnUiThread(() -> {
                            showLoading(false);
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(VaccinationListActivity.this, "Failed to load vaccinations",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(VaccinationListActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(VaccinationListActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadDataOLD_REMOVE() {
        showLoading(true);

        // Sync any missing vaccinations from notes first
        dbHelper.syncMissingVaccinations();

        allVaccinations.clear();

        // Load all vaccinations for all patients
        List<Vaccination> allVaccs = dbHelper.getAllVaccinations();
        for (Vaccination vacc : allVaccs) {
            // Get patient name for each vaccination
            Patient patient = dbHelper.getPatientById(vacc.getPatientId());
            if (patient != null) {
                vacc.setPatientName(patient.getName());
            }
            allVaccinations.add(vacc);
        }

        filterVaccinations();
        showLoading(false);
        swipeRefresh.setRefreshing(false);
    }

    private void filterVaccinations() {
        vaccinationList.clear();

        for (Vaccination vacc : allVaccinations) {
            String status = calculateVaccinationStatus(vacc.getDueDate());

            if (currentFilter.equals("All")) {
                vaccinationList.add(vacc);
            } else if (currentFilter.equals("Overdue") && status.equals("overdue")) {
                vaccinationList.add(vacc);
            } else if (currentFilter.equals("Due Soon") && status.equals("due soon")) {
                vaccinationList.add(vacc);
            }
        }

        updateUI();
    }

    private String calculateVaccinationStatus(String dueDate) {
        if (dueDate == null || dueDate.isEmpty()) {
            return "upcoming";
        }

        try {
            Date due = dateFormat.parse(dueDate);
            Date today = new Date();

            long diff = due.getTime() - today.getTime();
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            if (daysDiff < 0) {
                return "overdue";
            } else if (daysDiff <= 7) {
                return "due soon";
            } else {
                return "upcoming";
            }
        } catch (ParseException e) {
            return "upcoming";
        }
    }

    private void updateUI() {
        adapter.updateList(vaccinationList);

        if (vaccinationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onVaccinationClick(Vaccination vaccination) {
        // Open patient vaccination detail screen
        Intent intent = new Intent(this, PatientVaccinationDetailActivity.class);
        intent.putExtra("patient_id", (int) vaccination.getPatientId()); // Cast to int
        intent.putExtra("patient_name", vaccination.getPatientName());
        startActivity(intent);
    }

    @Override
    public void onMarkDoneClick(Vaccination vaccination) {
        // ONLINE-FIRST: Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "⚠️ No internet connection. Status update requires network.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Show loading
        showLoading(true);

        // Prepare updated status and given date
        String newStatus = "Given";
        String givenDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // ONLINE-FIRST: Update vaccination status via backend API
        String url = sessionManager.getApiBaseUrl() + "vaccinations.php";
        JSONObject params = new JSONObject();

        try {
            params.put("action", "update_status");
            params.put("vaccination_id", vaccination.getServerId());
            params.put("status", newStatus);
            params.put("given_date", givenDate);

            apiHelper.makeRequest(com.android.volley.Request.Method.POST, url, params, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        try {
                            boolean isSuccess = (response.optBoolean("success", false) ||
                                    "success".equals(response.optString("status", "")));

                            if (isSuccess) {
                                Toast.makeText(VaccinationListActivity.this,
                                        "✓ Vaccination marked as given", Toast.LENGTH_SHORT).show();

                                // Reload data from API to reflect changes
                                loadData();
                            } else {
                                String errorMsg = response.optString("message", "Failed to update vaccination status");
                                Toast.makeText(VaccinationListActivity.this,
                                        "⚠️ " + errorMsg, Toast.LENGTH_LONG).show();
                                showLoading(false);
                            }
                        } catch (Exception e) {
                            Toast.makeText(VaccinationListActivity.this,
                                    "⚠️ Error processing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            showLoading(false);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(VaccinationListActivity.this,
                                "⚠️ Network error: " + error, Toast.LENGTH_LONG).show();
                        showLoading(false);
                    });
                }
            });

        } catch (JSONException e) {
            Toast.makeText(this, "⚠️ Error preparing request: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showLoading(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
