package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.Patient;

import java.util.List;

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.ViewHolder> {

    private Context context;
    private List<Patient> patients;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientsAdapter(Context context, List<Patient> patients, OnPatientClickListener listener) {
        this.context = context;
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvPatientInfo, tvCategoryBadge, tvRiskBadge, tvPendingBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvPatientInfo = itemView.findViewById(R.id.tv_patient_info);
            tvCategoryBadge = itemView.findViewById(R.id.tv_category_badge);
            tvRiskBadge = itemView.findViewById(R.id.tv_risk_badge);
            tvPendingBadge = itemView.findViewById(R.id.tv_pending_badge);
        }

        void bind(Patient patient) {
            tvPatientName.setText(patient.getName());

            // Age and village
            String info = patient.getAge() + " years";
            if (patient.getArea() != null && !patient.getArea().isEmpty()) {
                info += " • " + patient.getArea();
            }
            tvPatientInfo.setText(info);

            // Category badge
            String category = patient.getCategory();
            if (category != null && !category.isEmpty()) {
                tvCategoryBadge.setVisibility(View.VISIBLE);
                if (category.contains("Pregnant")) {
                    tvCategoryBadge.setText("Pregnant");
                } else if (category.contains("Child")) {
                    tvCategoryBadge.setText("Child");
                } else {
                    tvCategoryBadge.setText("General");
                }
            } else {
                tvCategoryBadge.setVisibility(View.GONE);
            }

            // High risk badge (show for pregnant women)
            if (category != null && category.contains("Pregnant")) {
                tvRiskBadge.setVisibility(View.VISIBLE);
            } else {
                tvRiskBadge.setVisibility(View.GONE);
            }

            // Pending sync badge
            if ("PENDING".equals(patient.getSyncStatus())) {
                tvPendingBadge.setVisibility(View.VISIBLE);
            } else {
                tvPendingBadge.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPatientClick(patient);
                }
            });
        }
    }
}
