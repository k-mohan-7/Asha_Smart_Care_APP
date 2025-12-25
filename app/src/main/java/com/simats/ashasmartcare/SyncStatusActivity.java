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

import com.simats.ashasmartcare.adapters.SyncAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.SyncRecord;
import com.simats.ashasmartcare.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class SyncStatusActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvSyncStatus, tvPendingCount, tvSyncedCount, tvFailedCount;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private Button btnSyncNow;
    private ProgressBar progressBar;

    private SyncAdapter adapter;
    private List<SyncRecord> syncRecordList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_status);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvSyncedCount = findViewById(R.id.tvSyncedCount);
        tvFailedCount = findViewById(R.id.tvFailedCount);
        recyclerView = findViewById(R.id.recyclerView);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = DatabaseHelper.getInstance(this);
        syncRecordList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new SyncAdapter(this, syncRecordList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSyncNow.setOnClickListener(v -> performSync());
    }

    private void loadData() {
        syncRecordList.clear();
        syncRecordList.addAll(dbHelper.getAllSyncRecords());
        
        int pending = 0, synced = 0, failed = 0;
        for (SyncRecord record : syncRecordList) {
            switch (record.getSyncStatus()) {
                case "PENDING":
                    pending++;
                    break;
                case "SYNCED":
                    synced++;
                    break;
                case "FAILED":
                    failed++;
                    break;
            }
        }

        tvPendingCount.setText(String.valueOf(pending));
        tvSyncedCount.setText(String.valueOf(synced));
        tvFailedCount.setText(String.valueOf(failed));

        if (NetworkUtils.isNetworkAvailable(this)) {
            tvSyncStatus.setText("Online");
            tvSyncStatus.setTextColor(getResources().getColor(R.color.success));
            btnSyncNow.setEnabled(pending > 0);
        } else {
            tvSyncStatus.setText("Offline");
            tvSyncStatus.setTextColor(getResources().getColor(R.color.error));
            btnSyncNow.setEnabled(false);
        }

        adapter.notifyDataSetChanged();

        if (syncRecordList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
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
