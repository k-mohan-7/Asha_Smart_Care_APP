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
import com.simats.ashasmartcare.adapters.ChildGrowthAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.ChildGrowth;
import com.simats.ashasmartcare.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class ChildGrowthListActivity extends AppCompatActivity implements ChildGrowthAdapter.OnGrowthClickListener {

    private ImageView ivBack;
    private TextView tvPatientName, tvSubtitle;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;

    private ChildGrowthAdapter adapter;
    private List<ChildGrowth> growthList;

    private DatabaseHelper dbHelper;
    private long patientId;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_growth_list);

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
        tvSubtitle = findViewById(R.id.tvSubtitle);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        dbHelper = DatabaseHelper.getInstance(this);
        growthList = new ArrayList<>();

        patient = dbHelper.getPatientById(patientId);
        if (patient != null) {
            tvPatientName.setText(patient.getName());
        }
    }

    private void setupRecyclerView() {
        adapter = new ChildGrowthAdapter(this, growthList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddChildGrowthActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        showLoading(true);
        growthList.clear();
        growthList.addAll(dbHelper.getChildGrowthByPatient(patientId));
        updateUI();
        showLoading(false);
        swipeRefresh.setRefreshing(false);
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        tvSubtitle.setText(growthList.size() + " record(s)");

        if (growthList.isEmpty()) {
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
    public void onGrowthClick(ChildGrowth growth) {
        Intent intent = new Intent(this, AddChildGrowthActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("growth_id", growth.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
