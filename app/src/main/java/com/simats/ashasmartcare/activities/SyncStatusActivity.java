package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.SyncWorkerAdapter;
import com.simats.ashasmartcare.models.SyncWorker;

import java.util.ArrayList;
import java.util.List;

public class SyncStatusActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvTotalAshas, tvSynced, tvDelayed;
    private EditText etSearch;
    private TextView chipAll, chipSynced, chipDelayed;
    private CardView cardTotal, cardSynced, cardDelayed;
    private RecyclerView rvSyncWorkers;

    private SyncWorkerAdapter workerAdapter;
    private List<SyncWorker> workerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_sync_status);

        initViews();
        setupRecyclerView();
        loadSampleData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTotalAshas = findViewById(R.id.tvTotalAshas);
        tvSynced = findViewById(R.id.tvSynced);
        tvDelayed = findViewById(R.id.tvDelayed);
        etSearch = findViewById(R.id.etSearch);
        chipAll = findViewById(R.id.chipAll);
        chipSynced = findViewById(R.id.chipSynced);
        chipDelayed = findViewById(R.id.chipDelayed);
        cardTotal = findViewById(R.id.cardTotal);
        cardSynced = findViewById(R.id.cardSynced);
        cardDelayed = findViewById(R.id.cardDelayed);
        rvSyncWorkers = findViewById(R.id.rvSyncWorkers);
    }

    private void setupRecyclerView() {
        workerList = new ArrayList<>();
        workerAdapter = new SyncWorkerAdapter(this, workerList);
        rvSyncWorkers.setLayoutManager(new LinearLayoutManager(this));
        rvSyncWorkers.setAdapter(workerAdapter);
    }

    private void loadSampleData() {
        // Load REAL data from database - removed dummy data
        workerList.clear();
        
        // Get actual sync records from database
        com.simats.ashasmartcare.database.DatabaseHelper dbHelper = 
            com.simats.ashasmartcare.database.DatabaseHelper.getInstance(this);
        
        int totalRecords = dbHelper.getTotalSyncRecords();
        int syncedRecords = dbHelper.getSyncedRecordsCount();
        int pendingRecords = dbHelper.getTotalPendingRecords();
        
        // Update stats with REAL data
        tvTotalAshas.setText(String.valueOf(totalRecords));
        tvSynced.setText(String.valueOf(syncedRecords));
        tvDelayed.setText(String.valueOf(pendingRecords));

        // Show message if no data
        if (totalRecords == 0) {
            tvTotalAshas.setText("0");
            tvSynced.setText("0");
            tvDelayed.setText("0");
        }

        workerAdapter.setData(workerList);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                workerAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter chips
        chipAll.setOnClickListener(v -> {
            selectChip(chipAll);
            workerAdapter.filterByStatus("all");
        });

        chipSynced.setOnClickListener(v -> {
            selectChip(chipSynced);
            workerAdapter.filterByStatus("synced");
        });

        chipDelayed.setOnClickListener(v -> {
            selectChip(chipDelayed);
            workerAdapter.filterByStatus("delayed");
        });

        // Card clicks
        cardTotal.setOnClickListener(v -> {
            selectChip(chipAll);
            workerAdapter.filterByStatus("all");
        });

        cardSynced.setOnClickListener(v -> {
            selectChip(chipSynced);
            workerAdapter.filterByStatus("synced");
        });

        cardDelayed.setOnClickListener(v -> {
            selectChip(chipDelayed);
            workerAdapter.filterByStatus("delayed");
        });
    }

    private void selectChip(TextView selectedChip) {
        // Reset all chips
        chipAll.setTextColor(getResources().getColor(R.color.text_secondary));
        chipAll.setBackgroundResource(R.drawable.bg_gray_light);
        chipSynced.setTextColor(getResources().getColor(R.color.text_secondary));
        chipSynced.setBackgroundResource(R.drawable.bg_gray_light);
        chipDelayed.setTextColor(getResources().getColor(R.color.text_secondary));
        chipDelayed.setBackgroundResource(R.drawable.bg_gray_light);

        // Highlight selected (Light blue bg, Blue text as per design)
        selectedChip.setTextColor(getResources().getColor(R.color.primary));
        selectedChip.setBackgroundResource(R.drawable.bg_blue_light);
    }
}
