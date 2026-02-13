package com.simats.ashasmartcare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.fragments.PatientAlertsFragment;
import com.simats.ashasmartcare.fragments.PatientGrowthFragment;
import com.simats.ashasmartcare.fragments.PatientOverviewFragment;
import com.simats.ashasmartcare.fragments.PatientVisitsFragment;
import com.simats.ashasmartcare.models.Patient;

public class ChildProfileActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView tvPatientName, tvLocation, tvAge;
    private TextView tvRiskBadge, tvCategoryBadge;
    private ImageView ivBackHeader;
    private Button btnBack, btnAddVisit;

    private int patientId;
    private Patient patient;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_profile);

        databaseHelper = DatabaseHelper.getInstance(this);
        patientId = getIntent().getIntExtra("patient_id", -1);

        if (patientId == -1) {
            finish();
            return;
        }

        initViews();
        loadPatientData();
        setupTabs();
        setupListeners();
    }

    private void initViews() {
        ivBackHeader = findViewById(R.id.iv_back_header);
        tvPatientName = findViewById(R.id.tv_patient_name);
        tvLocation = findViewById(R.id.tv_location);
        tvAge = findViewById(R.id.tv_age);
        tvRiskBadge = findViewById(R.id.tv_risk_badge);
        tvCategoryBadge = findViewById(R.id.tv_category_badge);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        btnBack = findViewById(R.id.btn_back);
        btnAddVisit = findViewById(R.id.btn_add_visit);
    }

    private void loadPatientData() {
        patient = databaseHelper.getPatientById(patientId);
        if (patient != null) {
            tvPatientName.setText(patient.getName());
              tvLocation.setText(patient.getAddress());
            tvAge.setText(patient.getAge() + " Years");
            tvCategoryBadge.setText("Child (0-5)");

            // In a real app, logic would set risk status from DB
            if (patient.isHighRisk()) {
                tvRiskBadge.setVisibility(android.view.View.VISIBLE);
                tvRiskBadge.setText("High Risk");
            } else {
                tvRiskBadge.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void setupTabs() {
        ChildTabsAdapter adapter = new ChildTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Overview");
                    break;
                case 1:
                    tab.setText("Visits");
                    break;
                case 2:
                    tab.setText("Alerts");
                    break;
                case 3:
                    tab.setText("Growth");
                    break;
            }
        }).attach();
    }

    private void setupListeners() {
        ivBackHeader.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
        btnAddVisit.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddChildGrowthActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });
    }

    private class ChildTabsAdapter extends FragmentStateAdapter {
        public ChildTabsAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            Bundle args = new Bundle();
            args.putInt("patient_id", patientId);
            args.putString("category", "child"); // Pass category for dynamic logic in fragments

            switch (position) {
                case 0:
                    fragment = new PatientOverviewFragment();
                    break;
                case 1:
                    fragment = new PatientVisitsFragment();
                    break;
                case 2:
                    fragment = new PatientAlertsFragment();
                    break;
                case 3:
                    fragment = new PatientGrowthFragment();
                    break;
                default:
                    fragment = new PatientOverviewFragment();
                    break;
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
