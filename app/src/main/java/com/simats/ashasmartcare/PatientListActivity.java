package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.adapters.PatientAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientListActivity extends AppCompatActivity implements PatientAdapter.OnPatientClickListener {

    private RecyclerView recyclerView;
    private PatientAdapter adapter;
    private List<Patient> patientList;
    private List<Patient> filteredList;
    private EditText etSearch;
    private ImageView ivBack;
    private FloatingActionButton fabAdd;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView tvTotalPatients;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadPatients();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerPatients);
        etSearch = findViewById(R.id.etSearch);
        ivBack = findViewById(R.id.ivBack);
        fabAdd = findViewById(R.id.fabAddPatient);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        patientList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new PatientAdapter(this, filteredList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPatientActivity.class);
            startActivity(intent);
        });

        swipeRefresh.setOnRefreshListener(this::refreshPatients);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPatients() {
        showLoading(true);

        if (NetworkUtils.isNetworkAvailable(this)) {
            // Try to fetch from server first
            fetchPatientsFromServer();
        } else {
            // Load from local database
            loadPatientsFromLocal();
        }
    }

    private void fetchPatientsFromServer() {
        String ashaId = String.valueOf(sessionManager.getUserId());
        
        apiHelper.getPatients(ashaId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONArray patientsArray = response.getJSONArray("patients");
                        patientList.clear();
                        
                        for (int i = 0; i < patientsArray.length(); i++) {
                            JSONObject patientObj = patientsArray.getJSONObject(i);
                            Patient patient = parsePatientFromJson(patientObj);
                            patientList.add(patient);
                            
                            // Update local database
                            dbHelper.insertOrUpdatePatient(patient);
                        }
                        
                        updateUI();
                    } else {
                        loadPatientsFromLocal();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadPatientsFromLocal();
                }
                showLoading(false);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                loadPatientsFromLocal();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadPatientsFromLocal() {
        patientList.clear();
        patientList.addAll(dbHelper.getAllPatients());
        updateUI();
        showLoading(false);
    }

    private void updateUI() {
        filteredList.clear();
        filteredList.addAll(patientList);
        adapter.notifyDataSetChanged();

        tvTotalPatients.setText(String.format("Total: %d patients", patientList.size()));

        if (patientList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void filterPatients(String query) {
        filteredList.clear();
        
        if (query.isEmpty()) {
            filteredList.addAll(patientList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Patient patient : patientList) {
                if (patient.getName().toLowerCase().contains(lowerQuery) ||
                    patient.getPhone().contains(query) ||
                    patient.getVillage().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(patient);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        tvTotalPatients.setText(String.format("Showing: %d patients", filteredList.size()));
    }

    private void refreshPatients() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchPatientsFromServer();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private Patient parsePatientFromJson(JSONObject obj) throws JSONException {
        Patient patient = new Patient();
        patient.setServerId(obj.optInt("id", 0));
        patient.setName(obj.optString("name", ""));
        patient.setAge(obj.optInt("age", 0));
        patient.setGender(obj.optString("gender", ""));
        patient.setPhone(obj.optString("phone", ""));
        patient.setAddress(obj.optString("address", ""));
        patient.setVillage(obj.optString("village", ""));
        patient.setDistrict(obj.optString("district", ""));
        patient.setState(obj.optString("state", ""));
        patient.setCategory(obj.optString("category", ""));
        patient.setBloodGroup(obj.optString("blood_group", ""));
        patient.setEmergencyContact(obj.optString("emergency_contact", ""));
        patient.setMedicalHistory(obj.optString("medical_history", ""));
        patient.setAshaId(obj.optString("asha_id", ""));
        patient.setRegistrationDate(obj.optString("registration_date", ""));
        patient.setSyncStatus("SYNCED");
        return patient;
    }

    @Override
    public void onPatientClick(Patient patient) {
        Intent intent = new Intent(this, PatientProfileActivity.class);
        intent.putExtra("patient_id", patient.getId());
        startActivity(intent);
    }

    @Override
    public void onCallClick(Patient patient) {
        // Open dialer with patient's phone number
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(android.net.Uri.parse("tel:" + patient.getPhone()));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }
}
