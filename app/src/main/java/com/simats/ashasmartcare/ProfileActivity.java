package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.activities.LoginActivity;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivEdit;
    private TextView tvName, tvPhone, tvEmail, tvAshaId, tvLocation, tvPatientsCount;
    private Button btnLogout;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivEdit = findViewById(R.id.ivEdit);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvAshaId = findViewById(R.id.tvAshaId);
        tvLocation = findViewById(R.id.tvLocation);
        tvPatientsCount = findViewById(R.id.tvPatientsCount);
        btnLogout = findViewById(R.id.btnLogout);

        sessionManager = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        ivEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit profile feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadData() {
        tvName.setText(sessionManager.getUserName());
        tvPhone.setText(sessionManager.getUserPhone());
        tvEmail.setText(sessionManager.getUserEmail());
        tvAshaId.setText("ASHA ID: " + sessionManager.getUserId());
        tvLocation.setText(sessionManager.getUserLocation());

        int patientCount = dbHelper.getAllPatients().size();
        tvPatientsCount.setText(String.valueOf(patientCount));
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
