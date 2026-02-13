package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.Vaccination;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VaccinationAdapter extends RecyclerView.Adapter<VaccinationAdapter.ViewHolder> {

    private Context context;
    private List<Vaccination> vaccinationList;
    private OnVaccinationClickListener listener;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    public interface OnVaccinationClickListener {
        void onVaccinationClick(Vaccination vaccination);

        void onMarkDoneClick(Vaccination vaccination);
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

        // Set patient name (assuming it's available in the Vaccination model)
        holder.tvPatientName
                .setText(vaccination.getPatientName() != null ? vaccination.getPatientName() : "Unknown Patient");

        // Set vaccine name
        holder.tvVaccineName.setText(vaccination.getVaccineName());

        // Format and display due date
        String dueDate = vaccination.getScheduledDate();
        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                Date date = inputFormat.parse(dueDate);
                holder.tvDueDate.setText("Due: " + outputFormat.format(date));
            } catch (Exception e) {
                holder.tvDueDate.setText("Due: " + dueDate);
            }
        } else {
            holder.tvDueDate.setText("Due: N/A");
        }

        // Calculate and set status
        String badgeText;
        int badgeColor;
        int badgeBg;

        if ("Given".equals(vaccination.getStatus())) {
            badgeText = "Given";
            badgeBg = R.drawable.bg_badge_synced; // Green circle/bg
            badgeColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
            holder.btnMarkDone.setVisibility(View.GONE);
        } else {
            badgeText = calculateStatus(dueDate);
            holder.btnMarkDone.setVisibility(View.VISIBLE);
            switch (badgeText) {
                case "overdue":
                    badgeBg = R.drawable.bg_badge_risk;
                    badgeColor = ContextCompat.getColor(context, android.R.color.holo_red_dark);
                    break;
                case "due soon":
                    badgeBg = R.drawable.bg_badge_pending;
                    badgeColor = ContextCompat.getColor(context, android.R.color.holo_orange_dark);
                    break;
                default: // upcoming
                    badgeBg = R.drawable.bg_badge_category;
                    badgeColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark);
                    break;
            }
        }

        holder.tvStatusBadge.setText(badgeText);
        holder.tvStatusBadge.setBackgroundResource(badgeBg);
        holder.tvStatusBadge.setTextColor(badgeColor);

        // Mark Done button click
        holder.btnMarkDone.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkDoneClick(vaccination);
            }
        });

        // Card click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVaccinationClick(vaccination);
            }
        });
    }

    private String calculateStatus(String dueDate) {
        if (dueDate == null || dueDate.isEmpty()) {
            return "upcoming";
        }

        try {
            Date due = inputFormat.parse(dueDate);
            Date today = new Date();

            // Calculate difference in days
            long diff = due.getTime() - today.getTime();
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            if (daysDiff < 0) {
                return "overdue";
            } else if (daysDiff <= 7) {
                return "due soon";
            } else {
                return "upcoming";
            }
        } catch (Exception e) {
            return "upcoming";
        }
    }

    @Override
    public int getItemCount() {
        return vaccinationList.size();
    }

    public void updateList(List<Vaccination> newList) {
        this.vaccinationList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvVaccineName, tvDueDate, tvStatusBadge;
        Button btnMarkDone;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvVaccineName = itemView.findViewById(R.id.tv_vaccine_name);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            btnMarkDone = itemView.findViewById(R.id.btn_mark_done);
        }
    }
}
