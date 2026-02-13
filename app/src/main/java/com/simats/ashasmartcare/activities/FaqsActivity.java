package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.ashasmartcare.R;

public class FaqsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_content);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);

        tvTitle.setText("Frequently Asked Questions");
        tvContent.setText("Q1: How do I sync data?\n" +
                "A: Go to Settings and click on 'Sync Now'. Ideally, data syncs automatically when internet is available.\n\n"
                +
                "Q2: How do I add a new patient?\n" +
                "A: Used the '+' button on the dashboard or go to Patients tab and click 'Add Patient'.\n\n" +
                "Q3: Can I use the app offline?\n" +
                "A: Yes, you can view and add data offline. It will sync when you are back online.\n\n" +
                "Q4: How do I change my password?\n" +
                "A: Feature coming soon in Profile settings.");

        ivBack.setOnClickListener(v -> finish());
    }
}
