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
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;
import com.android.volley.Request;
import org.json.JSONObject;

public class WorkerProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivWorkerAvatar;
    private TextView tvWorkerName;
    private TextView tvWorkerId;
    private TextView tvVillage;
    private TextView tvTotalVisits;
    private LinearLayout btnDisableAccount;
    private TextView tvDisableButtonText;
    private ImageView ivEditProfile;

    private String workerName;
    private String workerId;
    private long id;
    private String village;
    private String status;

    private ApiHelper apiHelper;
    private SessionManager sessionManager;

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

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

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
        tvDisableButtonText = findViewById(R.id.tvDisableButtonText);
        ivEditProfile = findViewById(R.id.ivEditProfile);
    }

    private void getWorkerData() {
        Intent intent = getIntent();
        id = intent.getLongExtra("id", -1);
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
        // Update button text based on current status
        if ("disabled".equals(status) || "deactivated".equals(status)) {
            tvDisableButtonText.setText("Enable Account");
        } else {
            tvDisableButtonText.setText("Disable Account");
        }
        // Load profile picture
        loadProfileImage(sessionManager.getProfileImage());
    }

    private void loadProfileImage(String path) {
        if (path != null && !path.isEmpty()) {
            java.io.File imgFile = new java.io.File(path);
            if (imgFile.exists()) {
                android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (ivWorkerAvatar != null) {
                    ivWorkerAvatar.setImageBitmap(myBitmap);
                }
            }
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnDisableAccount.setOnClickListener(v -> {
            // Toggle between disable and enable based on current status
            if ("disabled".equals(status) || "deactivated".equals(status)) {
                showEnableConfirmationDialog();
            } else {
                showDisableConfirmationDialog();
            }
        });

        ivEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.simats.ashasmartcare.EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void showDisableConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Disable Account")
                .setMessage("Are you sure you want to disable " + workerName + "'s account? They will not be able to login.")
                .setPositiveButton("Disable", (dialog, which) -> {
                    updateWorkerStatus("disabled", "Disabling account...", "Account disabled successfully");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEnableConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Account")
                .setMessage("Enable " + workerName + "'s account? They will be able to login again.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    updateWorkerStatus("approved", "Enabling account...", "Account enabled successfully");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateWorkerStatus(String newStatus, String loadingMsg, String successMsg) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Admin actions require online access.", Toast.LENGTH_LONG).show();
            return;
        }

        if (id == -1) {
            Toast.makeText(this, "Error: Invalid worker ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php";

        try {
            JSONObject params = new JSONObject();
            params.put("action", "update_worker_status");
            params.put("worker_id", id); // Backend expects worker_id, not user_id
            params.put("status", newStatus);

            // Show loading
            Toast.makeText(this, loadingMsg, Toast.LENGTH_SHORT).show();

            apiHelper.makeRequest(
                    Request.Method.POST,
                    apiUrl,
                    params,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    // No local DB update - admin actions are online only
                                    runOnUiThread(() -> {
                                        Toast.makeText(WorkerProfileActivity.this,
                                                successMsg, Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        String msg = response.optString("message",
                                                "Failed to update account status");
                                        Toast.makeText(WorkerProfileActivity.this, msg, Toast.LENGTH_SHORT)
                                                .show();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> {
                                    Toast.makeText(WorkerProfileActivity.this, "Error parsing response",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(WorkerProfileActivity.this, "Error: " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }
}
