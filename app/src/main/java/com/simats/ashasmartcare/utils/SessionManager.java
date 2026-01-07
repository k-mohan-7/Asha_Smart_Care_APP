package com.simats.ashasmartcare.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences helper class for storing app settings and user session
 */
public class SessionManager {

    private static final String PREF_NAME = "AshaHealthcarePrefs";
    private static final int PRIVATE_MODE = 0;

    // Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_WORKER_ID = "workerId";
    private static final String KEY_USER_STATE = "userState";
    private static final String KEY_USER_DISTRICT = "userDistrict";
    private static final String KEY_USER_AREA = "userArea";
    private static final String KEY_LAST_SYNC_TIME = "lastSyncTime";
    private static final String KEY_API_BASE_URL = "apiBaseUrl";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    private static SessionManager instance;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(long userId, String name, String phone, String email,
                                   String workerId, String state, String district, String area) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_WORKER_ID, workerId);
        editor.putString(KEY_USER_STATE, state);
        editor.putString(KEY_USER_DISTRICT, district);
        editor.putString(KEY_USER_AREA, area);
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Logout user
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Clear session (alias for logout)
     */
    public void clearSession() {
        logout();
    }

    // Getters
    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getWorkerId() {
        return pref.getString(KEY_WORKER_ID, "");
    }

    public String getUserState() {
        return pref.getString(KEY_USER_STATE, "");
    }

    public String getUserDistrict() {
        return pref.getString(KEY_USER_DISTRICT, "");
    }

    public String getUserArea() {
        return pref.getString(KEY_USER_AREA, "");
    }

    public String getUserLocation() {
        String area = getUserArea();
        String district = getUserDistrict();
        String state = getUserState();

        StringBuilder location = new StringBuilder();
        if (area != null && !area.isEmpty()) {
            location.append(area);
        }
        if (district != null && !district.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(district);
        }
        if (state != null && !state.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        return location.toString();
    }

    /**
     * Save last sync time
     */
    public void setLastSyncTime(long timestamp) {
        editor.putLong(KEY_LAST_SYNC_TIME, timestamp);
        editor.apply();
    }

    public long getLastSyncTime() {
        return pref.getLong(KEY_LAST_SYNC_TIME, 0);
    }

    public String getLastSyncTimeFormatted() {
        long timestamp = getLastSyncTime();
        if (timestamp == 0) {
            return "Never";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    /**
     * API Base URL
     */
    public void setApiBaseUrl(String url) {
        editor.putString(KEY_API_BASE_URL, url);
        editor.apply();
    }

    public String getApiBaseUrl() {
        // Default URL for Android Emulator to access localhost
        return pref.getString(KEY_API_BASE_URL, "http://10.190.92.63/asha_api/");
    }
}
