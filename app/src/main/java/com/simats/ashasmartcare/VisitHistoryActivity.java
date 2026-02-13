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

import com.simats.ashasmartcare.activities.BaseActivity;
import com.simats.ashasmartcare.adapters.VisitHistoryAdapter;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Visit;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitHistoryActivity extends BaseActivity implements VisitHistoryAdapter.OnVisitClickListener {

    @Override
    protected int getNavItemId() {
        return R.id.nav_visits;
    }

    private ImageView ivBack;
    private RecyclerView recyclerToday, recyclerYesterday, recyclerOlder;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty, layoutTodaySection, layoutYesterdaySection, layoutOlderSection;

    private VisitHistoryAdapter todayAdapter, yesterdayAdapter, olderAdapter;
    private List<Visit> allVisits, todayVisits, yesterdayVisits, olderVisits;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

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
        recyclerToday = findViewById(R.id.recyclerToday);
        recyclerYesterday = findViewById(R.id.recyclerYesterday);
        recyclerOlder = findViewById(R.id.recyclerOlder);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutTodaySection = findViewById(R.id.layoutTodaySection);
        layoutYesterdaySection = findViewById(R.id.layoutYesterdaySection);
        layoutOlderSection = findViewById(R.id.layoutOlderSection);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
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
    }

    private void loadData() {
        showLoading(true);

        if (NetworkUtils.isNetworkAvailable(this)) {
            // ONLINE: Fetch from backend API
            fetchVisitsFromBackend();
        } else {
            // OFFLINE: Show no internet message
            showLoading(false);
            Toast.makeText(this, "⚠️ No internet connection. Cannot load visits.", Toast.LENGTH_LONG).show();
            allVisits.clear();
            categorizeVisits();
        }
    }

    private void fetchVisitsFromBackend() {
        String ashaId = String.valueOf(sessionManager.getUserId());

        apiHelper.makeGetRequest("visits.php?asha_id=" + ashaId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        JSONArray visitsArray = response.getJSONArray("data");
                        allVisits.clear();

                        for (int i = 0; i < visitsArray.length(); i++) {
                            JSONObject visitObj = visitsArray.getJSONObject(i);
                            Visit visit = new Visit();
                            visit.setServerId(visitObj.getInt("id"));
                            visit.setPatientId(visitObj.getInt("patient_id"));
                            visit.setVisitDate(visitObj.getString("visit_date"));
                            visit.setVisitType(visitObj.optString("visit_type", "General"));
                            visit.setDescription(visitObj.optString("description", ""));
                            visit.setNotes(visitObj.optString("notes", ""));
                            allVisits.add(visit);
                        }

                        runOnUiThread(() -> {
                            categorizeVisits();
                            showLoading(false);
                        });
                    } else {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(VisitHistoryActivity.this, "Failed to load visits", Toast.LENGTH_SHORT)
                                    .show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(VisitHistoryActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(VisitHistoryActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
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
