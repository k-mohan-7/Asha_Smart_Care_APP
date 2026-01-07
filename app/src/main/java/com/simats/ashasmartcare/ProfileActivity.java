package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.activities.LoginActivity;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivEdit, ivEditPhoto;
    private TextView tvName, tvRole, tvEmployeeId, tvDateOfBirth, tvGender;
    private TextView tvPhone, tvEmail, tvEmergencyName, tvEmergencyPhone;
    private TextView tvLocation, tvHouseholds, tvSupervisor;
    private Button btnLogout;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivEdit = findViewById(R.id.ivEdit);
        ivEditPhoto = findViewById(R.id.ivEditPhoto);
        
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        tvEmployeeId = findViewById(R.id.tvEmployeeId);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvGender = findViewById(R.id.tvGender);
        
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvEmergencyName = findViewById(R.id.tvEmergencyName);
        tvEmergencyPhone = findViewById(R.id.tvEmergencyPhone);
        
        tvLocation = findViewById(R.id.tvLocation);
        tvHouseholds = findViewById(R.id.tvHouseholds);
        tvSupervisor = findViewById(R.id.tvSupervisor);
        
        btnLogout = findViewById(R.id.btnLogout);

        sessionManager = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        ivEditPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadData() {
        // Load basic info from SessionManager
        tvName.setText(sessionManager.getUserName());
        tvEmployeeId.setText(String.valueOf(sessionManager.getUserId()));
        tvPhone.setText(sessionManager.getUserPhone());
        tvEmail.setText(sessionManager.getUserEmail());
        tvLocation.setText(sessionManager.getUserLocation());

        // Load additional profile data from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE);
        String dob = prefs.getString("dob", "12 Aug 1985");
        String gender = prefs.getString("gender", "Female");
        String emergencyName = prefs.getString("emergency_name", "Not Set");
        String emergencyPhone = prefs.getString("emergency_phone", "Not Set");
        String role = prefs.getString("role", "ASHA Worker - Zone 1");
        String households = prefs.getString("households", "0");
        String supervisor = prefs.getString("supervisor", "Not Assigned");

        tvDateOfBirth.setText(dob);
        tvGender.setText(gender);
        tvRole.setText(role);
        tvEmergencyName.setText(emergencyName);
        tvEmergencyPhone.setText(emergencyPhone.isEmpty() || emergencyPhone.equals("Not Set") ? "Not Set" : "+91 " + emergencyPhone);
        tvHouseholds.setText(households + " Families");
        tvSupervisor.setText(supervisor);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
