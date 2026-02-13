package com.simats.ashasmartcare;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.database.DatabaseHelper;

public class PatientDetailActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvPatientName;
    private View itemAge, itemGender, itemPhone, itemState, itemDistrict, itemVillage, itemCategory;
    private View actionEditProfile, actionPregnancyVisits, actionChildGrowth, actionVaccinations, actionNormalVisits;

    private int patientId;
    private com.simats.ashasmartcare.models.Patient patient;
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

        initViews();
        loadPatientData();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvPatientName = findViewById(R.id.tv_patient_name);

        // Detail items
        itemAge = findViewById(R.id.item_age);
        itemGender = findViewById(R.id.item_gender);
        itemPhone = findViewById(R.id.item_phone);
        itemState = findViewById(R.id.item_state);
        itemDistrict = findViewById(R.id.item_district);
        itemVillage = findViewById(R.id.item_village);
        itemCategory = findViewById(R.id.item_category);

        // Action items
        actionEditProfile = findViewById(R.id.action_edit_profile);
        actionPregnancyVisits = findViewById(R.id.action_pregnancy_visits);
        actionChildGrowth = findViewById(R.id.action_child_growth);
        actionVaccinations = findViewById(R.id.action_vaccinations);
        actionNormalVisits = findViewById(R.id.action_normal_visits);

        // Setup labels and icons
        setupDetailItem(itemAge, "Age", "ic_calendar_today"); // Or just leave icon if not in layout
        setupDetailItem(itemGender, "Gender", "");
        setupDetailItem(itemPhone, "Phone", "");
        setupDetailItem(itemState, "State", "");
        setupDetailItem(itemDistrict, "District", "");
        setupDetailItem(itemVillage, "Village", "");
        setupDetailItem(itemCategory, "Category", "");

        setupActionItem(actionEditProfile, "Edit Profile", R.drawable.ic_edit, "#F3F4F6");
        setupActionItem(actionPregnancyVisits, "Pregnancy Visits", R.drawable.ic_pregnant, "#EBF5FF");
        setupActionItem(actionChildGrowth, "Child Growth", R.drawable.ic_growth_monitoring, "#ECFDF5");
        setupActionItem(actionVaccinations, "Vaccinations", R.drawable.ic_vaccine, "#FFFBEB");
        setupActionItem(actionNormalVisits, "Normal Visits", R.drawable.ic_history, "#F5F3FF");
    }

    private void setupDetailItem(View view, String label, String iconName) {
        TextView tvLabel = view.findViewById(R.id.tv_label);
        tvLabel.setText(label);
    }

    private void setupActionItem(View view, String title, int iconRes, String bgColor) {
        TextView tvTitle = view.findViewById(R.id.tv_action_title);
        ImageView ivIcon = view.findViewById(R.id.iv_action_icon);
        View cvContainer = view.findViewById(R.id.cv_icon_container);

        tvTitle.setText(title);
        ivIcon.setImageResource(iconRes);
        if (bgColor != null) {
            cvContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(bgColor)));
        }
    }

    private void loadPatientData() {
        patient = databaseHelper.getPatientById(patientId);

        if (patient != null) {
            tvPatientName.setText(patient.getName());

            setDetailValue(itemAge, patient.getAge() + " years");
            setDetailValue(itemGender, patient.getGender() != null ? patient.getGender() : "Not Specified");
            setDetailValue(itemPhone, patient.getPhone() != null ? patient.getPhone() : "Not Available");
            setDetailValue(itemVillage, patient.getAddress() != null ? patient.getAddress() : "Not Available");
            setDetailValue(itemCategory, patient.getCategory() != null ? patient.getCategory() : "General");

            // Hide/Show actions based on category
            if (!"Pregnant Woman".equalsIgnoreCase(patient.getCategory())
                    && !"Pregnant".equalsIgnoreCase(patient.getCategory())) {
                actionPregnancyVisits.setVisibility(View.GONE);
            }
            if (!"Child".equalsIgnoreCase(patient.getCategory())
                    && !"Child (0-5 years)".equalsIgnoreCase(patient.getCategory())) {
                actionChildGrowth.setVisibility(View.GONE);
                actionVaccinations.setVisibility(View.GONE);
            }
        }
    }

    private void setDetailValue(View view, String value) {
        TextView tvValue = view.findViewById(R.id.tv_value);
        tvValue.setText(value);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        actionEditProfile.setOnClickListener(v -> {
            // Intent to Edit Activity (to be implemented or linked)
            Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
        });

        actionPregnancyVisits.setOnClickListener(v -> {
            // Navigate to Pregnancy Visits
            Toast.makeText(this, "Pregnancy Visits Clicked", Toast.LENGTH_SHORT).show();
        });

        actionChildGrowth.setOnClickListener(v -> {
            Toast.makeText(this, "Child Growth Clicked", Toast.LENGTH_SHORT).show();
        });

        actionVaccinations.setOnClickListener(v -> {
            Toast.makeText(this, "Vaccinations Clicked", Toast.LENGTH_SHORT).show();
        });

        actionNormalVisits.setOnClickListener(v -> {
            Toast.makeText(this, "Normal Visits Clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
