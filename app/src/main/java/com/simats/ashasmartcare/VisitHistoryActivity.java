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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.adapters.VisitHistoryAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Visit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitHistoryActivity extends AppCompatActivity implements VisitHistoryAdapter.OnVisitClickListener {

    private ImageView ivBack, ivFilter;
    private EditText etSearch;
    private RecyclerView recyclerToday, recyclerYesterday, recyclerOlder;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty, layoutTodaySection, layoutYesterdaySection, layoutOlderSection;

    private VisitHistoryAdapter todayAdapter, yesterdayAdapter, olderAdapter;
    private List<Visit> allVisits, todayVisits, yesterdayVisits, olderVisits;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_history);

        initViews();
        setupRecyclerViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivFilter = findViewById(R.id.ivFilter);
        etSearch = findViewById(R.id.etSearch);
        recyclerToday = findViewById(R.id.recyclerToday);
        recyclerYesterday = findViewById(R.id.recyclerYesterday);
        recyclerOlder = findViewById(R.id.recyclerOlder);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutTodaySection = findViewById(R.id.layoutTodaySection);
        layoutYesterdaySection = findViewById(R.id.layoutYesterdaySection);
        layoutOlderSection = findViewById(R.id.layoutOlderSection);

        dbHelper = DatabaseHelper.getInstance(this);
        allVisits = new ArrayList<>();
        todayVisits = new ArrayList<>();
        yesterdayVisits = new ArrayList<>();
        olderVisits = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        todayAdapter = new VisitHistoryAdapter(this, todayVisits, this);
        recyclerToday.setLayoutManager(new LinearLayoutManager(this));
        recyclerToday.setAdapter(todayAdapter);

        yesterdayAdapter = new VisitHistoryAdapter(this, yesterdayVisits, this);
        recyclerYesterday.setLayoutManager(new LinearLayoutManager(this));
        recyclerYesterday.setAdapter(yesterdayAdapter);

        olderAdapter = new VisitHistoryAdapter(this, olderVisits, this);
        recyclerOlder.setLayoutManager(new LinearLayoutManager(this));
        recyclerOlder.setAdapter(olderAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivFilter.setOnClickListener(v -> 
            Toast.makeText(this, "Filter functionality coming soon", Toast.LENGTH_SHORT).show()
        );

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVisits(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        showLoading(true);
        allVisits.clear();
        allVisits.addAll(dbHelper.getAllVisits());
        categorizeVisits();
        showLoading(false);
    }

    private void categorizeVisits() {
        todayVisits.clear();
        yesterdayVisits.clear();
        olderVisits.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = sdf.format(cal.getTime());

        for (Visit visit : allVisits) {
            String visitDate = visit.getVisitDate().substring(0, 10); // Extract date part
            
            if (visitDate.equals(today)) {
                todayVisits.add(visit);
            } else if (visitDate.equals(yesterday)) {
                yesterdayVisits.add(visit);
            } else {
                olderVisits.add(visit);
            }
        }

        updateUI();
    }

    private void filterVisits(String query) {
        if (query.isEmpty()) {
            categorizeVisits();
            return;
        }

        List<Visit> filtered = new ArrayList<>();
        for (Visit visit : allVisits) {
            String patientName = dbHelper.getPatientById(visit.getPatientId()).getName().toLowerCase();
            String patientId = String.valueOf(visit.getPatientId());
            
            if (patientName.contains(query.toLowerCase()) || patientId.contains(query)) {
                filtered.add(visit);
            }
        }

        todayVisits.clear();
        yesterdayVisits.clear();
        olderVisits.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = sdf.format(cal.getTime());

        for (Visit visit : filtered) {
            String visitDate = visit.getVisitDate().substring(0, 10);
            
            if (visitDate.equals(today)) {
                todayVisits.add(visit);
            } else if (visitDate.equals(yesterday)) {
                yesterdayVisits.add(visit);
            } else {
                olderVisits.add(visit);
            }
        }

        updateUI();
    }

    private void updateUI() {
        todayAdapter.notifyDataSetChanged();
        yesterdayAdapter.notifyDataSetChanged();
        olderAdapter.notifyDataSetChanged();

        // Show/hide sections
        layoutTodaySection.setVisibility(todayVisits.isEmpty() ? View.GONE : View.VISIBLE);
        layoutYesterdaySection.setVisibility(yesterdayVisits.isEmpty() ? View.GONE : View.VISIBLE);
        layoutOlderSection.setVisibility(olderVisits.isEmpty() ? View.GONE : View.VISIBLE);

        // Show empty state if no visits at all
        if (allVisits.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onVisitClick(Visit visit) {
        Intent intent = new Intent(this, AddVisitActivity.class);
        intent.putExtra("patient_id", visit.getPatientId());
        intent.putExtra("visit_id", visit.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
