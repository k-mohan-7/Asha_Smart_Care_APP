package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.HighRiskAlert;

import java.util.List;

public class HighRiskAlertAdapter extends RecyclerView.Adapter<HighRiskAlertAdapter.AlertViewHolder> {

    private Context context;
    private List<HighRiskAlert> alertList;

    public HighRiskAlertAdapter(Context context, List<HighRiskAlert> alertList) {
        this.context = context;
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_high_risk_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        HighRiskAlert alert = alertList.get(position);

        holder.tvPatientName.setText(alert.getPatientName());
        holder.tvVillage.setText(alert.getVillage());

        String type = alert.getAlertType();
        holder.tvAlertBadge.setText(type);

        if (type.contains("Vaccine") || type.contains("Overdue")) {
            holder.tvAlertBadge.setTextColor(context.getResources().getColor(R.color.error));
            holder.tvAlertBadge.setBackgroundResource(R.drawable.bg_red_light);
        } else if (type.contains("Weight") || type.contains("Malnutrition")) {
            holder.tvAlertBadge.setTextColor(context.getResources().getColor(R.color.status_pending_text));
            holder.tvAlertBadge.setBackgroundResource(R.drawable.bg_badge_pending);
        } else if (type.contains("BP") || type.contains("Pregnancy")) {
            holder.tvAlertBadge.setTextColor(context.getResources().getColor(R.color.category_pregnant));
            holder.tvAlertBadge.setBackgroundResource(R.drawable.bg_badge_pregnant);
        } else {
            holder.tvAlertBadge.setTextColor(context.getResources().getColor(R.color.primary_dark));
            holder.tvAlertBadge.setBackgroundResource(R.drawable.bg_blue_light);
        }

        // Show/hide reviewed state
        if (alert.isReviewed()) {
            holder.btnMarkReviewed.setVisibility(View.GONE);
            holder.layoutReviewed.setVisibility(View.VISIBLE);

            // Gray out the card
            holder.tvPatientName.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.tvVillage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.btnMarkReviewed.setVisibility(View.VISIBLE);
            holder.layoutReviewed.setVisibility(View.GONE);

            // Normal colors
            holder.tvPatientName.setTextColor(context.getResources().getColor(R.color.black));
            holder.tvVillage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Handle mark reviewed button
        holder.btnMarkReviewed.setOnClickListener(v -> {
            com.simats.ashasmartcare.database.DatabaseHelper.getInstance(context)
                    .markAlertReviewed(alert.getPatientId(), alert.getAlertType(), true);

            // Update local model and refresh view instead of removing
            alert.setReviewed(true);
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName;
        TextView tvVillage;
        TextView tvAlertBadge;
        Button btnMarkReviewed;
        LinearLayout layoutReviewed;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvVillage = itemView.findViewById(R.id.tvVillage);
            tvAlertBadge = itemView.findViewById(R.id.tvAlertBadge);
            btnMarkReviewed = itemView.findViewById(R.id.btnMarkReviewed);
            layoutReviewed = itemView.findViewById(R.id.layoutReviewed);
        }
    }
}
