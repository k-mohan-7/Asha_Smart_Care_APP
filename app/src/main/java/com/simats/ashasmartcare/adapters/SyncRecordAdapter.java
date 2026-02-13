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

import java.util.List;

public class SyncRecordAdapter extends RecyclerView.Adapter<SyncRecordAdapter.ViewHolder> {

    private Context context;
    private List<SyncRecord> syncRecords;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(SyncRecord record);
    }

    public SyncRecordAdapter(Context context, List<SyncRecord> syncRecords) {
        this.context = context;
        this.syncRecords = syncRecords;
    }

    public SyncRecordAdapter(Context context, List<SyncRecord> syncRecords, OnDeleteClickListener listener) {
        this.context = context;
        this.syncRecords = syncRecords;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker_sync_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SyncRecord record = syncRecords.get(position);
        holder.tvTitle.setText(record.getTitle());
        holder.tvTimestamp.setText(record.getTimestamp());

        if (record.isSynced()) {
            holder.tvStatusBadge.setText("Synced");
            holder.tvStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_synced));
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_synced_text));

            holder.ivIcon.setImageResource(R.drawable.ic_check_circle_outline);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_synced));
            holder.iconContainer
                    .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_icon_circle_light_green));

            holder.ivDelete.setVisibility(View.GONE);
        } else if ("FAILED".equalsIgnoreCase(record.getSyncStatus())) {
            holder.tvStatusBadge.setText("Failed");
            holder.tvStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_pending));
            holder.tvStatusBadge.setTextColor(android.graphics.Color.RED);

            holder.ivIcon.setImageResource(R.drawable.ic_warning);
            holder.ivIcon.setColorFilter(android.graphics.Color.RED);
            holder.iconContainer
                    .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_icon_circle_light_yellow));

            holder.ivDelete.setVisibility(View.VISIBLE);

            if (record.getErrorMessage() != null && !record.getErrorMessage().isEmpty()) {
                holder.tvError.setText(record.getErrorMessage());
                holder.tvError.setVisibility(View.VISIBLE);
            } else {
                holder.tvError.setVisibility(View.GONE);
            }
        } else {
            holder.tvStatusBadge.setText("Pending");
            holder.tvStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_pending));
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));

            holder.ivIcon.setImageResource(R.drawable.ic_sync_disabled);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_pending));
            holder.iconContainer
                    .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_icon_circle_light_yellow));

            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.tvError.setVisibility(View.GONE);
        }

        holder.ivDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return syncRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivDelete;
        View iconContainer;
        TextView tvTitle, tvTimestamp, tvStatusBadge, tvError;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvError = itemView.findViewById(R.id.tvError);
        }
    }
}
