package com.simats.ashasmartcare;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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

public class PatientDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView tvPatientName, tvPatientVillage, tvPatientAge;
    private TextView tvCategoryBadge, tvRiskBadge;
    private ImageView ivBack, ivMore;
    
    private int patientId;
    private String patientName, patientCategory;
    private int patientAge;
    private String patientVillage;
    private boolean isHighRisk;
    
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Get patient ID from intent
        patientId = getIntent().getIntExtra("patient_id", -1);
        
        if (patientId == -1) {
            finish();
            return;
        }
        
        // Initialize views
        initViews();
        
        // Load patient data
        loadPatientData();
        
        // Setup ViewPager and Tabs
        setupViewPager();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivMore = findViewById(R.id.iv_more);
        tvPatientName = findViewById(R.id.tv_patient_name);
        tvPatientVillage = findViewById(R.id.tv_village);
        tvPatientAge = findViewById(R.id.tv_age);
        tvCategoryBadge = findViewById(R.id.tv_category_badge);
        tvRiskBadge = findViewById(R.id.tv_risk_badge);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
    }

    private void loadPatientData() {
        com.simats.ashasmartcare.models.Patient patient = databaseHelper.getPatientById(patientId);
        
        if (patient != null) {
            patientName = patient.getName();
            patientAge = patient.getAge();
            patientCategory = patient.getCategory();
            patientVillage = patient.getArea();
            
            // Update UI
            tvPatientName.setText(patientName);
            tvPatientVillage.setText(patientVillage);
            tvPatientAge.setText(patientAge + " years");
            
            // Set category badge
            String categoryText = "";
            if ("pregnant".equalsIgnoreCase(patientCategory)) {
                categoryText = "Pregnant";
                isHighRisk = true; // For now, all pregnant women are considered high risk
            } else if ("child".equalsIgnoreCase(patientCategory)) {
                categoryText = "Child";
                isHighRisk = false;
            } else {
                categoryText = "General";
                isHighRisk = false;
            }
            tvCategoryBadge.setText(categoryText);
            
            // Show/hide risk badge
            if (isHighRisk) {
                tvRiskBadge.setVisibility(TextView.VISIBLE);
                tvRiskBadge.setText("High Risk");
            } else {
                tvRiskBadge.setVisibility(TextView.GONE);
            }
        }
    }

    private void setupViewPager() {
        PatientTabsAdapter adapter = new PatientTabsAdapter(this);
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

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        ivMore.setOnClickListener(v -> {
            // TODO: Show menu with edit/delete options
        });
    }

    public int getPatientId() {
        return patientId;
    }

    public String getPatientCategory() {
        return patientCategory;
    }

    private class PatientTabsAdapter extends FragmentStateAdapter {
        public PatientTabsAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            Bundle args = new Bundle();
            args.putInt("patient_id", patientId);
            args.putString("patient_category", patientCategory);
            
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
