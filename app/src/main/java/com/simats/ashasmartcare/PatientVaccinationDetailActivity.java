package com.simats.ashasmartcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.Vaccination;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientVaccinationDetailActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvPatientName;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private VaccinationHistoryAdapter adapter;
    private List<Vaccination> allVaccinations;
    private List<Vaccination> filteredVaccinations;

    private DatabaseHelper dbHelper;
    private long patientId;
    private Patient patient;
    private String currentTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_vaccination_detail);

        patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvPatientName = findViewById(R.id.tv_patient_name);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        dbHelper = DatabaseHelper.getInstance(this);
        allVaccinations = new ArrayList<>();
        filteredVaccinations = new ArrayList<>();

        patient = dbHelper.getPatientById(patientId);
        if (patient != null) {
            tvPatientName.setText(patient.getName());
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
    }

    private void setupRecyclerView() {
        adapter = new VaccinationHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getText().toString();
                filterVaccinations();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        showLoading(true);
        allVaccinations.clear();
        allVaccinations.addAll(dbHelper.getVaccinationsByPatientId(patientId));
        filterVaccinations();
        showLoading(false);
        swipeRefresh.setRefreshing(false);
    }

    private void filterVaccinations() {
        filteredVaccinations.clear();

        for (Vaccination vacc : allVaccinations) {
            boolean isCompleted = "COMPLETED".equals(vacc.getStatus()) || "Given".equals(vacc.getStatus());
            
            if (currentTab.equals("All")) {
                filteredVaccinations.add(vacc);
            } else if (currentTab.equals("Completed") && isCompleted) {
                filteredVaccinations.add(vacc);
            } else if (currentTab.equals("Pending") && !isCompleted) {
                filteredVaccinations.add(vacc);
            }
        }

        adapter.updateList(filteredVaccinations);
        updateUI();
    }

    private void updateUI() {
        if (filteredVaccinations.isEmpty()) {
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

    // Inner adapter class for vaccination history
    private class VaccinationHistoryAdapter extends RecyclerView.Adapter<VaccinationHistoryAdapter.ViewHolder> {

        private List<Vaccination> vaccinations = new ArrayList<>();
        private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public void updateList(List<Vaccination> newList) {
            this.vaccinations = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vaccination_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Vaccination vaccination = vaccinations.get(position);

            holder.tvVaccineName.setText(vaccination.getVaccineName());

            // Due date
            String dueDate = vaccination.getDueDate();
            if (dueDate != null && !dueDate.isEmpty()) {
                try {
                    Date date = inputFormat.parse(dueDate);
                    holder.tvDueDate.setText("Due: " + outputFormat.format(date));
                } catch (Exception e) {
                    holder.tvDueDate.setText("Due: " + dueDate);
                }
            }

            // Given date
            String givenDate = vaccination.getGivenDate();
            boolean isCompleted = "COMPLETED".equals(vaccination.getStatus()) || "Given".equals(vaccination.getStatus());
            
            if (isCompleted && givenDate != null && !givenDate.isEmpty()) {
                holder.layoutGivenDate.setVisibility(View.VISIBLE);
                try {
                    Date date = inputFormat.parse(givenDate);
                    holder.tvGivenDate.setText("Given: " + outputFormat.format(date));
                } catch (Exception e) {
                    holder.tvGivenDate.setText("Given: " + givenDate);
                }
            } else {
                holder.layoutGivenDate.setVisibility(View.GONE);
            }

            // Status
            if (isCompleted) {
                holder.tvStatus.setText("completed");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_category);
                holder.tvStatus.setTextColor(ContextCompat.getColor(PatientVaccinationDetailActivity.this, android.R.color.holo_green_dark));
            } else {
                holder.tvStatus.setText("pending");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                holder.tvStatus.setTextColor(ContextCompat.getColor(PatientVaccinationDetailActivity.this, android.R.color.holo_orange_dark));
            }

            // Batch number
            String batch = vaccination.getBatchNumber();
            if (batch != null && !batch.isEmpty()) {
                holder.tvBatchNumber.setText("Batch: " + batch);
                holder.tvBatchNumber.setVisibility(View.VISIBLE);
            } else {
                holder.tvBatchNumber.setVisibility(View.GONE);
            }

            // Notes
            String notes = vaccination.getNotes();
            if (notes != null && !notes.isEmpty()) {
                holder.tvNotes.setText(notes);
                holder.tvNotes.setVisibility(View.VISIBLE);
            } else {
                holder.tvNotes.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return vaccinations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvVaccineName, tvStatus, tvDueDate, tvGivenDate, tvBatchNumber, tvNotes;
            LinearLayout layoutDueDate, layoutGivenDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvVaccineName = itemView.findViewById(R.id.tv_vaccine_name);
                tvStatus = itemView.findViewById(R.id.tv_status);
                tvDueDate = itemView.findViewById(R.id.tv_due_date);
                tvGivenDate = itemView.findViewById(R.id.tv_given_date);
                tvBatchNumber = itemView.findViewById(R.id.tv_batch_number);
                tvNotes = itemView.findViewById(R.id.tv_notes);
                layoutDueDate = itemView.findViewById(R.id.layout_due_date);
                layoutGivenDate = itemView.findViewById(R.id.layout_given_date);
            }
        }
    }
}
