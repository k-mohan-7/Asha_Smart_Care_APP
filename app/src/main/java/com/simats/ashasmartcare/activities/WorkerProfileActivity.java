package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.R;

public class WorkerProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivWorkerAvatar;
    private TextView tvWorkerName;
    private TextView tvWorkerId;
    private TextView tvVillage;
    private TextView tvTotalVisits;
    private LinearLayout btnDisableAccount;

    private String workerName;
    private String workerId;
    private String village;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_worker_profile);

        initViews();
        getWorkerData();
        displayWorkerInfo();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivWorkerAvatar = findViewById(R.id.ivWorkerAvatar);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerId = findViewById(R.id.tvWorkerId);
        tvVillage = findViewById(R.id.tvVillage);
        tvTotalVisits = findViewById(R.id.tvTotalVisits);
        btnDisableAccount = findViewById(R.id.btnDisableAccount);
    }

    private void getWorkerData() {
        Intent intent = getIntent();
        workerName = intent.getStringExtra("workerName");
        workerId = intent.getStringExtra("workerId");
        village = intent.getStringExtra("village");
        status = intent.getStringExtra("status");
    }

    private void displayWorkerInfo() {
        if (workerName != null) {
            tvWorkerName.setText(workerName);
        }
        if (workerId != null) {
            tvWorkerId.setText(workerId);
        }
        if (village != null) {
            tvVillage.setText(village);
        }

        // Show hardcoded visits count for demo (in real app, this would come from
        // database)
        tvTotalVisits.setText("128");
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnDisableAccount.setOnClickListener(v -> {
            showDisableConfirmationDialog();
        });
    }

    private void showDisableConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Disable Account")
                .setMessage("Are you sure you want to disable " + workerName + "'s account?")
                .setPositiveButton("Disable", (dialog, which) -> {
                    // TODO: Implement actual disable logic
                    Toast.makeText(this, "Account disabled", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
