package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.SyncRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncSyncedAdapter extends RecyclerView.Adapter<SyncSyncedAdapter.ViewHolder> {

    private Context context;
    private List<SyncRecord> recordList;

    public SyncSyncedAdapter(Context context, List<SyncRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sync_synced, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SyncRecord record = recordList.get(position);
        
        // Use tableName instead of getDataType() and format it nicely
        String tableName = record.getTableName() != null ? formatTableName(record.getTableName()) : "Unknown";
        holder.tvName.setText(tableName);
        holder.tvTimestamp.setText(getTimeAgo(record.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    private String formatTableName(String tableName) {
        switch (tableName) {
            case "patients": return "Patient Record";
            case "pregnancy_visits": return "Pregnancy Visit";
            case "child_growth": return "Child Growth";
            case "vaccinations": return "Vaccination";
            case "visits": return "General Visit";
            default: return tableName;
        }
    }

    private String getTimeAgo(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            if (date == null) return "Unknown";

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " min ago";
            } else {
                return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTimestamp;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
