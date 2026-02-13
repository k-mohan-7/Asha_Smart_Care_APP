package com.simats.ashasmartcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.ScheduleItem;

import java.util.ArrayList;
import java.util.List;

public class PatientOverviewFragment extends Fragment {

    private CardView cardAlert;
    private TextView tvAlertTitle, tvAlertMessage;
    private TextView tvBp, tvBpStatus, tvWeight, tvWeightChange, tvHemoglobin, tvHemoglobinStatus, tvFetalHeart;
    private RecyclerView rvSchedule;

    private int patientId;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_overview, container, false);

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        if (getArguments() != null) {
            patientId = getArguments().getInt("patient_id", -1);
        }

        initViews(view);
        loadData(view);
        setupSchedule();

        return view;
    }

    private void initViews(View view) {
        cardAlert = view.findViewById(R.id.card_alert);
        tvAlertTitle = view.findViewById(R.id.tv_alert_title);
        tvAlertMessage = view.findViewById(R.id.tv_alert_message);
        tvBp = view.findViewById(R.id.tv_bp);
        tvBpStatus = view.findViewById(R.id.tv_bp_status);
        tvWeight = view.findViewById(R.id.tv_weight);
        tvWeightChange = view.findViewById(R.id.tv_weight_change);
        tvHemoglobin = view.findViewById(R.id.tv_hemoglobin);
        tvHemoglobinStatus = view.findViewById(R.id.tv_hemoglobin_status);
        tvFetalHeart = view.findViewById(R.id.tv_fetal_heart);
        rvSchedule = view.findViewById(R.id.rv_schedule);
    }

    private void loadData(View view) {
        Patient patient = databaseHelper.getPatientById(patientId);
        String category = getArguments() != null ? getArguments().getString("category", "") : "";

        if (patient != null) {
            if ("child".equalsIgnoreCase(category)) {
                // Child vitals
                TextView tvLabelBp = view.findViewById(R.id.tv_label_bp);
                if (tvLabelBp != null)
                    tvLabelBp.setText("Height");

                tvBp.setText("72 cm");
                tvBpStatus.setText("Normal");
                tvBpStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));

                tvWeight.setText("8.5 kg");
                tvWeightChange.setText("Steady growth");

                tvHemoglobin.setText("11.2 g/dL");
                tvHemoglobinStatus.setText("Normal");
                tvHemoglobinStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));

                TextView tvLabelFetalHeart = view.findViewById(R.id.tv_label_fetal_heart);
                if (tvLabelFetalHeart != null)
                    tvLabelFetalHeart.setText("MUAC");

                tvFetalHeart.setText("13.5 cm");

                tvAlertTitle.setText("Growth Tracking");
                tvAlertMessage.setText("Rohan is following the expected growth curve. No immediate concerns.");
                cardAlert.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FDF4"));

                tvAlertTitle.setTextColor(android.graphics.Color.parseColor("#166534"));
                tvAlertMessage.setTextColor(android.graphics.Color.parseColor("#15803D"));
            } else {
                // Pregnancy vitals (existing)
                tvBp.setText("140/90");
                tvBpStatus.setText("High");
                tvWeight.setText("62 kg");
                tvWeightChange.setText("+2kg gain");
                tvHemoglobin.setText("10.5 g/dL");
                tvHemoglobinStatus.setText("Low");
                tvFetalHeart.setText("142 bpm");
                cardAlert.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupSchedule() {
        List<ScheduleItem> items = new ArrayList<>();
        items.add(new ScheduleItem("ANC Visit 3", "Oct 24, 2023", "Due Soon"));
        items.add(new ScheduleItem("Tetanus Shot", "Nov 15, 2023", "Upcoming"));

        rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedule.setAdapter(new ScheduleAdapter(items));
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
        private List<ScheduleItem> items;

        ScheduleAdapter(List<ScheduleItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScheduleItem item = items.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvDate.setText("Due: " + item.getDueDate());
            holder.tvStatus.setText(item.getStatus());

            if ("Upcoming".equalsIgnoreCase(item.getStatus())) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_general);
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4B5563"));
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_due_soon);
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvStatus;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_schedule_title);
                tvDate = v.findViewById(R.id.tv_schedule_date);
                tvStatus = v.findViewById(R.id.tv_schedule_status);
            }
        }
    }
}
