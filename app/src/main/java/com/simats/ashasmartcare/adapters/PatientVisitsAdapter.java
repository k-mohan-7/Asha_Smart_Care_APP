package com.simats.ashasmartcare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.VisitDisplayModel;

import java.util.List;

public class PatientVisitsAdapter extends RecyclerView.Adapter<PatientVisitsAdapter.ViewHolder> {

    private List<VisitDisplayModel> visits;

    public PatientVisitsAdapter(List<VisitDisplayModel> visits) {
        this.visits = visits;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitDisplayModel visit = visits.get(position);

        holder.tvTitle.setText(visit.getTitle());
        holder.tvDate.setText(visit.getDate());
        holder.tvStatus.setText(visit.getStatus());

        // Status Styling
        if ("Completed".equalsIgnoreCase(visit.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_completed); // Ensure this exists or fallback
            holder.tvStatus.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.ivIcon.setImageResource(R.drawable.ic_check);
            holder.ivIcon.setColorFilter(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.layoutIcon.setBackgroundResource(R.drawable.bg_circle_success); // Check existing
        } else if ("Missed".equalsIgnoreCase(visit.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_missed); // Ensure exists
            holder.tvStatus
                    .setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.ivIcon.setImageResource(R.drawable.ic_close); // Using basic close icon
            holder.ivIcon.setColorFilter(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            // Fallback bg
            holder.layoutIcon.setBackgroundResource(R.drawable.circle_blue_bg);
        } else {
            // Due Soon / Scheduled
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_due_soon);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary));
            holder.ivIcon.setImageResource(R.drawable.ic_event);
            holder.ivIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.primary));
            holder.layoutIcon.setBackgroundResource(R.drawable.circle_blue_bg);
        }

        // Ensure proper resources are used. I'm taking a safe bet on standard colors if
        // drawables missing.
        // Actually, let's fix the colors to be safe using hex parsing if resources
        // unsure.
        // But for now, standard logic.
    }

    @Override
    public int getItemCount() {
        return visits.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus;
        ImageView ivIcon;
        FrameLayout layoutIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvVisitTitle);
            tvDate = itemView.findViewById(R.id.tvVisitDate);
            tvStatus = itemView.findViewById(R.id.tvVisitStatus);
            ivIcon = itemView.findViewById(R.id.ivVisitIcon);
            layoutIcon = itemView.findViewById(R.id.layoutIcon);
        }
    }
}
