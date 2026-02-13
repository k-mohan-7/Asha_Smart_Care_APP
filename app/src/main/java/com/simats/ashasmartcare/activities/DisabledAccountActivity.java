package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.R;

/**
 * Activity shown when user account has been disabled by admin
 */
public class DisabledAccountActivity extends AppCompatActivity {

    private Button btnBackToLogin;
    private TextView tvUserName, tvUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_account);

        initViews();
        loadUserInfo();
        setupListeners();
    }

    private void initViews() {
        btnBackToLogin = findViewById(R.id.btn_back_to_login);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserPhone = findViewById(R.id.tv_user_phone);
    }

    private void loadUserInfo() {
        // Get user info from intent
        String phone = getIntent().getStringExtra("phone");
        String name = getIntent().getStringExtra("name");

        if (phone != null && !phone.isEmpty()) {
            tvUserPhone.setText(phone);
        }

        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        }
    }

    private void setupListeners() {
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to login
                Intent intent = new Intent(DisabledAccountActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Override back press to go to login
        Intent intent = new Intent(DisabledAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
