package com.simats.ashasmartcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PatientGrowthFragment extends Fragment {

    private TextView tvCurrentWeight, tvWeightChange, tvCurrentHb, tvHbStatus, tvLastChecked;
    private RecyclerView rvHistory;
    private int patientId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_growth, container, false);

        if (getArguments() != null) {
            patientId = getArguments().getInt("patient_id", -1);
        }

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        initViews(view);
        loadSummary(view);
        setupHistory();

        return view;
    }

    private DatabaseHelper databaseHelper;

    private void initViews(View view) {
        tvCurrentWeight = view.findViewById(R.id.tv_current_weight);
        tvWeightChange = view.findViewById(R.id.tv_weight_change);
        tvCurrentHb = view.findViewById(R.id.tv_current_hemoglobin);
        tvHbStatus = view.findViewById(R.id.tv_hemoglobin_status);
        tvLastChecked = view.findViewById(R.id.tv_last_checked);
        rvHistory = view.findViewById(R.id.rv_measurements);
    }

    private void loadSummary(View view) {
        String category = getArguments() != null ? getArguments().getString("category", "") : "";

        if ("child".equalsIgnoreCase(category)) {
            // Child Growth Metrics
            ((TextView) view.findViewById(R.id.tv_label_hemoglobin)).setText("Height");
            ((TextView) view.findViewById(R.id.tv_unit_hemoglobin)).setText("cm");
            ((ImageView) view.findViewById(R.id.iv_icon_hemoglobin)).setImageResource(R.drawable.ic_scale);
            ((ImageView) view.findViewById(R.id.iv_icon_hemoglobin))
                    .setColorFilter(android.graphics.Color.parseColor("#3B82F6"));

            List<com.simats.ashasmartcare.models.ChildGrowth> records = databaseHelper
                    .getChildGrowthByPatient(patientId);
            if (!records.isEmpty()) {
                com.simats.ashasmartcare.models.ChildGrowth latest = records.get(0);
                tvCurrentWeight.setText(String.valueOf(latest.getWeight()));
                tvCurrentHb.setText(String.valueOf(latest.getHeight())); // Reusing Hb field for Height

                // Calculate change if enough records
                if (records.size() > 1) {
                    double diff = latest.getWeight() - records.get(1).getWeight();
                    String diffStr = (diff >= 0 ? "+ " : "- ") + Math.abs(diff) + "kg";
                    tvWeightChange.setText(diffStr);
                } else {
                    tvWeightChange.setText("-");
                }

                tvHbStatus.setText(latest.getGrowthStatus());
                tvLastChecked.setText("Last checked: " + latest.getRecordDate());
            } else {
                tvCurrentWeight.setText("-");
                tvCurrentHb.setText("-");
                tvWeightChange.setText("-");
                tvHbStatus.setText("-");
                tvLastChecked.setText("No records");
            }
        } else {
            // Pregnancy Growth Metrics
            List<com.simats.ashasmartcare.models.PregnancyVisit> visits = databaseHelper
                    .getPregnancyVisitsByPatient(patientId);
            if (!visits.isEmpty()) {
                com.simats.ashasmartcare.models.PregnancyVisit latest = visits.get(0);
                tvCurrentWeight.setText(String.valueOf(latest.getWeight()));
                tvCurrentHb.setText(String.valueOf(latest.getHemoglobin()));

                if (visits.size() > 1) {
                    double diff = latest.getWeight() - visits.get(1).getWeight();
                    String diffStr = (diff >= 0 ? "+ " : "- ") + Math.abs(diff) + "kg";
                    tvWeightChange.setText(diffStr);
                } else {
                    tvWeightChange.setText("-");
                }

                if (latest.getHemoglobin() < 11.0) {
                    tvHbStatus.setText("Low");
                    tvHbStatus.setBackgroundResource(R.drawable.bg_badge_missed);
                    tvHbStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                } else {
                    tvHbStatus.setText("Normal");
                    tvHbStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                    tvHbStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
                }
                tvLastChecked.setText("Last checked: " + latest.getVisitDate());
            } else {
                tvCurrentWeight.setText("-");
                tvCurrentHb.setText("-");
                tvWeightChange.setText("-");
                tvHbStatus.setText("-");
                tvLastChecked.setText("No records");
            }
        }
    }

    private void setupHistory() {
        String category = getArguments() != null ? getArguments().getString("category", "") : "";
        List<MeasurementItem> items = new ArrayList<>();

        if ("child".equalsIgnoreCase(category)) {
            List<com.simats.ashasmartcare.models.ChildGrowth> records = databaseHelper
                    .getChildGrowthByPatient(patientId);
            for (com.simats.ashasmartcare.models.ChildGrowth r : records) {
                String date = r.getRecordDate(); // YYYY-MM-DD
                String month = "JAN";
                String day = "01";
                try {
                    String[] parts = date.split("-");
                    if (parts.length >= 3) {
                        // Simple month mapping (can use SimpleDateFormat but keeping it simple/fast)
                        int m = Integer.parseInt(parts[1]);
                        String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV",
                                "DEC" };
                        if (m >= 1 && m <= 12)
                            month = months[m - 1];
                        day = parts[2];
                    }
                } catch (Exception e) {
                }

                items.add(
                        new MeasurementItem(month, day, "Growth Record", r.getWeight() + " kg", r.getHeight() + " cm"));
            }
        } else {
            List<com.simats.ashasmartcare.models.PregnancyVisit> visits = databaseHelper
                    .getPregnancyVisitsByPatient(patientId);
            for (com.simats.ashasmartcare.models.PregnancyVisit v : visits) {
                String date = v.getVisitDate();
                String month = "JAN";
                String day = "01";
                try {
                    String[] parts = date.split("-");
                    if (parts.length >= 3) {
                        int m = Integer.parseInt(parts[1]);
                        String[] months = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV",
                                "DEC" };
                        if (m >= 1 && m <= 12)
                            month = months[m - 1];
                        day = parts[2];
                    }
                } catch (Exception e) {
                }

                String title = v.getWeeksPregnant() > 0 ? "Week " + v.getWeeksPregnant() : "ANC Checkup";
                items.add(new MeasurementItem(month, day, title, v.getWeight() + " kg", v.getHemoglobin() + " g/dL"));
            }
        }

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(new HistoryAdapter(items));
    }

    private class MeasurementItem {
        String month, day, title, weight, hb;

        MeasurementItem(String month, String day, String title, String weight, String hb) {
            this.month = month;
            this.day = day;
            this.title = title;
            this.weight = weight;
            this.hb = hb;
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<MeasurementItem> items;

        HistoryAdapter(List<MeasurementItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_measurement_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MeasurementItem item = items.get(position);
            holder.tvMonth.setText(item.month);
            holder.tvDay.setText(item.day);
            holder.tvTitle.setText(item.title);
            holder.tvWeight.setText(item.weight);
            holder.tvHb.setText(item.hb);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMonth, tvDay, tvTitle, tvWeight, tvHb;

            ViewHolder(View v) {
                super(v);
                tvMonth = v.findViewById(R.id.tv_month);
                tvDay = v.findViewById(R.id.tv_day);
                tvTitle = v.findViewById(R.id.tv_checkup_title);
                tvWeight = v.findViewById(R.id.tv_checkup_weight);
                tvHb = v.findViewById(R.id.tv_checkup_hb);
            }
        }
    }
}
