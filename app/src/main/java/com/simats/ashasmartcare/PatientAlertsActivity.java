package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.activities.BaseActivity;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.adapters.PatientAdapter;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import com.android.volley.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientAlertsActivity extends BaseActivity implements PatientAdapter.OnPatientClickListener {

    @Override
    protected int getNavItemId() {
        return R.id.nav_alerts;
    }

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvAlertCount;

    private PatientAdapter adapter;
    private List<Patient> highRiskPatients;
    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_alerts);

        initViews();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        tvAlertCount = findViewById(R.id.tvAlertCount);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        highRiskPatients = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new PatientAdapter(this, highRiskPatients, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        highRiskPatients.clear();

        if (NetworkUtils.isNetworkAvailable(this)) {
            // ONLINE: Fetch from backend API
            fetchHighRiskPatientsFromBackend();
        } else {
            // OFFLINE: No data available
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "⚠️ No internet connection. Cannot load alerts.", Toast.LENGTH_LONG).show();
            tvAlertCount.setText("0 high-risk patients");
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void fetchHighRiskPatientsFromBackend() {
        String ashaId = String.valueOf(sessionManager.getUserId());

        // Use GET instead of POST since backend POST has issues
        // Backend will return all patients, we filter for high-risk on client side
        apiHelper.makeGetRequest("patients.php?asha_id=" + ashaId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Backend returns either "success":true or "status":"success"
                    boolean isSuccess = (response.optBoolean("success", false) ||
                            "success".equals(response.optString("status", "")));

                    if (isSuccess) {
                        // Backend returns either "data" or "patients" array
                        JSONArray patientsArray = response.has("patients") ? response.getJSONArray("patients")
                                : response.getJSONArray("data");
                        highRiskPatients.clear();

                        for (int i = 0; i < patientsArray.length(); i++) {
                            JSONObject patientObj = patientsArray.getJSONObject(i);
                            if (patientObj.optInt("is_high_risk", 0) == 1) {
                                Patient patient = new Patient();
                                patient.setServerId(patientObj.getInt("id"));
                                patient.setName(patientObj.getString("name"));
                                patient.setAge(patientObj.getInt("age"));
                                patient.setGender(patientObj.getString("gender"));
                                patient.setCategory(patientObj.optString("category", "General"));
                                patient.setAddress(patientObj.optString("address", ""));
                                patient.setPhone(patientObj.optString("phone", ""));
                                patient.setHighRisk(true);
                                patient.setHighRiskReason(patientObj.optString("high_risk_reason", ""));
                                patient.setSyncStatus("SYNCED"); // These are fetched from backend, so they are synced
                                highRiskPatients.add(patient);
                            }
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvAlertCount.setText(highRiskPatients.size() + " high-risk patients");
                            adapter.notifyDataSetChanged();

                            if (highRiskPatients.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                layoutEmpty.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                layoutEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PatientAlertsActivity.this, "Failed to load alerts", Toast.LENGTH_SHORT)
                                    .show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PatientAlertsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PatientAlertsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    tvAlertCount.setText("0 high-risk patients");
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    public void onPatientClick(Patient patient) {
        Intent intent = new Intent(this, PatientProfileActivity.class);
        intent.putExtra("patient_id", patient.getServerId());
        startActivity(intent);
    }

    @Override
    public void onCallClick(Patient patient) {
        String phone = patient.getPhone();
        if (phone != null && !phone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phone));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
