package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.Visit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitHistoryAdapter extends RecyclerView.Adapter<VisitHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Visit> visitList;
    private OnVisitClickListener listener;
    private DatabaseHelper dbHelper;

    public interface OnVisitClickListener {
        void onVisitClick(Visit visit);
    }

    public VisitHistoryAdapter(Context context, List<Visit> visitList, OnVisitClickListener listener) {
        this.context = context;
        this.visitList = visitList;
        this.listener = listener;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visit_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Visit visit = visitList.get(position);
        Patient patient = dbHelper.getPatientById(visit.getPatientId());

        // Set patient name
        if (patient != null) {
            holder.tvPatientName.setText(patient.getName());
        } else {
            holder.tvPatientName.setText("Unknown Patient");
        }

        // Set visit info (type and time)
        String visitType = visit.getVisitType() != null ? visit.getVisitType() : "General Visit";
        String time = formatTime(visit.getVisitDate());
        holder.tvVisitInfo.setText(visitType + " • " + time);

        // Set vitals - Visit model doesn't store vitals, only PregnancyVisit does
        // For now, display N/A for general visits
        holder.tvBloodPressure.setText("N/A");
        holder.tvHeartRate.setText("N/A");
        holder.tvTemperature.setText("N/A");

        // Set sync status
        boolean isSynced = "SYNCED".equals(visit.getSyncStatus());
        if (isSynced) {
            holder.ivSyncIcon.setImageResource(R.drawable.ic_check_circle);
            holder.ivSyncIcon.setColorFilter(context.getResources().getColor(R.color.success));
            holder.tvSyncText.setText("Synced");
            holder.tvSyncText.setTextColor(context.getResources().getColor(R.color.success));
        } else {
            holder.ivSyncIcon.setImageResource(R.drawable.ic_pending);
            holder.ivSyncIcon.setColorFilter(context.getResources().getColor(R.color.warning));
            holder.tvSyncText.setText("Pending");
            holder.tvSyncText.setTextColor(context.getResources().getColor(R.color.warning));
        }

        // Set risk badge
        if (patient != null && patient.isHighRisk()) {
            holder.tvRiskBadge.setText("High Risk");
            holder.tvRiskBadge.setTextColor(context.getResources().getColor(R.color.error));
            holder.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_risk);
        } else {
            holder.tvRiskBadge.setText("Low Risk");
            holder.tvRiskBadge.setTextColor(context.getResources().getColor(R.color.success));
            holder.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_success);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVisitClick(visit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    private String formatTime(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateTime);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat.format(date);
        } catch (Exception e) {
            return "N/A";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvVisitInfo, tvBloodPressure, tvHeartRate, tvTemperature, tvRiskBadge, tvSyncText;
        ImageView ivSyncIcon;

        ViewHolder(View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvVisitInfo = itemView.findViewById(R.id.tvVisitInfo);
            tvBloodPressure = itemView.findViewById(R.id.tvBloodPressure);
            tvHeartRate = itemView.findViewById(R.id.tvHeartRate);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvRiskBadge = itemView.findViewById(R.id.tvRiskBadge);
            tvSyncText = itemView.findViewById(R.id.tvSyncText);
            ivSyncIcon = itemView.findViewById(R.id.ivSyncIcon);
        }
    }
}
