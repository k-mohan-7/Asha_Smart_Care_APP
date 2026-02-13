package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddAshaWorkerActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etFullName, etAge, etMobile, etPassword, etConfirmPassword;
    private TextView btnFemale, btnMale, btnOther;
    private Spinner spinnerVillage, spinnerPhc;
    private Button btnRegister;

    private String selectedGender = "Female";
    private ApiHelper apiHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asha_worker);

        // Set light status bar
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        // Initialize helpers
        sessionManager = SessionManager.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);

        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        btnFemale = findViewById(R.id.btnFemale);
        btnMale = findViewById(R.id.btnMale);
        btnOther = findViewById(R.id.btnOther);
        spinnerVillage = findViewById(R.id.spinnerVillage);
        spinnerPhc = findViewById(R.id.spinnerPhc);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Restrict name input to alphabets and spaces only
        etFullName.setFilters(new android.text.InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Point 2: Restrict mobile input to 10 digits only
        etMobile.setFilters(new android.text.InputFilter[] {
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

        // Restrict Age to numbers 1-100 only
        etAge.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(3),
                (source, start, end, dest, dstart, dend) -> {
                    String newVal = dest.toString().substring(0, dstart) + source.toString().substring(start, end)
                            + dest.toString().substring(dend);
                    if (newVal.isEmpty())
                        return null;
                    try {
                        int age = Integer.parseInt(newVal);
                        if (age >= 1 && age <= 100)
                            return null;
                    } catch (NumberFormatException ignored) {
                    }
                    return "";
                }
        });
    }

    private void setupSpinners() {
        // Sample data for villages
        List<String> villages = new ArrayList<>();
        villages.add("Select village");
        villages.add("Village A");
        villages.add("Village B");
        villages.add("Village C");

        ArrayAdapter<String> villageAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, villages);
        villageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerVillage.setAdapter(villageAdapter);

        // Sample data for PHCs
        List<String> phcs = new ArrayList<>();
        phcs.add("Select PHC / Block");
        phcs.add("PHC Alpha");
        phcs.add("PHC Beta");
        phcs.add("PHC Gamma");

        ArrayAdapter<String> phcAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, phcs);
        phcAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerPhc.setAdapter(phcAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnFemale.setOnClickListener(v -> selectGender("Female"));
        btnMale.setOnClickListener(v -> selectGender("Male"));
        btnOther.setOnClickListener(v -> selectGender("Other"));

        btnRegister.setOnClickListener(v -> validateAndRegister());

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

    private void selectGender(String gender) {
        selectedGender = gender;

        // Reset all
        btnFemale.setBackgroundResource(R.drawable.bg_gender_unselected);
        btnMale.setBackgroundResource(R.drawable.bg_gender_unselected);
        btnOther.setBackgroundResource(R.drawable.bg_gender_unselected);

        // Select one
        if (gender.equals("Female")) {
            btnFemale.setBackgroundResource(R.drawable.bg_gender_selected);
        } else if (gender.equals("Male")) {
            btnMale.setBackgroundResource(R.drawable.bg_gender_selected);
        } else {
            btnOther.setBackgroundResource(R.drawable.bg_gender_selected);
        }
    }

    private void validateAndRegister() {
        String name = etFullName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String village = spinnerVillage.getSelectedItem().toString();
        String phc = spinnerPhc.getSelectedItem().toString();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (village.equals("Select village") || phc.equals("Select PHC / Block")) {
            Toast.makeText(this, "Please select village and PHC", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.length() < 10) {
            etMobile.setError("Enter valid 10-digit mobile number");
            etMobile.requestFocus();
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

        // Registration Logic
        com.simats.ashasmartcare.models.Worker worker = new com.simats.ashasmartcare.models.Worker();
        worker.setName(name);
        worker.setAge(Integer.parseInt(age));
        worker.setGender(selectedGender);
        worker.setVillage(village);
        worker.setPhc(phc);
        worker.setPhone(mobile);
        worker.setPassword(password);
        worker.setWorkerId("ASHA-" + (1000 + new java.util.Random().nextInt(9000))); // Generate random ID
        worker.setStatus("active");

        // Save to local database
        long id = com.simats.ashasmartcare.database.DatabaseHelper.getInstance(this).insertWorker(worker);

        if (id > 0) {
            // Local save successful, now sync to backend
            syncWorkerToBackend(name, mobile, password, worker.getWorkerId(), village, phc);
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void syncWorkerToBackend(String name, String phone, String password, 
                                     String workerId, String village, String phc) {
        // Register worker on backend with admin role to get approved status
        apiHelper.registerAdminCreatedWorker(name, phone, password, workerId, village, phc,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                Toast.makeText(AddAshaWorkerActivity.this, 
                                    "ASHA Worker Registered Successfully", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                String message = response.optString("message", "Backend sync failed");
                                Toast.makeText(AddAshaWorkerActivity.this, 
                                    "Local save successful, but backend sync failed: " + message, 
                                    Toast.LENGTH_LONG).show();
                                // Still finish as local save was successful
                                finish();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(AddAshaWorkerActivity.this, 
                                "Worker saved locally. Backend sync error: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(AddAshaWorkerActivity.this, 
                            "Worker saved locally. Backend sync failed: " + errorMessage, 
                            Toast.LENGTH_LONG).show();
                        // Still finish as local save was successful
                        finish();
                    }
                });
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
