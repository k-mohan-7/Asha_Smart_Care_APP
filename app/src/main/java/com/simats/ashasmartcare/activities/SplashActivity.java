package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

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
    private TextView tvAppNameSmart;
    private TextView tvTagline;
    private View circleOuter;
    private View circleMiddle;
    private CardView cardLogo;
    private LinearLayout layoutAppName;
    private View progressSegment1;
    private View progressSegment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set light status bar for white background
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        setContentView(R.layout.activity_splash);

        initViews();
        startAnimations();
        navigateAfterDelay();
    }

    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppNameSmart = findViewById(R.id.tv_app_name_smart);
        tvTagline = findViewById(R.id.tv_tagline);
        circleOuter = findViewById(R.id.circle_outer);
        circleMiddle = findViewById(R.id.circle_middle);
        cardLogo = findViewById(R.id.card_logo);
        layoutAppName = findViewById(R.id.layout_app_name);
        progressSegment1 = findViewById(R.id.progress_segment_1);
        progressSegment2 = findViewById(R.id.progress_segment_2);
    }

    private void startAnimations() {
        // Scale animation for circles
        ScaleAnimation scaleIn = new ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleIn.setDuration(800);
        scaleIn.setFillAfter(true);

        // Fade in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(600);
        fadeIn.setFillAfter(true);

        // Start circle animations
        circleOuter.startAnimation(scaleIn);

        ScaleAnimation scaleInDelayed = new ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleInDelayed.setDuration(800);
        scaleInDelayed.setStartOffset(100);
        scaleInDelayed.setFillAfter(true);
        circleMiddle.startAnimation(scaleInDelayed);

        // Logo scale animation
        ScaleAnimation logoScale = new ScaleAnimation(
                0.5f, 1f, 0.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        logoScale.setDuration(600);
        logoScale.setStartOffset(200);
        logoScale.setFillAfter(true);
        cardLogo.startAnimation(logoScale);

        // Text fade in animation
        AlphaAnimation textFadeIn = new AlphaAnimation(0f, 1f);
        textFadeIn.setDuration(600);
        textFadeIn.setStartOffset(400);
        textFadeIn.setFillAfter(true);
        layoutAppName.startAnimation(textFadeIn);

        AlphaAnimation taglineFadeIn = new AlphaAnimation(0f, 1f);
        taglineFadeIn.setDuration(600);
        taglineFadeIn.setStartOffset(500);
        taglineFadeIn.setFillAfter(true);
        tvTagline.startAnimation(taglineFadeIn);

        // Animate progress segments
        animateProgressSegments();
    }

    private void animateProgressSegments() {
        // Animate progress segments with a sliding effect
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressSegment2.setBackgroundResource(R.drawable.bg_progress_segment_active);
        }, 1200);
    }

    private void navigateAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is logged in
                SessionManager sessionManager = SessionManager.getInstance(SplashActivity.this);

                Intent intent;
                if (sessionManager.isLoggedIn()) {
                    // Check role and navigate to appropriate dashboard
                    if (sessionManager.isAdmin()) {
                        // Admin user - go to Admin Dashboard
                        intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                    } else {
                        // Worker user - go to Worker Home
                        intent = new Intent(SplashActivity.this, HomeActivity.class);
                    }
                } else {
                    // Go to Welcome Screen
                    intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                }

                startActivity(intent);
                finish();

                // Transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }
}
