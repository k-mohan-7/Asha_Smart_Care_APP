package com.simats.ashasmartcare;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.adapters.SyncPendingAdapter;
import com.simats.ashasmartcare.adapters.SyncSyncedAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.SyncRecord;
import com.simats.ashasmartcare.utils.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncStatusActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvLastSynced, tvPendingInfo;
    private RecyclerView recyclerPending, recyclerSynced;
    private LinearLayout layoutEmpty, layoutPendingSection, layoutSyncedSection;
    private Button btnSyncNow;
    private ProgressBar progressBar;

    private SyncPendingAdapter pendingAdapter;
    private SyncSyncedAdapter syncedAdapter;
    private List<SyncRecord> pendingRecords, syncedRecords;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_status);

        initViews();
        setupRecyclerViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvLastSynced = findViewById(R.id.tvLastSynced);
        tvPendingInfo = findViewById(R.id.tvPendingInfo);
        recyclerPending = findViewById(R.id.recyclerPending);
        recyclerSynced = findViewById(R.id.recyclerSynced);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutPendingSection = findViewById(R.id.layoutPendingSection);
        layoutSyncedSection = findViewById(R.id.layoutSyncedSection);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = DatabaseHelper.getInstance(this);
        pendingRecords = new ArrayList<>();
        syncedRecords = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        pendingAdapter = new SyncPendingAdapter(this, pendingRecords);
        recyclerPending.setLayoutManager(new LinearLayoutManager(this));
        recyclerPending.setAdapter(pendingAdapter);

        syncedAdapter = new SyncSyncedAdapter(this, syncedRecords);
        recyclerSynced.setLayoutManager(new LinearLayoutManager(this));
        recyclerSynced.setAdapter(syncedAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSyncNow.setOnClickListener(v -> performSync());
    }

    private void loadData() {
        // Get all sync records
        List<SyncRecord> allRecords = dbHelper.getAllSyncRecords();
        
        pendingRecords.clear();
        syncedRecords.clear();
        
        for (SyncRecord record : allRecords) {
            if ("PENDING".equals(record.getSyncStatus())) {
                pendingRecords.add(record);
            } else if ("SYNCED".equals(record.getSyncStatus())) {
                syncedRecords.add(record);
            }
        }

        // Update pending info
        int pendingCount = pendingRecords.size();
        tvPendingInfo.setText(pendingCount + " record" + (pendingCount != 1 ? "s" : "") + " pending upload");

        // Update last synced time
        if (!syncedRecords.isEmpty()) {
            SyncRecord lastSynced = syncedRecords.get(0);
            String timeAgo = getTimeAgo(lastSynced.getCreatedAt());
            tvLastSynced.setText("Last synced: " + timeAgo);
        } else {
            tvLastSynced.setText("Last synced: Never");
        }

        // Update adapters
        pendingAdapter.notifyDataSetChanged();
        syncedAdapter.notifyDataSetChanged();

        // Show/hide sections
        if (pendingRecords.isEmpty()) {
            layoutPendingSection.setVisibility(View.GONE);
        } else {
            layoutPendingSection.setVisibility(View.VISIBLE);
        }

        if (syncedRecords.isEmpty()) {
            layoutSyncedSection.setVisibility(View.GONE);
        } else {
            layoutSyncedSection.setVisibility(View.VISIBLE);
        }

        // Show empty state if no records at all
        if (allRecords.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }

        // Enable/disable sync button
        btnSyncNow.setEnabled(NetworkUtils.isNetworkAvailable(this) && pendingCount > 0);
    }

    private String getTimeAgo(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            if (date == null) return "Unknown";

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
            } else if (hours < 24) {
                return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
            } else {
                return days + " day" + (days != 1 ? "s" : "") + " ago";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void performSync() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSyncNow.setEnabled(false);

        // Simulate sync process - in real implementation, this would call SyncService
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnSyncNow.setEnabled(true);
            Toast.makeText(this, "Sync completed", Toast.LENGTH_SHORT).show();
            loadData();
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
