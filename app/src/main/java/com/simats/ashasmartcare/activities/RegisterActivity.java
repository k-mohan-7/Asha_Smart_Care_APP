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

        // Restrict name input to alphabets and spaces only
        etName.setFilters(new android.text.InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Point 1: Restrict village input to alphabets only
        etVillage.setFilters(new android.text.InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Point 2: Restrict phone input to 10 digits only
        etPhone.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(10),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Point 5: Restrict worker ID to alphabets and numerical values only
        etWorkerId.setFilters(new android.text.InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetterOrDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Point 6: Restrict PHC / Block Name to alphabets and numerical values only
        etPhc.setFilters(new android.text.InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });
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

        // Point 4: Immediate confirm password match check
        etConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String pass = etPassword.getText().toString();
                String confirmPass = s.toString();
                if (!confirmPass.isEmpty() && !confirmPass.equals(pass)) {
                    etConfirmPassword.setError("Passwords do not match");
                } else {
                    etConfirmPassword.setError(null);
                }
            }
        });
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

        // Point 3: Password Complexity Validation
        if (!isPasswordValid(password)) {
            etPassword.setError("Password must be 8+ characters with uppercase, lowercase, digit, and special character");
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
                                // Get user info from response
                                String registeredPhone = phone;
                                String registeredName = name;

                                // Navigate to Pending Approval page instead of login
                                Intent intent = new Intent(RegisterActivity.this, PendingApprovalActivity.class);
                                intent.putExtra("phone", registeredPhone);
                                intent.putExtra("name", registeredName);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
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
                                "Account saved locally. Online sync failed to " + sessionManager.getApiBaseUrl() + "\n"
                                        + errorMessage,
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

    private boolean isPasswordValid(String password) {
        if (password.length() < 8)
            return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        String specialChars = "!@#$%^&*()-_=+[]{}|;:',.<>?/";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else if (specialChars.contains(String.valueOf(c)))
                hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
