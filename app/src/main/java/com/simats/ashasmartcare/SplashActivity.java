package com.simats.ashasmartcare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.activities.HomeActivity;
import com.simats.ashasmartcare.utils.SessionManager;

/**
 * Splash screen activity displayed when the app starts
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = SessionManager.getInstance(this);

        // Find views
        ImageView ivLogo = findViewById(R.id.iv_logo);
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);

        // Apply animations if views exist
        if (ivLogo != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            fadeIn.setDuration(1000);
            ivLogo.startAnimation(fadeIn);
        }

        if (tvAppName != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideUp.setDuration(1000);
            tvAppName.startAnimation(slideUp);
        }

        if (tvTagline != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            fadeIn.setDuration(1500);
            tvTagline.startAnimation(fadeIn);
        }

        // Navigate to appropriate screen after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void navigateToNextScreen() {
        Intent intent;
        
        if (sessionManager.isLoggedIn()) {
            // User is logged in, go to home activity
            intent = new Intent(this, HomeActivity.class);
        } else {
            // User is not logged in, go to login screen
            intent = new Intent(this, LoginActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
