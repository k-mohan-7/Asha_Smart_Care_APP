package com.simats.ashasmartcare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.adapters.HighRiskAlertAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.HighRiskAlert;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import com.android.volley.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AIInsightsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvAnalysisText;
    private RecyclerView rvAlerts;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;
    private HighRiskAlertAdapter alertAdapter;
    private List<HighRiskAlert> alertList;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_insights);

        initViews();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvAnalysisText = findViewById(R.id.tvAnalysisText);
        rvAlerts = findViewById(R.id.rvAlerts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        alertList = new ArrayList<>();

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        alertAdapter = new HighRiskAlertAdapter(this, alertList);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvAlerts.setAdapter(alertAdapter);
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        alertList.clear();

        if (NetworkUtils.isNetworkAvailable(this)) {
            // ONLINE: Fetch insights from backend
            fetchInsightsFromBackend();
        } else {
            // OFFLINE: No data available
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "⚠️ No internet connection. Cannot load insights.", Toast.LENGTH_LONG).show();
            tvAnalysisText.setText("Analyzing 0 patient records. Found 0 priority alerts.");
            alertAdapter.notifyDataSetChanged();
        }
    }

    private void fetchInsightsFromBackend() {
        String ashaId = String.valueOf(sessionManager.getUserId());

        // Use GET instead of POST since backend POST has issues
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
                        alertList.clear();
                        int totalPatients = patientsArray.length();

                        // Analyze high-risk patients
                        for (int i = 0; i < patientsArray.length(); i++) {
                            JSONObject patientObj = patientsArray.getJSONObject(i);
                            if (patientObj.optInt("is_high_risk", 0) == 1) {
                                String reason = patientObj.optString("high_risk_reason", "High Risk Patient Flagged");
                                if (reason.length() > 30 && reason.contains(",")) {
                                    reason = reason.split(",")[0] + "...";
                                }

                                HighRiskAlert alert = new HighRiskAlert(
                                        patientObj.getInt("id"),
                                        patientObj.getString("name"),
                                        patientObj.optString("area", "Unknown Village"),
                                        reason);

                                // Check if this alert was already reviewed locally
                                alert.setReviewed(dbHelper.isAlertReviewed(alert.getPatientId(), alert.getAlertType()));

                                alertList.add(alert);
                            }
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvAnalysisText.setText("Analyzing " + totalPatients + " patient records. Found "
                                    + alertList.size() + " priority alerts.");
                            alertAdapter.notifyDataSetChanged();
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AIInsightsActivity.this, "Failed to load insights", Toast.LENGTH_SHORT)
                                    .show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AIInsightsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AIInsightsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    tvAnalysisText.setText("Analyzing 0 patient records. Found 0 priority alerts.");
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
