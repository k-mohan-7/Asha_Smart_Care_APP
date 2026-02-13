package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.ashasmartcare.R;

public class OfflineGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_content);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);

        tvTitle.setText("Offline Usage Guide");
        tvContent.setText("ASHA SmartCare is designed to work seamlessly offline.\n\n" +
                "1. Data Entry:\n" +
                "You can add patients, visits, and health records without an internet connection. The data is saved locally on your device.\n\n"
                +
                "2. Syncing:\n" +
                "When you connect to the internet (Audit/Wi-Fi), the app will attempt to sync your pending data to the server. You can check the sync status in Settings > Sync Status.\n\n"
                +
                "3. Conflicts:\n" +
                "If data was modified on the server while you were offline, the server version generally takes precedence, but your new records are always preserved.\n\n"
                +
                "4. Storage:\n" +
                "Please ensure your phone has enough storage space to keep local records.");

        ivBack.setOnClickListener(v -> finish());
    }
}
