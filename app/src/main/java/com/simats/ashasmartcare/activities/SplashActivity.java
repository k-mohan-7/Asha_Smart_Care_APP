package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.utils.SessionManager;

/**
 * Splash Screen Activity
 * Shows app logo and checks login status
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_splash);

        initViews();
        startAnimations();
        navigateAfterDelay();
    }

    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        tvAppName = findViewById(R.id.tv_app_name);
        tvTagline = findViewById(R.id.tv_tagline);
    }

    private void startAnimations() {
        // Fade in animation for logo
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        ivLogo.startAnimation(fadeIn);

        // Slide up animation for text
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(1000);
        slideUp.setStartOffset(500);
        tvAppName.startAnimation(slideUp);
        tvTagline.startAnimation(slideUp);
    }

    private void navigateAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is logged in
                SessionManager sessionManager = SessionManager.getInstance(SplashActivity.this);
                
                Intent intent;
                if (sessionManager.isLoggedIn()) {
                    // Go to Home
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    // Go to Login
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                
                startActivity(intent);
                finish();
                
                // Transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }
}
