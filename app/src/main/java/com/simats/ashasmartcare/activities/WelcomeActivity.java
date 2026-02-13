package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.BuildConfig;
import com.simats.ashasmartcare.R;

public class WelcomeActivity extends AppCompatActivity {

    private CardView cardWorkerLogin;
    private CardView cardAdminLogin;
    private TextView tvVersion;
    private com.simats.ashasmartcare.utils.SessionManager sessionManager; // Add field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = com.simats.ashasmartcare.utils.SessionManager.getInstance(this); // Init

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_welcome);

        initViews();
        setupListeners();
        setupSecretSettings(); // Call setup

        // Check login status
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void initViews() {
        cardWorkerLogin = findViewById(R.id.card_worker_login);
        cardAdminLogin = findViewById(R.id.card_admin_login);
        tvVersion = findViewById(R.id.tv_version);

        // Set version
        String versionName = BuildConfig.VERSION_NAME;
        tvVersion.setText("Version " + (versionName != null ? versionName : "1.0.0"));
    }

    private void setupListeners() {
        cardWorkerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        cardAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupSecretSettings() {
        tvVersion.setOnLongClickListener(v -> {
            showServerIpDialog();
            return true;
        });
    }

    private void showServerIpDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(sessionManager.getApiBaseUrl());
        input.setHint("http://10.203.210.63/asha_api/");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Configure Server URL")
                .setMessage("Enter the full base URL for the API:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        if (!url.endsWith("/")) {
                            url += "/";
                        }
                        sessionManager.setApiBaseUrl(url);
                        android.widget.Toast.makeText(this, "Server URL Updated", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset to Default", (dialog, which) -> {
                    sessionManager.setApiBaseUrl(com.simats.ashasmartcare.utils.Constants.API_BASE_URL);
                    android.widget.Toast.makeText(this, "Reset to Default URL", android.widget.Toast.LENGTH_SHORT)
                            .show();
                })
                .show();
    }
}
