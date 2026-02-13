package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.ashasmartcare.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_content);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);

        tvTitle.setText("Privacy Policy");
        tvContent.setText("Privacy Policy for ASHA SmartCare\n\n" +
                "1. Information Collection:\n" +
                "We collect personal health information of patients solely for the purpose of healthcare delivery and monitoring.\n\n"
                +
                "2. Data Security:\n" +
                "All data is encrypted and stored securely. We adhere to strict data protection standards.\n\n" +
                "3. Data Sharing:\n" +
                "Data is shared only with authorized health officials and supervisors within the government health department framework.\n\n"
                +
                "4. User Rights:\n" +
                "Patients have the right to access and correct their data. Please contact your supervisor for any data correction requests.\n\n"
                +
                "Last Updated: Jan 2026");

        ivBack.setOnClickListener(v -> finish());
    }
}
