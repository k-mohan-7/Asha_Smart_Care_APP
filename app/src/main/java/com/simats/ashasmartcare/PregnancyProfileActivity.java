package com.simats.ashasmartcare;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

public class PregnancyProfileActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_pregnancy_profile);

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

            // For pregnancy profile, we assume category is already checked
            tvCategoryBadge.setText("Pregnant");

            // Toggle risk badge visibility if needed
            // if (patient.getRiskStatus() != null) { ... }
        }
    }

    private void setupTabs() {
        PregnancyTabsAdapter adapter = new PregnancyTabsAdapter(this);
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
            android.content.Intent intent = new android.content.Intent(this, AddPregnancyVisitActivity.class);
            intent.putExtra("patient_id", (long) patientId); // Cast to long as expected by AddPregnancyVisitActivity
            startActivity(intent);
        });
    }

    private class PregnancyTabsAdapter extends FragmentStateAdapter {
        public PregnancyTabsAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            Bundle args = new Bundle();
            args.putInt("patient_id", patientId);

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
