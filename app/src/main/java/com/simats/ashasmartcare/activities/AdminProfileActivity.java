package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.utils.SessionManager;

public class AdminProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private View cardEditProfile, btnLogout;
    private TextView tvAdminName, tvAdminRole, tvAdminEmail, tvAdminPhone;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_admin_profile);

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminRole = findViewById(R.id.tvAdminRole);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminPhone = findViewById(R.id.tvAdminPhone);
        btnLogout = findViewById(R.id.btnLogout);

        sessionManager = SessionManager.getInstance(this);
    }

    private void loadData() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name != null && !name.isEmpty())
            tvAdminName.setText(name);
        if (email != null && !email.isEmpty())
            tvAdminEmail.setText(email);

        // Static data from design or session if added later
        tvAdminRole.setText("PHC Supervisor");
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditAdminProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
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
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
