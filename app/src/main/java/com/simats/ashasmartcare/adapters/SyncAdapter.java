package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.SyncRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncAdapter extends RecyclerView.Adapter<SyncAdapter.ViewHolder> {

    private Context context;
    private List<SyncRecord> syncRecordList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

    public SyncAdapter(Context context, List<SyncRecord> syncRecordList) {
        this.context = context;
        this.syncRecordList = syncRecordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sync_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SyncRecord record = syncRecordList.get(position);

        holder.tvTableName.setText(formatTableName(record.getTableName()));
        holder.tvRecordId.setText("ID: " + record.getRecordId());

        // Format timestamp
        try {
            Date date = new Date(record.getCreatedAt());
            holder.tvTimestamp.setText(dateFormat.format(date));
        } catch (Exception e) {
            holder.tvTimestamp.setText("Unknown");
        }

        // Set status
        String status = record.getSyncStatus();
        holder.tvStatus.setText(status);
        
        switch (status) {
            case "PENDING":
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_schedule);
                break;
            case "SYNCED":
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check);
                break;
            case "FAILED":
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.error));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_warning);
                break;
        }

        // Show error message if available
        String error = record.getErrorMessage();
        if (error != null && !error.isEmpty()) {
            holder.tvError.setText(error);
            holder.tvError.setVisibility(View.VISIBLE);
        } else {
            holder.tvError.setVisibility(View.GONE);
        }
    }

    private String formatTableName(String tableName) {
        if (tableName == null) return "Unknown";
        switch (tableName) {
            case "patients":
                return "Patient";
            case "pregnancy_visits":
                return "Pregnancy Visit";
            case "child_growth":
                return "Child Growth";
            case "vaccinations":
                return "Vaccination";
            case "visits":
                return "Visit";
            default:
                return tableName;
        }
    }

    @Override
    public int getItemCount() {
        return syncRecordList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStatusIcon;
        TextView tvTableName, tvRecordId, tvTimestamp, tvStatus, tvError;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvTableName = itemView.findViewById(R.id.tvTableName);
            tvRecordId = itemView.findViewById(R.id.tvRecordId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvError = itemView.findViewById(R.id.tvError);
        }
    }
}
