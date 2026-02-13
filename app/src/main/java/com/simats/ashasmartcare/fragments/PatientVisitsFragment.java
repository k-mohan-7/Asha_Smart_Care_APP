package com.simats.ashasmartcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PatientVisitsFragment extends Fragment {

    private RecyclerView rvVisits;
    private int patientId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_visits, container, false);

        if (getArguments() != null) {
            patientId = getArguments().getInt("patient_id", -1);
        }

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        rvVisits = view.findViewById(R.id.rv_visits);
        setupVisits();

        // Hide the add visit button in the fragment because it's in the Activity bottom
        // bar
        View btnAdd = view.findViewById(R.id.btn_add_visit);
        if (btnAdd != null)
            btnAdd.setVisibility(View.GONE);

        return view;
    }

    private void setupVisits() {
        String category = getArguments() != null ? getArguments().getString("category", "") : "";
        List<VisitItem> items = new ArrayList<>();

        if ("child".equalsIgnoreCase(category)) {
            // General Visits for Child
            List<com.simats.ashasmartcare.models.Visit> visits = databaseHelper.getVisitsByPatient(patientId);
            for (com.simats.ashasmartcare.models.Visit v : visits) {
                items.add(new VisitItem(v.getVisitType(), v.getVisitDate(), "Completed"));

                // Check if this is the most recent visit and has a next visit date
                if (visits.indexOf(v) == 0 && v.getNextVisitDate() != null && !v.getNextVisitDate().isEmpty()) {
                    // Simple future check logic could go here, for now just add as upcoming
                    items.add(0, new VisitItem("Next Visit", "Due: " + v.getNextVisitDate(), "Due Soon"));
                }
            }
            if (items.isEmpty()) {
                // Add empty state or just show nothing? Maybe a placeholder.
                // For now, let's leave it empty or add a "No visits recorded" item if
                // supported,
                // but standard RecyclerView just shows empty.
            }
        } else {
            // Pregnancy Visits
            List<com.simats.ashasmartcare.models.PregnancyVisit> visits = databaseHelper
                    .getPregnancyVisitsByPatient(patientId);
            for (com.simats.ashasmartcare.models.PregnancyVisit v : visits) {
                String title = "ANC Checkup";
                if (v.getWeeksPregnant() > 0) {
                    title += " (" + v.getWeeksPregnant() + " Weeks)";
                }
                items.add(new VisitItem(title, v.getVisitDate(), "Completed"));

                if (visits.indexOf(v) == 0 && v.getNextVisitDate() != null && !v.getNextVisitDate().isEmpty()) {
                    items.add(0, new VisitItem("Next ANC Visit", "Due: " + v.getNextVisitDate(), "Due Soon"));
                }
            }
        }

        rvVisits.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvVisits.setAdapter(new VisitAdapter(items));
    }

    private DatabaseHelper databaseHelper;

    private class VisitItem {
        String title, subtitle, status;

        VisitItem(String title, String subtitle, String status) {
            this.title = title;
            this.subtitle = subtitle;
            this.status = status;
        }
    }

    private class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.ViewHolder> {
        private List<VisitItem> items;

        VisitAdapter(List<VisitItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visit_hi_fi, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VisitItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvSubtitle.setText(item.subtitle);
            holder.tvStatus.setText(item.status);

            if ("Completed".equalsIgnoreCase(item.status)) {
                holder.layoutIcon.setBackgroundResource(R.drawable.bg_badge_completed);
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#10B981"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else if ("Missed".equalsIgnoreCase(item.status)) {
                holder.layoutIcon.setBackgroundResource(R.drawable.bg_badge_missed);
                holder.ivIcon.setImageResource(R.drawable.ic_close); // I'll assume ic_close exists or use a cross
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_missed);
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            } else {
                holder.layoutIcon.setBackgroundResource(R.drawable.bg_badge_due_soon);
                holder.ivIcon.setImageResource(R.drawable.ic_event);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#3B82F6"));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_due_soon);
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvStatus;
            ImageView ivIcon;
            FrameLayout layoutIcon;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_visit_title);
                tvSubtitle = v.findViewById(R.id.tv_visit_subtitle);
                tvStatus = v.findViewById(R.id.tv_visit_status);
                ivIcon = v.findViewById(R.id.iv_visit_icon);
                layoutIcon = v.findViewById(R.id.layout_visit_icon);
            }
        }
    }
}
