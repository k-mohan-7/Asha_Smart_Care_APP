package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.Constants;
import com.simats.ashasmartcare.utils.NetworkUtils;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Register Activity
 * Handles new user registration with online/offline support
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private TextInputLayout tilState, tilDistrict, tilArea;
    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextInputEditText etDistrict, etArea;
    private AutoCompleteTextView spinnerState;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ApiHelper apiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initHelpers();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilState = findViewById(R.id.til_state);
        tilDistrict = findViewById(R.id.til_district);
        tilArea = findViewById(R.id.til_area);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        spinnerState = findViewById(R.id.spinner_state);
        etDistrict = findViewById(R.id.et_district);
        etArea = findViewById(R.id.et_area);

        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initHelpers() {
        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
    }

    private void setupSpinners() {
        // Setup state spinner
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Constants.INDIAN_STATES
        );
        spinnerState.setAdapter(stateAdapter);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndRegister();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void validateAndRegister() {
        // Reset errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilState.setError(null);
        tilDistrict.setError(null);
        tilArea.setError(null);

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String state = spinnerState.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String area = etArea.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            etName.requestFocus();
            return;
        }

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

        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            tilPassword.setError("Password must be at least 4 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(state)) {
            tilState.setError("State is required");
            spinnerState.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(district)) {
            tilDistrict.setError("District is required");
            etDistrict.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(area)) {
            tilArea.setError("Area is required");
            etArea.requestFocus();
            return;
        }

        // Generate worker ID
        String workerId = "ASHA" + System.currentTimeMillis() % 100000;

        // Perform registration
        performRegistration(name, email, phone, password, workerId, state, district, area);
    }

    private void performRegistration(String name, String email, String phone, String password,
                                     String workerId, String state, String district, String area) {
        showLoading(true);

        // Always save locally first
        long localId = dbHelper.insertUser(name, email, phone, password, workerId, state, district, area);

        if (localId > 0) {
            // Check if network is available
            if (NetworkUtils.isNetworkAvailable(this)) {
                // Register online
                registerOnline(name, email, phone, password, workerId, state, district, area, localId);
            } else {
                // Offline registration success
                showLoading(false);
                Toast.makeText(this, "Account created locally. Will sync when online.", Toast.LENGTH_LONG).show();
                
                // Create session and navigate to login
                navigateToLogin();
            }
        } else {
            showLoading(false);
            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerOnline(String name, String email, String phone, String password,
                               String workerId, String state, String district, String area, long localId) {
        apiHelper.register(name, email, phone, password, workerId, state, district, area,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        showLoading(false);
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            } else {
                                String message = response.optString("message", "Registration failed");
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegisterActivity.this, "Account created locally", Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, 
                                "Account saved locally. Online sync failed: " + errorMessage, 
                                Toast.LENGTH_LONG).show();
                        navigateToLogin();
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
