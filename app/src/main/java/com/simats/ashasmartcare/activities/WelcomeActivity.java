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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_welcome);

        initViews();
        setupListeners();
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
}
