package com.simats.ashasmartcare.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkMonitorService extends Service {
    
    private static final String TAG = "NetworkMonitorService";
    public static final String ACTION_NETWORK_CHANGED = "com.simats.ashasmartcare.NETWORK_CHANGED";
    public static final String EXTRA_IS_CONNECTED = "is_connected";
    
    // Cache for connectivity checks to avoid repeated overhead
    private static final AtomicBoolean cachedConnectivity = new AtomicBoolean(false);
    private static final AtomicLong lastCheckTime = new AtomicLong(0);
    private static final long CACHE_VALIDITY_MS = 3000; // 3 seconds
    
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Handler handler;
    private Runnable networkCheckRunnable;
    private boolean wasConnected = false;
    private boolean isFirstCheck = true;
    
    @Override
    public void onCreate() {
        super.onCreate();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        
        // Setup network callback for real-time monitoring
        setupNetworkCallback();
        
        // Setup periodic check every 1 second
        setupPeriodicCheck();
    }
    
    private void setupNetworkCallback() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                handleNetworkChange(true);
            }
            
            @Override
            public void onLost(@NonNull Network network) {
                handleNetworkChange(false);
            }
        };
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }
    
    private void setupPeriodicCheck() {
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkNetworkStatus();
                handler.postDelayed(this, 1000); // Check every 1 second
            }
        };
        handler.post(networkCheckRunnable);
    }
    
    private void checkNetworkStatus() {
        boolean isConnected = isNetworkAvailable();
        
        // Only broadcast if state changed or first check
        if (isFirstCheck || isConnected != wasConnected) {
            handleNetworkChange(isConnected);
            isFirstCheck = false;
        }
    }
    
    private void handleNetworkChange(boolean isConnected) {
        if (wasConnected != isConnected) {
            wasConnected = isConnected;
            // Clear cache on network state change to force fresh check
            clearCache();
            broadcastNetworkStatus(isConnected);
        }
    }
    
    private boolean isNetworkAvailable() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) return false;
        
        // Check for actual internet connectivity, not just connection
        boolean hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        
        boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        
        // Return true if we have transport and internet capability
        // Validation check removed as it can be too strict and delay detection
        return hasTransport && hasInternet;
    }
    
    private void broadcastNetworkStatus(boolean isConnected) {
        Intent intent = new Intent(ACTION_NETWORK_CHANGED);
        intent.putExtra(EXTRA_IS_CONNECTED, isConnected);
        sendBroadcast(intent);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Service will be restarted if killed
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        if (handler != null && networkCheckRunnable != null) {
            handler.removeCallbacks(networkCheckRunnable);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public static boolean isNetworkConnected(Context context) {
        // Check cache first to avoid repeated checks
        long now = System.currentTimeMillis();
        long lastCheck = lastCheckTime.get();
        
        if (now - lastCheck < CACHE_VALIDITY_MS) {
            boolean cached = cachedConnectivity.get();
            Log.d(TAG, "isNetworkConnected: Returning cached result=" + cached + 
                       " (age=" + (now - lastCheck) + "ms)");
            return cached;
        }
        
        // Perform fresh check
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (network == null) {
            Log.d(TAG, "isNetworkConnected: No active network");
            updateConnectivityCache(false);
            return false;
        }
        
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities == null) {
            Log.d(TAG, "isNetworkConnected: No network capabilities");
            updateConnectivityCache(false);
            return false;
        }
        
        // Check for actual internet connectivity
        boolean hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        
        boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        boolean isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        boolean notSuspended = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED);
        
        Log.d(TAG, "isNetworkConnected: hasTransport=" + hasTransport + 
                   ", hasInternet=" + hasInternet + 
                   ", isValidated=" + isValidated +
                   ", notSuspended=" + notSuspended);
        
        // Decision logic: Trust capability even if validation is pending
        boolean isConnected = false;
        
        if (hasTransport && hasInternet) {
            if (isValidated) {
                // Best case: Network is validated by Android
                Log.d(TAG, "isNetworkConnected: Network VALIDATED - ONLINE");
                isConnected = true;
            } else {
                // Network has capability but not validated yet
                // Trust it anyway (validation might be in progress or failed incorrectly)
                Log.d(TAG, "isNetworkConnected: Network has capability (validation pending) - ASSUME ONLINE");
                isConnected = true;
            }
        } else {
            Log.d(TAG, "isNetworkConnected: No transport or internet capability - OFFLINE");
            isConnected = false;
        }
        
        updateConnectivityCache(isConnected);
        return isConnected;
    }
    
    /**
     * Update connectivity cache with timestamp
     */
    private static void updateConnectivityCache(boolean isConnected) {
        cachedConnectivity.set(isConnected);
        lastCheckTime.set(System.currentTimeMillis());
        Log.d(TAG, "Cache updated: isConnected=" + isConnected);
    }
    
    /**
     * Clear cache to force fresh check
     * Call this when you need immediate recheck (e.g., after network change broadcast)
     */
    public static void clearCache() {
        lastCheckTime.set(0);
        cachedConnectivity.set(false);
        Log.d(TAG, "Connectivity cache cleared");
    }
}
