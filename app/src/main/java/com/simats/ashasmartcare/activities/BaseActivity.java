package com.simats.ashasmartcare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simats.ashasmartcare.PatientAlertsActivity;
import com.simats.ashasmartcare.ProfileActivity;
import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.VisitHistoryActivity;

/**
 * Base Activity with persistent bottom navigation
 * All main activities should extend this to have consistent navigation
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupBottomNavigation();
    }

    /**
     * Setup bottom navigation - call this after setContentView
     */
    protected void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        if (bottomNavigation != null) {
            bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_home) {
                        if (!(BaseActivity.this instanceof HomeActivity)) {
                            Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }
                        return true;
                    } else if (id == R.id.nav_profile) {
                        if (!(BaseActivity.this instanceof ProfileActivity)) {
                            Intent intent = new Intent(BaseActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        return true;
                    } else if (id == R.id.nav_visits) {
                        if (!(BaseActivity.this instanceof VisitHistoryActivity)) {
                            Intent intent = new Intent(BaseActivity.this, VisitHistoryActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        return true;
                    } else if (id == R.id.nav_alerts) {
                        if (!(BaseActivity.this instanceof PatientAlertsActivity)) {
                            Intent intent = new Intent(BaseActivity.this, PatientAlertsActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        return true;
                    }

                    return false;
                }
            });
            
            // Highlight the current activity's nav item
            setSelectedNavItem();
        }
    }

    /**
     * Override this in child activities to set the correct nav item as selected
     * @return The menu item ID to select (e.g., R.id.nav_home)
     */
    protected abstract int getNavItemId();

    protected void setSelectedNavItem() {
        if (bottomNavigation != null) {
            int navItemId = getNavItemId();
            if (navItemId != 0) {
                bottomNavigation.setSelectedItemId(navItemId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSelectedNavItem();
    }
}
