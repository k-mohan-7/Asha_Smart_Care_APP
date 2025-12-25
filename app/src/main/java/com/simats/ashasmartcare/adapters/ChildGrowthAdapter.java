package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.ChildGrowth;

import java.util.List;

public class ChildGrowthAdapter extends RecyclerView.Adapter<ChildGrowthAdapter.ViewHolder> {

    private Context context;
    private List<ChildGrowth> growthList;
    private OnGrowthClickListener listener;

    public interface OnGrowthClickListener {
        void onGrowthClick(ChildGrowth growth);
    }

    public ChildGrowthAdapter(Context context, List<ChildGrowth> growthList, OnGrowthClickListener listener) {
        this.context = context;
        this.growthList = growthList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_growth, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChildGrowth growth = growthList.get(position);
        holder.bind(growth);
    }

    @Override
    public int getItemCount() {
        return growthList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDate, tvAge, tvWeight, tvHeight, tvHeadCircumference, tvBMI, tvNutritionalStatus, tvSyncStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardGrowth);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvHeight = itemView.findViewById(R.id.tvHeight);
            tvHeadCircumference = itemView.findViewById(R.id.tvHeadCircumference);
            tvBMI = itemView.findViewById(R.id.tvBMI);
            tvNutritionalStatus = itemView.findViewById(R.id.tvNutritionalStatus);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
        }

        void bind(ChildGrowth growth) {
            tvDate.setText(growth.getMeasurementDate());
            tvAge.setText(growth.getAgeMonths() + " months");
            tvWeight.setText(String.format("%.1f kg", growth.getWeight()));
            tvHeight.setText(String.format("%.1f cm", growth.getHeight()));
            tvHeadCircumference.setText(String.format("%.1f cm", growth.getHeadCircumference()));
            
            // Calculate BMI
            double heightM = growth.getHeight() / 100;
            double bmi = growth.getWeight() / (heightM * heightM);
            tvBMI.setText(String.format("%.1f", bmi));

            // Nutritional status
            String status = growth.getNutritionalStatus();
            tvNutritionalStatus.setText(status != null && !status.isEmpty() ? status : "Normal");
            
            int statusColor;
            if ("Severe Underweight".equals(status) || "Severe Stunting".equals(status)) {
                statusColor = context.getResources().getColor(R.color.error);
            } else if ("Underweight".equals(status) || "Stunting".equals(status)) {
                statusColor = context.getResources().getColor(R.color.warning);
            } else {
                statusColor = context.getResources().getColor(R.color.success);
            }
            tvNutritionalStatus.setTextColor(statusColor);

            // Sync status
            String syncStatus = growth.getSyncStatus();
            if ("SYNCED".equals(syncStatus)) {
                tvSyncStatus.setText("✓ Synced");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.success));
            } else {
                tvSyncStatus.setText("⏳ Pending");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.warning));
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGrowthClick(growth);
                }
            });
        }
    }
}
