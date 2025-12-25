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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.adapters.VisitAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.Visit;

import java.util.ArrayList;
import java.util.List;

public class VisitHistoryActivity extends AppCompatActivity implements VisitAdapter.OnVisitClickListener {

    private ImageView ivBack;
    private TextView tvPatientName, tvTotalVisits;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;

    private VisitAdapter adapter;
    private List<Visit> visitList;

    private DatabaseHelper dbHelper;
    private long patientId;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_history);

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
        tvPatientName = findViewById(R.id.tvPatientName);
        tvTotalVisits = findViewById(R.id.tvTotalVisits);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        dbHelper = DatabaseHelper.getInstance(this);
        visitList = new ArrayList<>();

        patient = dbHelper.getPatientById(patientId);
        if (patient != null) {
            tvPatientName.setText(patient.getName());
        }
    }

    private void setupRecyclerView() {
        adapter = new VisitAdapter(this, visitList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVisitActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        showLoading(true);
        visitList.clear();
        visitList.addAll(dbHelper.getVisitsByPatientId(patientId));
        updateUI();
        showLoading(false);
        swipeRefresh.setRefreshing(false);
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        tvTotalVisits.setText(visitList.size() + " total visits");

        if (visitList.isEmpty()) {
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
    public void onVisitClick(Visit visit) {
        Intent intent = new Intent(this, AddVisitActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("visit_id", visit.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
