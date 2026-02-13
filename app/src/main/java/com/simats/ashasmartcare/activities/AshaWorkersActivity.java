package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.WorkerAdapter;
import com.simats.ashasmartcare.models.Worker;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AshaWorkersActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerActionListener {

    private ImageView ivBack;
    private EditText etSearch;
    private RecyclerView rvWorkers;
    private FloatingActionButton fabAddWorker;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;

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

        setContentView(R.layout.activity_asha_workers);

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkers();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etSearch = findViewById(R.id.etSearch);
        rvWorkers = findViewById(R.id.rvWorkers);
        fabAddWorker = findViewById(R.id.fabAddWorker);
    }

    private void setupRecyclerView() {
        workerList = new ArrayList<>();
        workerAdapter = new WorkerAdapter(this, workerList, this);
        rvWorkers.setLayoutManager(new LinearLayoutManager(this));
        rvWorkers.setAdapter(workerAdapter);
    }

    private void loadWorkers() {
        workerList.clear();

        if (!NetworkUtils.isNetworkAvailable(this)) {
            // Load from local DB if offline
            List<Worker> dbWorkers = com.simats.ashasmartcare.database.DatabaseHelper.getInstance(this).getAllWorkers();
            workerList.addAll(dbWorkers);
            workerAdapter.updateWorkerList(workerList);
            return;
        }

        // Load from backend API
        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php?action=get_workers";

        apiHelper.makeRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray workers = response.optJSONArray("data");
                                if (workers != null) {
                                    workerList.clear();
                                    for (int i = 0; i < workers.length(); i++) {
                                        JSONObject workerObj = workers.getJSONObject(i);

                                        int id = workerObj.optInt("id", 0);
                                        String name = workerObj.optString("name", "");
                                        String phone = workerObj.optString("phone", "");
                                        String village = workerObj.optString("village", "");
                                        // API returns "status" not "account_status"
                                        String status = workerObj.optString("status",
                                                workerObj.optString("account_status", "pending"));

                                        // Show approved and disabled workers (pending go to Pending Approvals screen)
                                        if ("approved".equals(status) || "disabled".equals(status)) {
                                            Worker worker = new Worker(name, phone, village, status);
                                            worker.setId(id);
                                            workerList.add(worker);
                                        }
                                    }

                                    runOnUiThread(() -> {
                                        workerAdapter.updateWorkerList(workerList);
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(AshaWorkersActivity.this,
                                        "Error loading workers", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AshaWorkersActivity.this,
                                    "Failed to load workers: " + error, Toast.LENGTH_SHORT).show();
                            // Load from local DB as fallback
                            List<Worker> dbWorkers = com.simats.ashasmartcare.database.DatabaseHelper
                                    .getInstance(AshaWorkersActivity.this).getAllWorkers();
                            workerList.addAll(dbWorkers);
                            workerAdapter.updateWorkerList(workerList);
                        });
                    }
                });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAddWorker.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AddAshaWorkerActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

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
    }

    @Override
    public void onActivateClick(Worker worker) {
        // Assuming Activate is similar to Approve but maybe for deactivated ones
        com.simats.ashasmartcare.database.DatabaseHelper.getInstance(this)
                .updateWorkerStatusByServerId((int) worker.getId(), "active");
        worker.setStatus("active");
        // Remove from list if we are hiding active ones
        workerList.remove(worker);
        workerAdapter.updateWorkerList(workerList);
        Toast.makeText(this, worker.getName() + " activated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeactivateClick(Worker worker) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call backend API to deactivate worker
        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php";

        try {
            JSONObject params = new JSONObject();
            params.put("action", "update_worker_status");
            params.put("user_id", worker.getId());
            params.put("status", "deactivated");

            Toast.makeText(this, "Deactivating...", Toast.LENGTH_SHORT).show();

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
                                    // Update local DB
                                    com.simats.ashasmartcare.database.DatabaseHelper.getInstance(
                                            AshaWorkersActivity.this)
                                            .updateWorkerStatusByServerId((int) worker.getId(), "deactivated");

                                    runOnUiThread(() -> {
                                        worker.setStatus("deactivated");
                                        workerAdapter.notifyDataSetChanged();
                                        Toast.makeText(AshaWorkersActivity.this,
                                                worker.getName() + " deactivated", Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        String msg = response.optString("message", "Failed to deactivate worker");
                                        Toast.makeText(AshaWorkersActivity.this,
                                                msg, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AshaWorkersActivity.this,
                                        "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onApproveClick(Worker worker) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call backend API to approve worker
        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php";

        try {
            JSONObject params = new JSONObject();
            params.put("action", "update_worker_status");
            params.put("user_id", worker.getId());
            params.put("status", "approved");

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
                                    // Update local DB
                                    com.simats.ashasmartcare.database.DatabaseHelper.getInstance(
                                            AshaWorkersActivity.this)
                                            .updateWorkerStatusByServerId((int) worker.getId(), "approved");

                                    runOnUiThread(() -> {
                                        worker.setStatus("approved");
                                        workerList.remove(worker);
                                        workerAdapter.updateWorkerList(workerList);
                                        Toast.makeText(AshaWorkersActivity.this,
                                                worker.getName() + " approved", Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        Toast.makeText(AshaWorkersActivity.this,
                                                "Failed to approve worker", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AshaWorkersActivity.this,
                                        "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }
}