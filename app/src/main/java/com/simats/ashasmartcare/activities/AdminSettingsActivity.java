package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.utils.SessionManager;

public class AdminSettingsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private View cardProfile, cardManageWorkers, layoutLogout;
    private View layoutFAQs, layoutGuide, layoutPrivacy, layoutTerms;
    private View navDashboard, navPatients, navAlerts;
    private androidx.appcompat.widget.SwitchCompat switchSync;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_admin_settings);

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        cardProfile = findViewById(R.id.cardProfile);
        cardManageWorkers = findViewById(R.id.cardManageWorkers);
        layoutLogout = findViewById(R.id.layoutLogout);

        layoutFAQs = findViewById(R.id.layoutFAQs);
        layoutGuide = findViewById(R.id.layoutGuide);
        layoutPrivacy = findViewById(R.id.layoutPrivacy);
        layoutTerms = findViewById(R.id.layoutTerms);

        switchSync = findViewById(R.id.switchSync);

        navDashboard = findViewById(R.id.navDashboard);
        navPatients = findViewById(R.id.navPatients);
        navAlerts = findViewById(R.id.navAlerts);

        sessionManager = SessionManager.getInstance(this);
    }

    private void loadData() {
        // Load admin data from session if available
        // For now, names are set in XML but can be dynamically updated here
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        cardManageWorkers.setOnClickListener(v -> {
            // Navigate to ASHA workers list
            Intent intent = new Intent(this, com.simats.ashasmartcare.activities.AshaWorkersActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        layoutLogout.setOnClickListener(v -> showLogoutDialog());

        // Support and legal clicks
        layoutFAQs.setOnClickListener(v -> {
            startActivity(new Intent(this, FaqsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        layoutGuide.setOnClickListener(v -> {
            startActivity(new Intent(this, OfflineGuideActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        layoutPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        layoutTerms.setOnClickListener(v -> {
            startActivity(new Intent(this, TermsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Bottom Navigation
        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        navPatients.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminPatientsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        navAlerts.setOnClickListener(v -> {
            startActivity(new Intent(this, HighRiskAlertsActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of the Admin panel?")
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
