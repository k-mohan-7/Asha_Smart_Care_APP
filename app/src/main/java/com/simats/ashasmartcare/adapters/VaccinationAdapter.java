package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.Vaccination;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VaccinationAdapter extends RecyclerView.Adapter<VaccinationAdapter.ViewHolder> {

    private Context context;
    private List<Vaccination> vaccinationList;
    private OnVaccinationClickListener listener;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnVaccinationClickListener {
        void onVaccinationClick(Vaccination vaccination);
    }

    public VaccinationAdapter(Context context, List<Vaccination> vaccinationList, OnVaccinationClickListener listener) {
        this.context = context;
        this.vaccinationList = vaccinationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vaccination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vaccination vaccination = vaccinationList.get(position);

        holder.tvVaccineName.setText(vaccination.getVaccineName());
        
        // Format and display scheduled date
        String scheduledDate = vaccination.getScheduledDate();
        if (scheduledDate != null && !scheduledDate.isEmpty()) {
            try {
                holder.tvScheduledDate.setText("Scheduled: " + outputFormat.format(inputFormat.parse(scheduledDate)));
            } catch (Exception e) {
                holder.tvScheduledDate.setText("Scheduled: " + scheduledDate);
            }
        } else {
            holder.tvScheduledDate.setText("Scheduled: N/A");
        }

        // Display given date if available
        String givenDate = vaccination.getGivenDate();
        if (givenDate != null && !givenDate.isEmpty()) {
            try {
                holder.tvGivenDate.setText("Given: " + outputFormat.format(inputFormat.parse(givenDate)));
                holder.tvGivenDate.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.tvGivenDate.setText("Given: " + givenDate);
                holder.tvGivenDate.setVisibility(View.VISIBLE);
            }
        } else {
            holder.tvGivenDate.setVisibility(View.GONE);
        }

        // Set status with color
        String status = vaccination.getStatus();
        holder.tvStatus.setText(status);
        
        switch (status) {
            case "Given":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_given);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));
                break;
            case "Scheduled":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_scheduled);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_schedule);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.info));
                break;
            case "Missed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_missed);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_warning);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.error));
                break;
            case "Delayed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_delayed);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_warning);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning));
                break;
        }

        // Batch number
        String batch = vaccination.getBatchNumber();
        if (batch != null && !batch.isEmpty()) {
            holder.tvBatchNumber.setText("Batch: " + batch);
            holder.tvBatchNumber.setVisibility(View.VISIBLE);
        } else {
            holder.tvBatchNumber.setVisibility(View.GONE);
        }

        // Sync status indicator
        if ("PENDING".equals(vaccination.getSyncStatus())) {
            holder.ivSyncStatus.setVisibility(View.VISIBLE);
        } else {
            holder.ivSyncStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVaccinationClick(vaccination);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vaccinationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStatusIcon, ivSyncStatus;
        TextView tvVaccineName, tvScheduledDate, tvGivenDate, tvStatus, tvBatchNumber;
        LinearLayout layoutDates;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            ivSyncStatus = itemView.findViewById(R.id.ivSyncStatus);
            tvVaccineName = itemView.findViewById(R.id.tvVaccineName);
            tvScheduledDate = itemView.findViewById(R.id.tvScheduledDate);
            tvGivenDate = itemView.findViewById(R.id.tvGivenDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBatchNumber = itemView.findViewById(R.id.tvBatchNumber);
            layoutDates = itemView.findViewById(R.id.layoutDates);
        }
    }
}
