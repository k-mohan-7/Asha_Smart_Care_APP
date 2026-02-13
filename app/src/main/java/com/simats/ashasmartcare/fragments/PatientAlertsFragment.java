package com.simats.ashasmartcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class PatientAlertsFragment extends Fragment {

    private RecyclerView rvActive, rvResolved;
    private int patientId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_alerts, container, false);

        if (getArguments() != null) {
            patientId = getArguments().getInt("patient_id", -1);
        }

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        rvActive = view.findViewById(R.id.rv_active_alerts);
        rvResolved = view.findViewById(R.id.rv_resolved_alerts);

        setupAlerts();

        return view;
    }

    private DatabaseHelper databaseHelper;

    private void setupAlerts() {
        List<AlertItem> active = new ArrayList<>();
        List<AlertItem> resolved = new ArrayList<>();

        String category = getArguments() != null ? getArguments().getString("category", "") : "";
        com.simats.ashasmartcare.models.Patient patient = databaseHelper.getPatientById(patientId);

        if (patient != null && patient.isHighRisk()) {
            active.add(new AlertItem("High Risk Patient", "Patient marked as high risk. Monitor closely.", "Ongoing",
                    "High"));
        }

        if ("child".equalsIgnoreCase(category)) {
            List<com.simats.ashasmartcare.models.ChildGrowth> records = databaseHelper
                    .getChildGrowthByPatient(patientId);
            for (com.simats.ashasmartcare.models.ChildGrowth r : records) {
                if (!"Normal".equalsIgnoreCase(r.getNutritionalStatus()) && !"".equals(r.getNutritionalStatus())) {
                    active.add(new AlertItem("Growth Issue", "Status: " + r.getNutritionalStatus(), r.getRecordDate(),
                            "High"));
                    // We collect all past issues as 'active' for history visibility, or just latest
                }
            }
        } else {
            List<com.simats.ashasmartcare.models.PregnancyVisit> visits = databaseHelper
                    .getPregnancyVisitsByPatient(patientId);
            for (com.simats.ashasmartcare.models.PregnancyVisit v : visits) {
                if (v.isHighRisk()) {
                    String reason = v.getRiskFactors();
                    if (reason == null || reason.isEmpty())
                        reason = "High risk factors identified during visit.";
                    active.add(new AlertItem("High Risk Visit", reason, v.getVisitDate(), "High"));
                }
                if (v.getHemoglobin() > 0 && v.getHemoglobin() < 11.0) {
                    active.add(new AlertItem("Low Hemoglobin", "Reading: " + v.getHemoglobin() + " g/dL",
                            v.getVisitDate(), "Medium"));
                }
                if (v.getBloodPressureSystolic() >= 140 || v.getBloodPressureDiastolic() >= 90) {
                    active.add(new AlertItem("High Blood Pressure", "BP: " + v.getBloodPressure(), v.getVisitDate(),
                            "High"));
                }
            }
        }

        if (active.isEmpty()) {
            active.add(new AlertItem("No Active Alerts", "Patient vitals are within normal range.", "Today", "Normal"));
        }

        rvActive.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActive.setAdapter(new AlertAdapter(active, false));

        rvResolved.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResolved.setAdapter(new AlertAdapter(resolved, true));
    }

    private class AlertItem {
        String title, desc, date, status;

        AlertItem(String title, String desc, String date, String status) {
            this.title = title;
            this.desc = desc;
            this.date = date;
            this.status = status;
        }
    }

    private class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
        private List<AlertItem> items;
        private boolean isResolved;

        AlertAdapter(List<AlertItem> items, boolean isResolved) {
            this.items = items;
            this.isResolved = isResolved;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_hi_fi, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlertItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.desc);
            holder.tvDate.setText(item.date);
            holder.tvBadge.setText(item.status);

            if (isResolved) {
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#10B981"));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_completed);
                holder.tvBadge.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                if ("High".equalsIgnoreCase(item.status)) {
                    holder.ivIcon.setImageResource(R.drawable.ic_warning);
                    holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_high_risk);
                    holder.tvBadge.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                } else {
                    holder.ivIcon.setImageResource(R.drawable.ic_warning); // Or droplet
                    holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#F59E0B"));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_low);
                    holder.tvBadge.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
                }
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvDate, tvBadge;
            ImageView ivIcon;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_alert_title);
                tvDesc = v.findViewById(R.id.tv_alert_desc);
                tvDate = v.findViewById(R.id.tv_alert_date);
                tvBadge = v.findViewById(R.id.tv_alert_badge);
                ivIcon = v.findViewById(R.id.iv_alert_icon);
            }
        }
    }
}
