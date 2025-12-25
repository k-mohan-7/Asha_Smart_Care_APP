package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.activities.HomeActivity;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private ApiHelper apiHelper;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_create_account);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                // Navigate to register activity
                try {
                    Class<?> registerClass = Class.forName("com.simats.ashasmartcare.activities.RegisterActivity");
                    Intent intent = new Intent(this, registerClass);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "Registration not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Toast.makeText(this, "Please contact admin for password reset", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void performLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (phone.isEmpty()) {
            etPhone.setError("Phone number required");
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etPhone.setError("Enter valid phone number");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        if (NetworkUtils.isNetworkAvailable(this)) {
            // Online login
            apiHelper.login(phone, password, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.getBoolean("success")) {
                            // PHP returns user data in "data" field
                            JSONObject user = response.getJSONObject("data");
                            
                            long userId = user.getLong("id");
                            String name = user.getString("name");
                            String email = user.optString("email", "");
                            String ashaId = user.optString("asha_id", "");
                            String state = user.optString("state", "");
                            String district = user.optString("district", "");
                            String village = user.optString("village", "");

                            sessionManager.createLoginSession(
                                    userId, name, phone, email,
                                    ashaId, state, district, village
                            );

                            showLoading(false);
                            navigateToHome();
                        } else {
                            showLoading(false);
                            String message = response.optString("message", "Login failed");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    // Try offline login
                    tryOfflineLogin(phone, password);
                }
            });
        } else {
            // Offline login
            tryOfflineLogin(phone, password);
        }
    }

    private void tryOfflineLogin(String phone, String password) {
        if (dbHelper.validateUser(phone, password)) {
            // Get user details from local DB
            dbHelper.setUserLoggedIn(phone, true);
            
            // For offline, we use minimal session data
            sessionManager.createLoginSession(
                    0, "ASHA Worker", phone, "",
                    "", "", "", ""
            );
            
            showLoading(false);
            navigateToHome();
        } else {
            showLoading(false);
            Toast.makeText(this, "Invalid credentials or no offline data available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!show);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
