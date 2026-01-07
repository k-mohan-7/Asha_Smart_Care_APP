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
 * Register Activity
 * Handles new user registration with online/offline support
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPhone, etWorkerId, etVillage, etPhc, etPassword, etConfirmPassword;
    private ImageView ivBack, ivTogglePassword, ivToggleConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initHelpers();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etWorkerId = findViewById(R.id.et_worker_id);
        etVillage = findViewById(R.id.et_village);
        etPhc = findViewById(R.id.et_phc);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        ivBack = findViewById(R.id.iv_back);
        ivTogglePassword = findViewById(R.id.iv_toggle_password);
        ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);

        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initHelpers() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
    }

    private void setupListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        btnRegister.setOnClickListener(v -> validateAndRegister());

        tvLogin.setOnClickListener(v -> finish());

        if (ivTogglePassword != null) {
            ivTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivTogglePassword.setImageResource(R.drawable.ic_visibility);
                }
                isPasswordVisible = !isPasswordVisible;
                etPassword.setSelection(etPassword.getText().length());
            });
        }

        if (ivToggleConfirmPassword != null) {
            ivToggleConfirmPassword.setOnClickListener(v -> {
                if (isConfirmPasswordVisible) {
                    etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
                } else {
                    etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
                }
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
                etConfirmPassword.setSelection(etConfirmPassword.getText().length());
            });
        }
    }

    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String workerId = etWorkerId.getText().toString().trim();
        String village = etVillage.getText().toString().trim();
        String phc = etPhc.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etPhone.setError("Enter valid phone number");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(workerId)) {
            etWorkerId.setError("Worker ID is required");
            etWorkerId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(village)) {
            etVillage.setError("Village is required");
            etVillage.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phc)) {
            etPhc.setError("PHC / Block name is required");
            etPhc.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to Terms of Service", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform registration
        performRegistration(name, "", phone, password, workerId, "", phc, village);
    }

    private void performRegistration(String name, String email, String phone, String password,
                                     String workerId, String state, String district, String area) {
        showLoading(true);

        // ONLINE-FIRST: Try API registration first
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Register online directly
            registerOnline(name, email, phone, password, workerId, state, district, area);
        } else {
            // OFFLINE FALLBACK: Save locally only if no internet
            long localId = dbHelper.insertUser(name, email, phone, password, workerId, state, district, area);
            showLoading(false);
            if (localId > 0) {
                Toast.makeText(this, "No internet. Account saved locally. Will sync when online.", Toast.LENGTH_LONG).show();
                navigateToLogin();
            } else {
                Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerOnline(String name, String email, String phone, String password,
                               String workerId, String state, String district, String area) {
        apiHelper.register(name, email, phone, password, workerId, state, district, area,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        showLoading(false);
                        try {
                            boolean success = response.getBoolean("success");
                            String message = response.optString("message", "");
                            
                            if (success) {
                                // Online registration successful - data stored in server
                                if (message.contains("pending") || message.contains("approval")) {
                                    Toast.makeText(RegisterActivity.this, 
                                            "✓ Registered online. Please wait for admin approval.", 
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, 
                                            "✓ Account created online successfully!", 
                                            Toast.LENGTH_SHORT).show();
                                }
                                navigateToLogin();
                            } else {
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegisterActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showLoading(false);
                        // Network failed - fallback to local storage
                        if (errorMessage.contains("409")) {
                            Toast.makeText(RegisterActivity.this, 
                                    "Phone number already registered. Please login.", 
                                    Toast.LENGTH_LONG).show();
                            navigateToLogin();
                        } else {
                            // Save locally as fallback
                            long localId = dbHelper.insertUser(name, email, phone, password, workerId, state, district, area);
                            if (localId > 0) {
                                Toast.makeText(RegisterActivity.this, 
                                        "Network error. Saved locally. Will sync when online.", 
                                        Toast.LENGTH_LONG).show();
                                navigateToLogin();
                            } else {
                                Toast.makeText(RegisterActivity.this, 
                                        "Registration failed: " + errorMessage, 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void navigateToLogin() {
        Toast.makeText(this, "Please login with your credentials", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}
