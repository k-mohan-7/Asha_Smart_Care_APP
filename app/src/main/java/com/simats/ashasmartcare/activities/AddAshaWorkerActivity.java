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

import java.util.ArrayList;
import java.util.List;

public class AddAshaWorkerActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etFullName, etAge, etMobile, etPassword, etConfirmPassword;
    private TextView btnFemale, btnMale, btnOther;
    private Spinner spinnerVillage, spinnerPhc;
    private Button btnRegister;

    private String selectedGender = "Female";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asha_worker);

        // Set light status bar
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

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
    }

    private void setupSpinners() {
        // Sample data for villages
        List<String> villages = new ArrayList<>();
        villages.add("Select village");
        villages.add("Village A");
        villages.add("Village B");
        villages.add("Village C");

        ArrayAdapter<String> villageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, villages);
        villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVillage.setAdapter(villageAdapter);

        // Sample data for PHCs
        List<String> phcs = new ArrayList<>();
        phcs.add("Select PHC / Block");
        phcs.add("PHC Alpha");
        phcs.add("PHC Beta");
        phcs.add("PHC Gamma");

        ArrayAdapter<String> phcAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, phcs);
        phcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhc.setAdapter(phcAdapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnFemale.setOnClickListener(v -> selectGender("Female"));
        btnMale.setOnClickListener(v -> selectGender("Male"));
        btnOther.setOnClickListener(v -> selectGender("Other"));

        btnRegister.setOnClickListener(v -> validateAndRegister());
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

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
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

        long id = com.simats.ashasmartcare.database.DatabaseHelper.getInstance(this).insertWorker(worker);

        if (id > 0) {
            Toast.makeText(this, "ASHA Worker Registered Successfully", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}
