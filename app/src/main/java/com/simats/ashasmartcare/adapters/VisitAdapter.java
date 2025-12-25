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
import com.simats.ashasmartcare.models.Visit;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.ViewHolder> {

    private Context context;
    private List<Visit> visitList;
    private OnVisitClickListener listener;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnVisitClickListener {
        void onVisitClick(Visit visit);
    }

    public VisitAdapter(Context context, List<Visit> visitList, OnVisitClickListener listener) {
        this.context = context;
        this.visitList = visitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Visit visit = visitList.get(position);

        holder.tvVisitType.setText(visit.getVisitType());
        holder.tvPurpose.setText(visit.getPurpose());

        // Format visit date
        String visitDate = visit.getVisitDate();
        if (visitDate != null && !visitDate.isEmpty()) {
            try {
                holder.tvVisitDate.setText(outputFormat.format(inputFormat.parse(visitDate)));
            } catch (Exception e) {
                holder.tvVisitDate.setText(visitDate);
            }
        } else {
            holder.tvVisitDate.setText("N/A");
        }

        // Format next visit date
        String nextVisitDate = visit.getNextVisitDate();
        if (nextVisitDate != null && !nextVisitDate.isEmpty()) {
            try {
                holder.tvNextVisit.setText("Next: " + outputFormat.format(inputFormat.parse(nextVisitDate)));
                holder.tvNextVisit.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.tvNextVisit.setText("Next: " + nextVisitDate);
                holder.tvNextVisit.setVisibility(View.VISIBLE);
            }
        } else {
            holder.tvNextVisit.setVisibility(View.GONE);
        }

        // Set icon based on visit type
        switch (visit.getVisitType()) {
            case "Home Visit":
                holder.ivVisitIcon.setImageResource(R.drawable.ic_home);
                holder.ivVisitIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary));
                break;
            case "ANC Checkup":
            case "PNC Checkup":
                holder.ivVisitIcon.setImageResource(R.drawable.ic_pregnant);
                holder.ivVisitIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent_pink));
                break;
            case "Child Health Checkup":
                holder.ivVisitIcon.setImageResource(R.drawable.ic_child);
                holder.ivVisitIcon.setColorFilter(ContextCompat.getColor(context, R.color.info));
                break;
            case "Vaccination":
                holder.ivVisitIcon.setImageResource(R.drawable.ic_vaccine);
                holder.ivVisitIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));
                break;
            case "Emergency":
                holder.ivVisitIcon.setImageResource(R.drawable.ic_warning);
                holder.ivVisitIcon.setColorFilter(ContextCompat.getColor(context, R.color.error));
                break;
            default:
                holder.ivVisitIcon.setImageResource(R.drawable.ic_history);
                holder.ivVisitIcon.setColorFilter(ContextCompat.getColor(context, R.color.teal_700));
                break;
        }

        // Show findings preview
        String findings = visit.getFindings();
        if (findings != null && !findings.isEmpty()) {
            holder.tvFindings.setText(findings.length() > 100 ? findings.substring(0, 100) + "..." : findings);
            holder.tvFindings.setVisibility(View.VISIBLE);
        } else {
            holder.tvFindings.setVisibility(View.GONE);
        }

        // Sync status indicator
        if ("PENDING".equals(visit.getSyncStatus())) {
            holder.ivSyncStatus.setVisibility(View.VISIBLE);
        } else {
            holder.ivSyncStatus.setVisibility(View.GONE);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVisitIcon, ivSyncStatus;
        TextView tvVisitType, tvVisitDate, tvPurpose, tvFindings, tvNextVisit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVisitIcon = itemView.findViewById(R.id.ivVisitIcon);
            ivSyncStatus = itemView.findViewById(R.id.ivSyncStatus);
            tvVisitType = itemView.findViewById(R.id.tvVisitType);
            tvVisitDate = itemView.findViewById(R.id.tvVisitDate);
            tvPurpose = itemView.findViewById(R.id.tvPurpose);
            tvFindings = itemView.findViewById(R.id.tvFindings);
            tvNextVisit = itemView.findViewById(R.id.tvNextVisit);
        }
    }
}
