package com.simats.ashasmartcare.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditAdminProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivProfilePicture;
    private android.view.View btnSave, tvChangePhoto;
    private EditText etName, etEmail, etRole, etPhone, etPhc;
    private EditText etNewPassword, etConfirmPassword;
    private SessionManager sessionManager;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_edit_admin_profile);

        initViews();
        loadData();
        setupListeners();
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

            File file = new File(directory, "admin_profile.jpg");
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
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnSave = findViewById(R.id.btnSave);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);

        etName = findViewById(R.id.etName);
        etRole = findViewById(R.id.etRole);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPhc = findViewById(R.id.etPhc);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        sessionManager = SessionManager.getInstance(this);
    }

    private void loadData() {
        etName.setText(sessionManager.getUserName());
        etEmail.setText(sessionManager.getUserEmail());

        // Load other data if available in session, otherwise use defaults from design
        String phone = sessionManager.getString("userPhone", "+91 98765 43210");
        String role = sessionManager.getString("userRole", "Senior Administrator");
        String phc = sessionManager.getString("userPhc", "Community Health Center - North");

        etPhone.setText(phone);
        etRole.setText(role);
        etPhc.setText(phc);

        loadProfileImage(sessionManager.getProfileImage());
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        tvChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String role = etRole.getText().toString().trim();
            String phc = etPhc.getText().toString().trim();
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Password validation
            if (!newPass.isEmpty()) {
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                // In a real app, we would update the password here
            }

            // Save to session
            sessionManager.saveString("userName", name);
            sessionManager.saveString("userEmail", email);
            sessionManager.saveString("userPhone", phone);
            sessionManager.saveString("userRole", role);
            sessionManager.saveString("userPhc", phc);

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
