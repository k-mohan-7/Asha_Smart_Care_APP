package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.activities.LoginActivity;
import com.simats.ashasmartcare.activities.BaseActivity;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.utils.SessionManager;

public class ProfileActivity extends BaseActivity {

    @Override
    protected int getNavItemId() {
        return R.id.nav_profile;
    }

    private ImageView ivBack, ivEdit, ivEditPhoto, ivProfilePicture;
    private TextView tvName, tvRole, tvEmployeeId, tvDateOfBirth, tvGender;
    private TextView tvPhone, tvPatientsManaged;
    private TextView tvLocation;
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
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        tvEmployeeId = findViewById(R.id.tvEmployeeId);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvGender = findViewById(R.id.tvGender);

        tvPhone = findViewById(R.id.tvPhone);
        tvLocation = findViewById(R.id.tvLocation);
        tvPatientsManaged = findViewById(R.id.tvPatientsManaged);

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
        tvLocation.setText(sessionManager.getUserLocation());

        // Load additional profile data from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE);
        String dob = prefs.getString("dob", "12 Aug 1985");
        String gender = prefs.getString("gender", "Female");
        String role = prefs.getString("role", "ASHA Worker - Zone 1");

        tvDateOfBirth.setText(dob);
        tvGender.setText(gender);
        tvRole.setText(role);

        // Load actual patient count from database
        int patientCount = dbHelper.getPatientCount();
        tvPatientsManaged.setText(patientCount + (patientCount == 1 ? " Patient" : " Patients"));

        // Load profile picture
        loadProfileImage(sessionManager.getProfileImage());
    }

    private void loadProfileImage(String path) {
        if (path != null && !path.isEmpty()) {
            java.io.File imgFile = new java.io.File(path);
            if (imgFile.exists()) {
                android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageBitmap(myBitmap);
                }
            }
        }
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
        Intent intent = new Intent(this, com.simats.ashasmartcare.activities.WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
