package com.simats.ashasmartcare;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.simats.ashasmartcare.database.DatabaseHelper;

/**
 * Application class for ASHA Healthcare App
 * Initializes global resources like Volley RequestQueue and Database
 */
public class AshaApplication extends Application {

    private static AshaApplication instance;
    private RequestQueue requestQueue;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static synchronized AshaApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    /**
     * Get Volley RequestQueue (singleton)
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Get Database Helper (singleton)
     */
    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        }
        return databaseHelper;
    }
}
