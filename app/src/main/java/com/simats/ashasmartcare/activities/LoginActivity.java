package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Login Activity
 * Handles user authentication with online/offline support
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilPhone, tilPassword;
    private TextInputEditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvCreateAccount, tvForgotPassword;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initHelpers();
        setupListeners();
    }

    private void initViews() {
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvCreateAccount = findViewById(R.id.tv_create_account);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initHelpers() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Contact administrator to reset password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndLogin() {
        // Reset errors
        tilPhone.setError(null);
        tilPassword.setError(null);

        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            tilPhone.setError("Enter valid phone number");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            tilPassword.setError("Password too short");
            etPassword.requestFocus();
            return;
        }

        // Attempt login
        performLogin(phone, password);
    }

    private void performLogin(String phone, String password) {
        showLoading(true);

        // Check if network is available
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Online login
            loginOnline(phone, password);
        } else {
            // Offline login
            loginOffline(phone, password);
        }
    }

    private void loginOnline(String phone, String password) {
        apiHelper.login(phone, password, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                showLoading(false);
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        JSONObject user = response.getJSONObject("user");
                        
                        // Save user session
                        sessionManager.createLoginSession(
                                user.optLong("id", 0),
                                user.optString("name", ""),
                                user.optString("phone", phone),
                                user.optString("email", ""),
                                user.optString("worker_id", ""),
                                user.optString("state", ""),
                                user.optString("district", ""),
                                user.optString("area", "")
                        );

                        // Save to local DB for offline login
                        dbHelper.setUserLoggedIn(phone, true);

                        navigateToHome();
                    } else {
                        String message = response.optString("message", "Login failed");
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        
                        // Try offline login as fallback
                        loginOffline(phone, password);
                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    loginOffline(phone, password);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Server error: " + errorMessage + ". Trying offline login...", Toast.LENGTH_SHORT).show();
                loginOffline(phone, password);
            }
        });
    }

    private void loginOffline(String phone, String password) {
        showLoading(false);
        
        // Validate from local database
        boolean isValid = dbHelper.validateUser(phone, password);
        
        if (isValid) {
            // Set login status
            dbHelper.setUserLoggedIn(phone, true);
            
            // Create basic session (limited info in offline mode)
            sessionManager.createLoginSession(
                    0, // No server ID in offline mode
                    "ASHA Worker",
                    phone,
                    "",
                    "",
                    "",
                    "",
                    ""
            );
            
            Toast.makeText(this, "Logged in offline", Toast.LENGTH_SHORT).show();
            navigateToHome();
        } else {
            Toast.makeText(this, "Invalid credentials or no local account found", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etPhone.setEnabled(!show);
        etPassword.setEnabled(!show);
    }
}
