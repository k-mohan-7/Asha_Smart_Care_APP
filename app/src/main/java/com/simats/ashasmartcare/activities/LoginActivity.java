package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private EditText etPhone, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    private TextView tvCreateAccount, tvForgotPassword, tvHelp;
    private ImageView ivTogglePassword;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initHelpers();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        btnLogin = findViewById(R.id.btn_login);
        tvCreateAccount = findViewById(R.id.tv_create_account);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvHelp = findViewById(R.id.tv_help);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        progressBar = findViewById(R.id.progress_bar);

        // Auto-fill credentials if "Remember Me" is enabled
        if (sessionManager.isRememberMeEnabled()) {
            etPhone.setText(sessionManager.getSavedPhone());
            etPassword.setText(sessionManager.getSavedPassword());
            cbRememberMe.setChecked(true);
        }
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
                Toast.makeText(LoginActivity.this, "Contact administrator to reset password", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        if (tvHelp != null) {
            tvHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(LoginActivity.this, "Help & Support", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (ivTogglePassword != null) {
            ivTogglePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePasswordVisibility();
                }
            });
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_visibility);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void validateAndLogin() {
        String phoneOrWorkerId = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(phoneOrWorkerId)) {
            etPhone.setError("Phone number or Worker ID is required");
            etPhone.requestFocus();
            return;
        }

        if (phoneOrWorkerId.length() < 4) {
            etPhone.setError("Enter valid phone number or Worker ID");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            etPassword.setError("Password too short");
            etPassword.requestFocus();
            return;
        }

        // Attempt login
        performLogin(phoneOrWorkerId, password);
    }

    private void performLogin(String phoneOrWorkerId, String password) {
        showLoading(true);

        // Check if network is available
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Online login
            loginOnline(phoneOrWorkerId, password);
        } else {
            // Offline login
            loginOffline(phoneOrWorkerId, password);
        }
    }

    private void loginOnline(String phoneOrWorkerId, String password) {
        apiHelper.login(phoneOrWorkerId, password, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                showLoading(false);
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        // API returns user data in "data" field, not "user"
                        JSONObject user = response.has("data") ? response.getJSONObject("data")
                                : response.getJSONObject("user");

                        // Check worker status - only active/approved can log in
                        String status = user.optString("status", user.optString("account_status", "pending"));
                        
                        // Check if account is disabled
                        if ("disabled".equalsIgnoreCase(status) || "deactivated".equalsIgnoreCase(status)) {
                            showLoading(false);
                            Intent intent = new Intent(LoginActivity.this, DisabledAccountActivity.class);
                            intent.putExtra("phone", phoneOrWorkerId);
                            intent.putExtra("name", user.optString("name", ""));
                            startActivity(intent);
                            return;
                        }
                        
                        // Check if account is pending or rejected
                        if (!"active".equalsIgnoreCase(status) && !"approved".equalsIgnoreCase(status)) {
                            // Account pending approval or rejected
                            showLoading(false);
                            Intent intent = new Intent(LoginActivity.this, PendingApprovalActivity.class);
                            intent.putExtra("phone", phoneOrWorkerId);
                            intent.putExtra("name", user.optString("name", ""));
                            startActivity(intent);
                            return;
                        }

                        // Save user session with server data including role
                        sessionManager.createLoginSession(
                                user.optLong("id", 0),
                                user.optString("name", ""),
                                user.optString("phone", phoneOrWorkerId),
                                user.optString("email", ""),
                                user.optString("asha_id", ""), // Use asha_id as worker_id
                                user.optString("state", ""),
                                user.optString("district", ""),
                                user.optString("village", ""), // Use village as area
                                user.optString("role", "worker") // Get role from server
                        );

                        // Save or clear remembered credentials
                        sessionManager.setRememberMe(cbRememberMe.isChecked(), phoneOrWorkerId, password);

                        // Save to local DB for offline login capability
                        // Update status in local DB too
                        dbHelper.updateUserStatus(phoneOrWorkerId, status);
                        dbHelper.setUserLoggedIn(phoneOrWorkerId, true);

                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        String message = response.optString("message", "Login failed");
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                        // Try offline login as fallback
                        loginOffline(phoneOrWorkerId, password);
                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    loginOffline(phoneOrWorkerId, password);
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);

                // Check if error is about pending admin approval
                if (errorMessage != null &&
                        (errorMessage.toLowerCase().contains("pending") &&
                                errorMessage.toLowerCase().contains("approval"))) {
                    // Navigate to Pending Approval page
                    Intent intent = new Intent(LoginActivity.this, PendingApprovalActivity.class);
                    intent.putExtra("phone", phoneOrWorkerId);
                    intent.putExtra("name", ""); // Name not available yet
                    startActivity(intent);                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);                    return;
                }

                // For other errors, show error message and try offline
                String attemptedUrl = sessionManager.getApiBaseUrl();
                Toast.makeText(LoginActivity.this,
                        "Login failed: " + errorMessage + "\nTrying offline...",
                        Toast.LENGTH_LONG).show();
                loginOffline(phoneOrWorkerId, password);
            }
        });
    }

    private void loginOffline(String phoneOrWorkerId, String password) {
        showLoading(false);

        // Validate from local database (supports both phone and worker_id)
        boolean isValid = dbHelper.validateUser(phoneOrWorkerId, password);

        if (isValid) {
            // Set login status
            dbHelper.setUserLoggedIn(phoneOrWorkerId, true);

            // Create basic session (limited info in offline mode)
            sessionManager.createLoginSession(
                    0, // No server ID in offline mode
                    "ASHA Worker",
                    phoneOrWorkerId,
                    "",
                    "",
                    "",
                    "",
                    "");

            // Save or clear remembered credentials
            sessionManager.setRememberMe(cbRememberMe.isChecked(), phoneOrWorkerId, password);

            Toast.makeText(this, "Logged in offline", Toast.LENGTH_SHORT).show();
            navigateToHome();
        } else {
            Toast.makeText(this, "Invalid credentials or no local account found", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToHome() {
        // Check user role and navigate to appropriate dashboard
        Intent intent;
        if (sessionManager.isAdmin()) {
            // Admin user - go to Admin Dashboard
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else {
            // Worker user - go to Worker Home
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        }
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
