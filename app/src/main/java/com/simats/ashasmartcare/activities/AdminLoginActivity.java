package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.BuildConfig;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Admin Login Activity
 */
public class AdminLoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ImageView ivTogglePassword;
    private ProgressBar progressBar;
    private TextView tvVersion;
    private boolean isPasswordVisible = false;

    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_admin_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        progressBar = findViewById(R.id.progress_bar);
        tvVersion = findViewById(R.id.tv_version);

        apiHelper = ApiHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        String versionName = BuildConfig.VERSION_NAME;
        tvVersion.setText("v" + (versionName != null ? versionName : "1.0.0") + " • ASHA SmartCare Systems");
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });

        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
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
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Admin ID is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        performLogin(username, password);
    }

    private void performLogin(String username, String password) {
        showLoading(true);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showLoading(false);
            Toast.makeText(AdminLoginActivity.this, "Network unavailable. Admin login requires internet.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Use actual API to login
        apiHelper.login(username, password, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                showLoading(false);
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        JSONObject user = response.has("data") ? response.getJSONObject("data")
                                : response.getJSONObject("user");

                        String role = user.optString("role", "");
                        
                        // Verify this is actually an admin
                        if (!"admin".equals(role)) {
                            Toast.makeText(AdminLoginActivity.this, 
                                "Access Denied: This account is not an admin account", 
                                Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Save admin session
                        sessionManager.createLoginSession(
                                user.optLong("id", 0),
                                user.optString("name", ""),
                                user.optString("phone", username),
                                user.optString("email", ""),
                                user.optString("asha_id", ""),
                                user.optString("state", ""),
                                user.optString("district", ""),
                                user.optString("village", ""),
                                user.optString("role", "admin")
                        );

                        Toast.makeText(AdminLoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();

                        // Navigate to Admin Dashboard
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("IS_ADMIN", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = response.optString("message", "Login failed");
                        Toast.makeText(AdminLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(AdminLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(AdminLoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
    }
}
