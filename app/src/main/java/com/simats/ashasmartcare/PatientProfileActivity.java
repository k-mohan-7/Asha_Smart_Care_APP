package com.simats.ashasmartcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.VolleyError;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;

import org.json.JSONObject;

public class PatientProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivEdit, ivDelete;
    private TextView tvInitial, tvName, tvAge, tvGender, tvCategory;
    private TextView tvPhone, tvAddress, tvVillage, tvDistrict, tvState;
    private TextView tvBloodGroup, tvEmergencyContact, tvMedicalHistory;
    private TextView tvRegistrationDate, tvSyncStatus;
    private LinearLayout layoutCall, layoutMessage;
    private CardView cardPregnancy, cardChildGrowth, cardVaccination, cardVisits;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private Patient patient;
    private long patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadPatientData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivEdit = findViewById(R.id.ivEdit);
        ivDelete = findViewById(R.id.ivDelete);

        tvInitial = findViewById(R.id.tvInitial);
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvCategory = findViewById(R.id.tvCategory);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvVillage = findViewById(R.id.tvVillage);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvState = findViewById(R.id.tvState);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        tvEmergencyContact = findViewById(R.id.tvEmergencyContact);
        tvMedicalHistory = findViewById(R.id.tvMedicalHistory);
        tvRegistrationDate = findViewById(R.id.tvRegistrationDate);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);

        layoutCall = findViewById(R.id.layoutCall);
        layoutMessage = findViewById(R.id.layoutMessage);

        cardPregnancy = findViewById(R.id.cardPregnancy);
        cardChildGrowth = findViewById(R.id.cardChildGrowth);
        cardVaccination = findViewById(R.id.cardVaccination);
        cardVisits = findViewById(R.id.cardVisits);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPatientActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        ivDelete.setOnClickListener(v -> showDeleteConfirmation());

        layoutCall.setOnClickListener(v -> {
            if (patient != null && patient.getPhone() != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + patient.getPhone()));
                startActivity(intent);
            }
        });

        layoutMessage.setOnClickListener(v -> {
            if (patient != null && patient.getPhone() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + patient.getPhone()));
                startActivity(intent);
            }
        });

        cardPregnancy.setOnClickListener(v -> {
            Intent intent = new Intent(this, PregnancyListActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        cardChildGrowth.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildGrowthListActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        cardVaccination.setOnClickListener(v -> {
            Intent intent = new Intent(this, VaccinationListActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        cardVisits.setOnClickListener(v -> {
            Intent intent = new Intent(this, VisitHistoryActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });
    }

    private void loadPatientData() {
        patient = dbHelper.getPatientById(patientId);
        if (patient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayPatientData();
    }

    private void displayPatientData() {
        // Set initial
        String name = patient.getName();
        if (name != null && !name.isEmpty()) {
            tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        tvName.setText(name);
        tvAge.setText(patient.getAge() + " years");
        tvGender.setText(patient.getGender());
        tvCategory.setText(patient.getCategory());
        tvPhone.setText(patient.getPhone());

        // Address
        String address = patient.getAddress();
        tvAddress.setText(address != null && !address.isEmpty() ? address : "Not provided");

        tvVillage.setText(patient.getVillage());
        tvDistrict.setText(patient.getDistrict() != null && !patient.getDistrict().isEmpty() 
                ? patient.getDistrict() : "Not provided");
        tvState.setText(patient.getState() != null && !patient.getState().isEmpty() 
                ? patient.getState() : "Not provided");

        // Medical info
        tvBloodGroup.setText(patient.getBloodGroup() != null && !patient.getBloodGroup().isEmpty() 
                ? patient.getBloodGroup() : "Unknown");
        tvEmergencyContact.setText(patient.getEmergencyContact() != null && !patient.getEmergencyContact().isEmpty() 
                ? patient.getEmergencyContact() : "Not provided");
        tvMedicalHistory.setText(patient.getMedicalHistory() != null && !patient.getMedicalHistory().isEmpty() 
                ? patient.getMedicalHistory() : "No medical history recorded");

        tvRegistrationDate.setText("Registered: " + patient.getRegistrationDate());

        // Sync status
        String syncStatus = patient.getSyncStatus();
        if ("SYNCED".equals(syncStatus)) {
            tvSyncStatus.setText("✓ Synced");
            tvSyncStatus.setTextColor(getResources().getColor(R.color.success));
        } else if ("PENDING".equals(syncStatus)) {
            tvSyncStatus.setText("⏳ Pending Sync");
            tvSyncStatus.setTextColor(getResources().getColor(R.color.warning));
        } else {
            tvSyncStatus.setText("✗ Sync Failed");
            tvSyncStatus.setTextColor(getResources().getColor(R.color.error));
        }

        // Show/hide category-specific cards
        String category = patient.getCategory();
        if ("Pregnant Woman".equals(category)) {
            cardPregnancy.setVisibility(View.VISIBLE);
            cardChildGrowth.setVisibility(View.GONE);
        } else if ("Lactating Mother".equals(category)) {
            cardPregnancy.setVisibility(View.VISIBLE);
            cardChildGrowth.setVisibility(View.VISIBLE);
        } else if ("Child (0-5 yrs)".equals(category)) {
            cardPregnancy.setVisibility(View.GONE);
            cardChildGrowth.setVisibility(View.VISIBLE);
        } else {
            cardPregnancy.setVisibility(View.GONE);
            cardChildGrowth.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete this patient? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Delete from server first
            apiHelper.deletePatient(patient.getServerId(), new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    deletePatientLocally();
                }

                @Override
                public void onError(String error) {
                    // Mark as deleted locally, will sync later
                    deletePatientLocally();
                }
            });
        } else {
            deletePatientLocally();
        }
    }

    private void deletePatientLocally() {
        int result = dbHelper.deletePatient(patientId);
        if (result > 0) {
            Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientData();
    }
}
