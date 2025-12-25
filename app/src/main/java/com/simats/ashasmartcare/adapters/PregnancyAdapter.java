package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.PregnancyVisit;

import java.util.List;

public class PregnancyAdapter extends RecyclerView.Adapter<PregnancyAdapter.ViewHolder> {

    private Context context;
    private List<PregnancyVisit> visitList;
    private OnPregnancyClickListener listener;

    public interface OnPregnancyClickListener {
        void onPregnancyClick(PregnancyVisit visit);
    }

    public PregnancyAdapter(Context context, List<PregnancyVisit> visitList, OnPregnancyClickListener listener) {
        this.context = context;
        this.visitList = visitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pregnancy_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PregnancyVisit visit = visitList.get(position);
        holder.bind(visit);
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvVisitNumber, tvVisitDate, tvWeeks, tvWeight, tvBP, tvHemoglobin;
        TextView tvHighRisk, tvSyncStatus;
        ImageView ivHighRisk;
        View riskIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardVisit);
            tvVisitNumber = itemView.findViewById(R.id.tvVisitNumber);
            tvVisitDate = itemView.findViewById(R.id.tvVisitDate);
            tvWeeks = itemView.findViewById(R.id.tvWeeks);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvBP = itemView.findViewById(R.id.tvBP);
            tvHemoglobin = itemView.findViewById(R.id.tvHemoglobin);
            tvHighRisk = itemView.findViewById(R.id.tvHighRisk);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
            ivHighRisk = itemView.findViewById(R.id.ivHighRisk);
            riskIndicator = itemView.findViewById(R.id.riskIndicator);
        }

        void bind(PregnancyVisit visit) {
            int position = getAdapterPosition() + 1;
            tvVisitNumber.setText("Visit #" + position);
            tvVisitDate.setText(visit.getVisitDate());
            tvWeeks.setText(visit.getWeeksPregnant() + " weeks");
            tvWeight.setText(String.format("%.1f kg", visit.getWeight()));
            tvBP.setText(visit.getBloodPressureSystolic() + "/" + visit.getBloodPressureDiastolic() + " mmHg");
            tvHemoglobin.setText(String.format("%.1f g/dL", visit.getHemoglobin()));

            // High risk indicator
            if (visit.isHighRisk()) {
                tvHighRisk.setVisibility(View.VISIBLE);
                ivHighRisk.setVisibility(View.VISIBLE);
                riskIndicator.setBackgroundColor(context.getResources().getColor(R.color.error));
            } else {
                tvHighRisk.setVisibility(View.GONE);
                ivHighRisk.setVisibility(View.GONE);
                riskIndicator.setBackgroundColor(context.getResources().getColor(R.color.success));
            }

            // Sync status
            String syncStatus = visit.getSyncStatus();
            if ("SYNCED".equals(syncStatus)) {
                tvSyncStatus.setText("✓ Synced");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.success));
            } else {
                tvSyncStatus.setText("⏳ Pending");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.warning));
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPregnancyClick(visit);
                }
            });
        }
    }
}
