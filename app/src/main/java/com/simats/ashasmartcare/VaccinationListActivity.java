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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VaccinationListActivity extends AppCompatActivity implements VaccinationAdapter.OnVaccinationClickListener {

    private ImageView ivBack;
    private Chip chipAll, chipOverdue, chipDueSoon, chipUpcoming;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList;
    private List<Vaccination> allVaccinations;
    private String currentFilter = "All";

    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
        chipUpcoming = findViewById(R.id.chipUpcoming);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        dbHelper = DatabaseHelper.getInstance(this);
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

        chipUpcoming.setOnClickListener(v -> {
            currentFilter = "Upcoming";
            filterVaccinations();
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        showLoading(true);
        allVaccinations.clear();
        
        // Load all vaccinations for all patients (exclude completed ones)
        List<Vaccination> allVaccs = dbHelper.getAllVaccinations();
        for (Vaccination vacc : allVaccs) {
            if (!"COMPLETED".equals(vacc.getStatus()) && !"Given".equals(vacc.getStatus())) {
                // Get patient name for each vaccination
                Patient patient = dbHelper.getPatientById(vacc.getPatientId());
                if (patient != null) {
                    vacc.setPatientName(patient.getName());
                }
                allVaccinations.add(vacc);
            }
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
            } else if (currentFilter.equals("Upcoming") && status.equals("upcoming")) {
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
        intent.putExtra("patient_id", vaccination.getPatientId());
        startActivity(intent);
    }

    @Override
    public void onMarkDoneClick(Vaccination vaccination) {
        // Mark vaccination as completed
        vaccination.setStatus("COMPLETED");
        vaccination.setGivenDate(dateFormat.format(new Date()));
        
        // Update in database
        dbHelper.updateVaccination(vaccination);
        
        // Add to sync queue
        dbHelper.addToSyncQueue("vaccinations", vaccination.getLocalId(), "UPDATE");
        
        Toast.makeText(this, "Vaccination marked as done", Toast.LENGTH_SHORT).show();
        
        // Reload data
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
