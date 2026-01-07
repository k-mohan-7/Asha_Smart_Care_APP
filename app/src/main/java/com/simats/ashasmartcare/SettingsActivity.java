package com.simats.ashasmartcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.ashasmartcare.activities.HomeActivity;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.services.SyncService;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    private View ivBack;
    private SwitchCompat switchNotifications;
    private TextView tvSyncTime;
    private Button btnSyncNow;
    private LinearLayout layoutFaqs, layoutOfflineGuide, layoutPrivacyPolicy, layoutTerms, layoutNotifications;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        initHelpers();
        loadPreferences();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        switchNotifications = findViewById(R.id.switchNotifications);
        tvSyncTime = findViewById(R.id.tvSyncTime);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        layoutFaqs = findViewById(R.id.layoutFaqs);
        layoutOfflineGuide = findViewById(R.id.layoutOfflineGuide);
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy);
        layoutTerms = findViewById(R.id.layoutTerms);
        layoutNotifications = findViewById(R.id.layoutNotifications);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void initHelpers() {
        sessionManager = SessionManager.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE);
    }

    private void loadPreferences() {
        // Load notification preference
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        // Load last sync time
        updateSyncTime();
    }

    private void updateSyncTime() {
        String lastSync = sessionManager.getLastSyncTimeFormatted();
        if (lastSync.equals("Never")) {
            tvSyncTime.setText("Not synced");
        } else {
            long lastSyncTime = sessionManager.getLastSyncTime();
            long diff = System.currentTimeMillis() - lastSyncTime;
            long minutes = diff / (60 * 1000);
            
            if (minutes < 1) {
                tvSyncTime.setText("Just now");
            } else if (minutes < 60) {
                tvSyncTime.setText(minutes + " min" + (minutes > 1 ? "s" : "") + " ago");
            } else {
                long hours = minutes / 60;
                if (hours < 24) {
                    tvSyncTime.setText(hours + " hour" + (hours > 1 ? "s" : "") + " ago");
                } else {
                    tvSyncTime.setText(lastSync);
                }
            }
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        // Notification toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        // Sync Now button
        btnSyncNow.setOnClickListener(v -> performSync());

        // FAQs
        layoutFaqs.setOnClickListener(v -> {
            Toast.makeText(this, "FAQs - Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Offline Usage Guide
        layoutOfflineGuide.setOnClickListener(v -> {
            Toast.makeText(this, "Offline Usage Guide - Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Privacy Policy
        layoutPrivacyPolicy.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy Policy - Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Terms & Conditions
        layoutTerms.setOnClickListener(v -> {
            Toast.makeText(this, "Terms & Conditions - Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_visits) {
                    startActivity(new Intent(SettingsActivity.this, VisitHistoryActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_alerts) {
                    startActivity(new Intent(SettingsActivity.this, PatientAlertsActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void performSync() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please connect and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show sync status activity
        startActivity(new Intent(this, SyncStatusActivity.class));
        
        // Start sync service
        Intent syncIntent = new Intent(this, SyncService.class);
        startService(syncIntent);
        
        Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSyncTime();
    }
}
