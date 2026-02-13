package com.simats.ashasmartcare.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.ashasmartcare.R;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_content);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);

        tvTitle.setText("Terms & Conditions");
        tvContent.setText("Terms of Service\n\n" +
                "1. Usage:\n" +
                "This app is intended for use by authorized ASHA workers and health officials only.\n\n" +
                "2. Responsibility:\n" +
                "Users are responsible for maintaining the confidentiality of their login credentials and the data they access.\n\n"
                +
                "3. Accuracy:\n" +
                "Users must ensure that the data entered is accurate and up-to-date to the best of their knowledge.\n\n"
                +
                "4. Termination:\n" +
                "Access to the app may be revoked if misuse is detected or upon termination of employment.\n\n" +
                "By using this app, you agree to these terms.");

        ivBack.setOnClickListener(v -> finish());
    }
}
