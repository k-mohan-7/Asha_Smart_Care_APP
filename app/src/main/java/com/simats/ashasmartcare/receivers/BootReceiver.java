package com.simats.ashasmartcare.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.simats.ashasmartcare.services.SyncService;
import com.simats.ashasmartcare.utils.NetworkUtils;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed - checking for pending sync");
            
            if (NetworkUtils.isNetworkAvailable(context)) {
                Intent syncIntent = new Intent(context, SyncService.class);
                context.startService(syncIntent);
            }
        }
    }
}
