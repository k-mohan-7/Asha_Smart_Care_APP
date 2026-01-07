package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.simats.ashasmartcare.utils.SessionManager;

import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivEditPhoto;
    private TextView tvSave;
    private TextInputEditText etName, etEmployeeId, etDateOfBirth, etGender;
    private TextInputEditText etPhone, etEmail, etEmergencyName, etEmergencyPhone;
    private TextInputEditText etRole, etLocation, etHouseholds, etSupervisor;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivEditPhoto = findViewById(R.id.ivEditPhoto);
        tvSave = findViewById(R.id.tvSave);
        
        etName = findViewById(R.id.etName);
        etEmployeeId = findViewById(R.id.etEmployeeId);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etGender = findViewById(R.id.etGender);
        
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        
        etRole = findViewById(R.id.etRole);
        etLocation = findViewById(R.id.etLocation);
        etHouseholds = findViewById(R.id.etHouseholds);
        etSupervisor = findViewById(R.id.etSupervisor);

        sessionManager = SessionManager.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        tvSave.setOnClickListener(v -> saveProfile());
        
        ivEditPhoto.setOnClickListener(v -> 
            Toast.makeText(this, "Photo upload feature coming soon", Toast.LENGTH_SHORT).show()
        );

        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        
        etGender.setOnClickListener(v -> showGenderDialog());
    }

    private void loadData() {
        // Load data from SessionManager
        etName.setText(sessionManager.getUserName());
        etEmployeeId.setText(String.valueOf(sessionManager.getUserId()));
        etPhone.setText(sessionManager.getUserPhone().replace("+91 ", ""));
        etEmail.setText(sessionManager.getUserEmail());
        etLocation.setText(sessionManager.getUserLocation());
        
        // Load other profile data from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE);
        etDateOfBirth.setText(prefs.getString("dob", ""));
        etGender.setText(prefs.getString("gender", ""));
        etEmergencyName.setText(prefs.getString("emergency_name", ""));
        etEmergencyPhone.setText(prefs.getString("emergency_phone", ""));
        etRole.setText(prefs.getString("role", ""));
        etHouseholds.setText(prefs.getString("households", ""));
        etSupervisor.setText(prefs.getString("supervisor", ""));
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SessionManager - update the current session with new data
        sessionManager.createLoginSession(
            sessionManager.getUserId(),
            name,
            "+91 " + phone,
            email,
            sessionManager.getWorkerId(),
            sessionManager.getUserState(),
            sessionManager.getUserDistrict(),
            location
        );

        // Save other profile data to SharedPreferences
        android.content.SharedPreferences.Editor editor = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE).edit();
        editor.putString("dob", etDateOfBirth.getText().toString());
        editor.putString("gender", etGender.getText().toString());
        editor.putString("emergency_name", etEmergencyName.getText().toString());
        editor.putString("emergency_phone", etEmergencyPhone.getText().toString());
        editor.putString("role", etRole.getText().toString());
        editor.putString("households", etHouseholds.getText().toString());
        editor.putString("supervisor", etSupervisor.getText().toString());
        editor.apply();

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                String date = selectedDay + " " + months[selectedMonth] + " " + selectedYear;
                etDateOfBirth.setText(date);
            },
            year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showGenderDialog() {
        String[] genders = {"Male", "Female", "Other"};
        new AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(genders, (dialog, which) -> etGender.setText(genders[which]))
            .show();
    }
}
