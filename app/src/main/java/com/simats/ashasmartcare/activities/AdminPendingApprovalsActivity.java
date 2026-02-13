package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
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

public class AdminPendingApprovalsActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerActionListener {

    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvNoRequests;
    private EditText etSearch;
    private RecyclerView rvPendingWorkers;
    private RecyclerView rvApprovedWorkers;
    private TextView tvApprovedHeader;

    private WorkerAdapter pendingAdapter;
    private WorkerAdapter approvedAdapter;
    private List<Worker> pendingWorkerList;
    private List<Worker> approvedWorkerList;

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

        setContentView(R.layout.activity_admin_pending_approvals);

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupRecyclerViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingApprovals();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvNoRequests = findViewById(R.id.tvNoRequests);
        etSearch = findViewById(R.id.etSearch);
        rvPendingWorkers = findViewById(R.id.rvSyncWorkers); // Reuse existing RecyclerView
        rvApprovedWorkers = findViewById(R.id.rvApprovedWorkers);
        tvApprovedHeader = findViewById(R.id.tvApprovedHeader);
        
        // Update title
        if (tvTitle != null) {
            tvTitle.setText("Pending Approvals");
        }
        
        // Hide stats cards (they're for sync status, not needed here)
        View cardTotal = findViewById(R.id.cardTotal);
        View cardSynced = findViewById(R.id.cardSynced);
        View cardDelayed = findViewById(R.id.cardDelayed);
        View chipAll = findViewById(R.id.chipAll);
        View chipSynced = findViewById(R.id.chipSynced);
        View chipDelayed = findViewById(R.id.chipDelayed);
        
        if (cardTotal != null) cardTotal.setVisibility(View.GONE);
        if (cardSynced != null) cardSynced.setVisibility(View.GONE);
        if (cardDelayed != null) cardDelayed.setVisibility(View.GONE);
        if (chipAll != null) chipAll.setVisibility(View.GONE);
        if (chipSynced != null) chipSynced.setVisibility(View.GONE);
        if (chipDelayed != null) chipDelayed.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        // Pending workers list
        pendingWorkerList = new ArrayList<>();
        pendingAdapter = new WorkerAdapter(this, pendingWorkerList, this);
        rvPendingWorkers.setLayoutManager(new LinearLayoutManager(this));
        rvPendingWorkers.setAdapter(pendingAdapter);
        
        // Approved workers list (shown if no pending)
        approvedWorkerList = new ArrayList<>();
        approvedAdapter = new WorkerAdapter(this, approvedWorkerList, this);
        if (rvApprovedWorkers != null) {
            rvApprovedWorkers.setLayoutManager(new LinearLayoutManager(this));
            rvApprovedWorkers.setAdapter(approvedAdapter);
        }
    }

    private void loadPendingApprovals() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php?action=get_pending_approvals";

        apiHelper.makeRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (success) {
                                JSONArray workers = response.optJSONArray("data");
                                if (workers != null) {
                                    pendingWorkerList.clear();
                                    
                                    for (int i = 0; i < workers.length(); i++) {
                                        JSONObject workerObj = workers.getJSONObject(i);
                                        
                                        int id = workerObj.optInt("id", 0);
                                        String name = workerObj.optString("name", "");
                                        String phone = workerObj.optString("phone", "");
                                        String village = workerObj.optString("village", "");
                                        String status = workerObj.optString("status", 
                                                          workerObj.optString("account_status", "pending"));
                                        
                                        // Only show pending workers
                                        if ("pending".equals(status)) {
                                            Worker worker = new Worker(name, phone, village, status);
                                            worker.setId(id);
                                            pendingWorkerList.add(worker);
                                        }
                                    }
                                    
                                    runOnUiThread(() -> {
                                        pendingAdapter.updateWorkerList(pendingWorkerList);
                                        updateUI();
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(AdminPendingApprovalsActivity.this,
                                        "Error loading pending approvals", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminPendingApprovalsActivity.this,
                                    "Failed to load approvals: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }
    
    private void updateUI() {
        if (pendingWorkerList.isEmpty()) {
            // No pending requests - show message and optionally load approved workers
            if (tvNoRequests != null) {
                tvNoRequests.setVisibility(View.VISIBLE);
                tvNoRequests.setText("No pending requests yet");
            }
            rvPendingWorkers.setVisibility(View.GONE);
            
            // Optionally load approved workers below
            loadApprovedWorkers();
        } else {
            // Have pending requests - show them
            if (tvNoRequests != null) {
                tvNoRequests.setVisibility(View.GONE);
            }
            rvPendingWorkers.setVisibility(View.VISIBLE);
            
            // Hide approved list when there are pending requests
            if (rvApprovedWorkers != null) {
                rvApprovedWorkers.setVisibility(View.GONE);
            }
            if (tvApprovedHeader != null) {
                tvApprovedHeader.setVisibility(View.GONE);
            }
        }
    }
    
    private void loadApprovedWorkers() {
        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php?action=get_workers";

        apiHelper.makeRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (success) {
                                JSONArray workers = response.optJSONArray("data");
                                if (workers != null) {
                                    approvedWorkerList.clear();
                                    
                                    for (int i = 0; i < workers.length(); i++) {
                                        JSONObject workerObj = workers.getJSONObject(i);
                                        
                                        int id = workerObj.optInt("id", 0);
                                        String name = workerObj.optString("name", "");
                                        String phone = workerObj.optString("phone", "");
                                        String village = workerObj.optString("village", "");
                                        String status = workerObj.optString("status", 
                                                          workerObj.optString("account_status", "approved"));
                                        
                                        // Only show approved workers who have logged in
                                        if ("approved".equals(status)) {
                                            Worker worker = new Worker(name, phone, village, status);
                                            worker.setId(id);
                                            approvedWorkerList.add(worker);
                                        }
                                    }
                                    
                                    runOnUiThread(() -> {
                                        if (!approvedWorkerList.isEmpty()) {
                                            approvedAdapter.updateWorkerList(approvedWorkerList);
                                            if (rvApprovedWorkers != null) {
                                                rvApprovedWorkers.setVisibility(View.VISIBLE);
                                            }
                                            if (tvApprovedHeader != null) {
                                                tvApprovedHeader.setVisibility(View.VISIBLE);
                                                tvApprovedHeader.setText("Workers Who Have Logged In");
                                            }
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Silently fail - approved list is optional
                    }
                }
        );
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pendingAdapter.filter(s.toString());
                if (rvApprovedWorkers != null && rvApprovedWorkers.getVisibility() == View.VISIBLE) {
                    approvedAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // WorkerAdapter callbacks for approve/reject actions
    @Override
    public void onActivateClick(Worker worker) {
        // Not used in pending approvals screen
    }

    @Override
    public void onDeactivateClick(Worker worker) {
        // Not used in pending approvals screen
    }
    
    @Override
    public void onApproveClick(Worker worker) {
        // Handle approve action
        approveWorker(worker);
    }
    
    private void approveWorker(Worker worker) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl = sessionManager.getApiBaseUrl() + "admin.php";
        
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("action", "approve_worker");
            requestData.put("worker_id", worker.getId());
            
            apiHelper.makeRequest(
                    Request.Method.POST,
                    apiUrl,
                    requestData,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            runOnUiThread(() -> {
                                Toast.makeText(AdminPendingApprovalsActivity.this,
                                        "Worker approved successfully", Toast.LENGTH_SHORT).show();
                                loadPendingApprovals(); // Refresh list
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AdminPendingApprovalsActivity.this,
                                        "Failed to approve worker: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error approving worker", Toast.LENGTH_SHORT).show();
        }
    }
}
