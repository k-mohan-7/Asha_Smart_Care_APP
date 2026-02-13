package com.simats.ashasmartcare.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.simats.ashasmartcare.R;

public class ConnectionStatusManager {
    
    private static ConnectionStatusManager instance;
    private Context context;
    private Toast currentToast;
    private boolean isOnline = true;
    private boolean hasShownOfflineMessage = false;
    private boolean hasShownOnlineMessage = false;
    
    private ConnectionStatusManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized ConnectionStatusManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionStatusManager(context);
        }
        return instance;
    }
    
    public void showNetworkStatus(boolean isConnected) {
        // State changed from online to offline
        if (!isConnected && isOnline && !hasShownOfflineMessage) {
            showOfflineMessage();
            isOnline = false;
            hasShownOfflineMessage = true;
            hasShownOnlineMessage = false;
        }
        // State changed from offline to online
        else if (isConnected && !isOnline && !hasShownOnlineMessage) {
            showOnlineMessage();
            isOnline = true;
            hasShownOnlineMessage = true;
            hasShownOfflineMessage = false;
        }
    }
    
    private void showOfflineMessage() {
        cancelCurrentToast();
        currentToast = Toast.makeText(context, 
            " No Internet Connection\nMoving to offline mode. Data will be saved locally.", 
            Toast.LENGTH_LONG);
        currentToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        currentToast.show();
    }
    
    private void showOnlineMessage() {
        cancelCurrentToast();
        currentToast = Toast.makeText(context, 
            " Back Online\nYou can now sync your data.", 
            Toast.LENGTH_LONG);
        currentToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        currentToast.show();
    }
    
    private void cancelCurrentToast() {
        if (currentToast != null) {
            currentToast.cancel();
        }
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setInitialState(boolean isConnected) {
        this.isOnline = isConnected;
        this.hasShownOfflineMessage = false;
        this.hasShownOnlineMessage = false;
    }
}
