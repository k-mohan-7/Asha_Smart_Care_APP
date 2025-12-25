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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.adapters.PregnancyAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.PregnancyVisit;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PregnancyListActivity extends AppCompatActivity implements PregnancyAdapter.OnPregnancyClickListener {

    private ImageView ivBack;
    private TextView tvTitle, tvPatientName, tvSubtitle;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;

    private PregnancyAdapter adapter;
    private List<PregnancyVisit> pregnancyList;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private long patientId;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy_list);

        patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        pregnancyList = new ArrayList<>();

        patient = dbHelper.getPatientById(patientId);
        if (patient != null) {
            tvPatientName.setText(patient.getName());
        }
    }

    private void setupRecyclerView() {
        adapter = new PregnancyAdapter(this, pregnancyList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPregnancyVisitActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        showLoading(true);

        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchFromServer();
        } else {
            loadFromLocal();
        }
    }

    private void fetchFromServer() {
        apiHelper.getPregnancyVisits(patient.getServerId(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONArray visitsArray = response.getJSONArray("visits");
                        pregnancyList.clear();

                        for (int i = 0; i < visitsArray.length(); i++) {
                            JSONObject obj = visitsArray.getJSONObject(i);
                            PregnancyVisit visit = parseVisitFromJson(obj);
                            pregnancyList.add(visit);
                            dbHelper.insertOrUpdatePregnancyVisit(visit);
                        }

                        updateUI();
                    } else {
                        loadFromLocal();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadFromLocal();
                }
                showLoading(false);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                loadFromLocal();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadFromLocal() {
        pregnancyList.clear();
        pregnancyList.addAll(dbHelper.getPregnancyVisitsByPatientId(patientId));
        updateUI();
        showLoading(false);
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        tvSubtitle.setText(pregnancyList.size() + " visit(s) recorded");

        if (pregnancyList.isEmpty()) {
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

    private PregnancyVisit parseVisitFromJson(JSONObject obj) throws JSONException {
        PregnancyVisit visit = new PregnancyVisit();
        visit.setServerId(obj.optString("id", ""));
        visit.setPatientId(patientId);
        visit.setPatientServerId(obj.optString("patient_id", ""));
        visit.setVisitDate(obj.optString("visit_date", ""));
        visit.setWeeksPregnant(obj.optInt("weeks_pregnant", 0));
        visit.setWeight(obj.optDouble("weight", 0));
        visit.setBloodPressureSystolic(obj.optInt("bp_systolic", 0));
        visit.setBloodPressureDiastolic(obj.optInt("bp_diastolic", 0));
        visit.setHemoglobin(obj.optDouble("hemoglobin", 0));
        visit.setFetalHeartRate(obj.optInt("fetal_heart_rate", 0));
        visit.setFundalHeight(obj.optDouble("fundal_height", 0));
        visit.setUrineSugar(obj.optString("urine_sugar", ""));
        visit.setUrineAlbumin(obj.optString("urine_albumin", ""));
        visit.setComplaints(obj.optString("complaints", ""));
        visit.setAdvice(obj.optString("advice", ""));
        visit.setNextVisitDate(obj.optString("next_visit_date", ""));
        visit.setHighRisk(obj.optBoolean("high_risk", false));
        visit.setRiskFactors(obj.optString("risk_factors", ""));
        visit.setNotes(obj.optString("notes", ""));
        visit.setSyncStatus("SYNCED");
        return visit;
    }

    @Override
    public void onPregnancyClick(PregnancyVisit visit) {
        Intent intent = new Intent(this, PregnancyDetailActivity.class);
        intent.putExtra("visit_id", visit.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
