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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.adapters.WorkerAdapter;
import com.simats.ashasmartcare.models.Worker;

import java.util.ArrayList;
import java.util.List;

public class AshaWorkersActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerActionListener {

    private ImageView ivBack;
    private EditText etSearch;
    private RecyclerView rvWorkers;
    private FloatingActionButton fabAddWorker;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_asha_workers);

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
        List<Worker> dbWorkers = com.simats.ashasmartcare.database.DatabaseHelper.getInstance(this).getAllWorkers();
        if (dbWorkers.isEmpty()) {
            // Only load samples if DB is empty to show something initially
            workerList.add(new Worker("Priya Sharma", "ASHA-1024", "Rampur", "active"));
            workerList.add(new Worker("Anjali Devi", "ASHA-1025", "Sitapur", "active"));
            workerList.add(new Worker("Sunita Kumar", "ASHA-1026", "Govindpur", "pending"));
        } else {
            workerList.addAll(dbWorkers);
        }
        workerAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        fabAddWorker.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AddAshaWorkerActivity.class));
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
        worker.setStatus("active");
        workerAdapter.notifyDataSetChanged();
        Toast.makeText(this, worker.getName() + " activated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeactivateClick(Worker worker) {
        worker.setStatus("deactivated");
        workerAdapter.notifyDataSetChanged();
        Toast.makeText(this, worker.getName() + " deactivated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveClick(Worker worker) {
        worker.setStatus("active");
        workerAdapter.notifyDataSetChanged();
        Toast.makeText(this, worker.getName() + " approved and activated", Toast.LENGTH_SHORT).show();
    }
}
