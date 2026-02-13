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
        TextView tvPatientName, tvPatientInfo;
        View layoutPendingBadge, layoutSyncedBadge;
        TextView tvCategoryPregnant, tvCategoryChild, tvCategoryHighRisk, tvCategoryGeneral;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvPatientInfo = itemView.findViewById(R.id.tv_patient_info);
            layoutPendingBadge = itemView.findViewById(R.id.layout_pending_badge);
            layoutSyncedBadge = itemView.findViewById(R.id.layout_synced_badge);
            tvCategoryPregnant = itemView.findViewById(R.id.tv_category_pregnant);
            tvCategoryChild = itemView.findViewById(R.id.tv_category_child);
            tvCategoryHighRisk = itemView.findViewById(R.id.tv_category_high_risk);
            tvCategoryGeneral = itemView.findViewById(R.id.tv_category_general);
        }

        void bind(Patient patient) {
            tvPatientName.setText(patient.getName());

            // Age and address
            String info = patient.getAge() + " years";
            if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
                info += " • " + patient.getAddress();
            }
            tvPatientInfo.setText(info);

            // Hide all category chips initially
            tvCategoryPregnant.setVisibility(View.GONE);
            tvCategoryChild.setVisibility(View.GONE);
            tvCategoryHighRisk.setVisibility(View.GONE);
            tvCategoryGeneral.setVisibility(View.GONE);

            // Category logic matching screenshot
            String category = patient.getCategory();
            if (category != null && !category.isEmpty()) {
                if (category.toLowerCase().contains("pregnant")) {
                    tvCategoryPregnant.setVisibility(View.VISIBLE);
                    // Pregnant are often high risk in this app's logic
                    tvCategoryHighRisk.setVisibility(View.VISIBLE);
                } else if (category.toLowerCase().contains("child")) {
                    tvCategoryChild.setVisibility(View.VISIBLE);
                } else {
                    tvCategoryGeneral.setVisibility(View.VISIBLE);
                }
            } else {
                tvCategoryGeneral.setVisibility(View.VISIBLE);
            }

            // Sync status badge
            if ("PENDING".equals(patient.getSyncStatus())) {
                layoutPendingBadge.setVisibility(View.VISIBLE);
                layoutSyncedBadge.setVisibility(View.GONE);
            } else if ("SYNCED".equals(patient.getSyncStatus())) {
                layoutPendingBadge.setVisibility(View.GONE);
                layoutSyncedBadge.setVisibility(View.VISIBLE);
            } else {
                layoutPendingBadge.setVisibility(View.GONE);
                layoutSyncedBadge.setVisibility(View.GONE);
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
