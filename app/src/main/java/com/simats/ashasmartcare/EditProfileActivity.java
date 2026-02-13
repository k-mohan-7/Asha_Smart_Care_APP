package com.simats.ashasmartcare;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.InputFilter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.simats.ashasmartcare.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivEditPhoto, ivProfilePicture;
    private TextView tvSave;
    private TextInputEditText etName, etEmployeeId, etDateOfBirth, etPhone, etAddress;
    private androidx.appcompat.widget.AppCompatAutoCompleteTextView etGender, etAssignedVillage;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;

    private SessionManager sessionManager;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupListeners();
        loadData();
        registerImagePicker();
    }

    private void registerImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String path = saveImageToInternalStorage(uri);
                        if (path != null) {
                            sessionManager.saveProfileImage(path);
                            loadProfileImage(path);
                            Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            File directory = new File(getFilesDir(), "profile_images");
            if (!directory.exists())
                directory.mkdirs();

            File file = new File(directory, "worker_profile.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadProfileImage(String path) {
        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivProfilePicture.setImageBitmap(myBitmap);
            }
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivEditPhoto = findViewById(R.id.ivEditPhoto);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvSave = findViewById(R.id.tvSave);

        etName = findViewById(R.id.etName);
        etEmployeeId = findViewById(R.id.etEmployeeId);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etGender = findViewById(R.id.etGender);
        etPhone = findViewById(R.id.etPhone);

        etAddress = findViewById(R.id.etAddress);
        etAssignedVillage = findViewById(R.id.etAssignedVillage);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        sessionManager = SessionManager.getInstance(this);

        // Apply alphabet filter to Name
        etName.setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        tvSave.setOnClickListener(v -> saveProfile());

        ivEditPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Photo label click also triggers picker
        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        etDateOfBirth.setOnClickListener(v -> showDatePicker());

        // Disable editing for Restricted fields (Gender, Assigned Village)
        etGender.setFocusable(false);
        etGender.setClickable(false);
        etGender.setOnClickListener(null);

        etAssignedVillage.setFocusable(false);
        etAssignedVillage.setClickable(false);
        etAssignedVillage.setOnClickListener(null);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Navigate to Home
                Intent intent = new Intent(this, com.simats.ashasmartcare.activities.HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            // For other items, we can implement if needed, or just let them be selectable
            return false;
        });
    }

    private void loadData() {
        // Load data from SessionManager
        etName.setText(sessionManager.getUserName());
        etEmployeeId.setText(String.valueOf(sessionManager.getWorkerId())); // Using workerID for display
        etPhone.setText(sessionManager.getUserPhone().replace("+91 ", ""));

        // Map userLocation (Session) to Assigned Village
        etAssignedVillage.setText(sessionManager.getUserArea());

        // Load other profile data from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE);
        etDateOfBirth.setText(prefs.getString("dob", "12 Aug 1990"));
        etGender.setText("Female");

        // Address is a new field, load or default
        etAddress.setText(
                prefs.getString("address", "H.No 12/B, Near Panchayat Bhawan, Rampur Village, District Kanpur"));

        loadProfileImage(sessionManager.getProfileImage());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String village = etAssignedVillage.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || village.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SessionManager - preserve existing email/state/district
        sessionManager.createLoginSession(
                sessionManager.getUserId(),
                name,
                "+91 " + phone,
                sessionManager.getUserEmail(), // Preserve email
                sessionManager.getWorkerId(),
                sessionManager.getUserState(),
                sessionManager.getUserDistrict(),
                village); // Update location/village

        // Save other profile data to SharedPreferences
        android.content.SharedPreferences.Editor editor = getSharedPreferences("AshaHealthcarePrefs", MODE_PRIVATE)
                .edit();
        editor.putString("dob", etDateOfBirth.getText().toString());
        editor.putString("gender", etGender.getText().toString());
        editor.putString("address", etAddress.getText().toString());
        // We don't overwrite role/households/emergency here as they are not editable in
        // this view
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
                    String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
                    String date = selectedDay + " " + months[selectedMonth] + " " + selectedYear;
                    etDateOfBirth.setText(date);
                },
                year, month, day);
        // Age should be between 25 and 50
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -50);
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -25);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showGenderDialog() {
        // Option removed as per requirement: defaultly female
    }

    private void showVillageDialog() {
        // Option removed as per requirement: remove edit option
    }
}
