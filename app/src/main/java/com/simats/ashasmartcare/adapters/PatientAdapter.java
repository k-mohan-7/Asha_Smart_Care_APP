package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.Patient;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private Context context;
    private List<Patient> patientList;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
        void onCallClick(Patient patient);
    }

    public PatientAdapter(Context context, List<Patient> patientList, OnPatientClickListener listener) {
        this.context = context;
        this.patientList = patientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public void updateList(List<Patient> newList) {
        patientList.clear();
        patientList.addAll(newList);
        notifyDataSetChanged();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvInitial, tvName, tvAge, tvPhone, tvVillage, tvCategory, tvSyncStatus;
        ImageView ivCall, ivSyncIndicator;
        View categoryIndicator;

        PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardPatient);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvVillage = itemView.findViewById(R.id.tvVillage);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvSyncStatus = itemView.findViewById(R.id.tvSyncStatus);
            ivCall = itemView.findViewById(R.id.ivCall);
            ivSyncIndicator = itemView.findViewById(R.id.ivSyncIndicator);
            categoryIndicator = itemView.findViewById(R.id.categoryIndicator);
        }

        void bind(Patient patient) {
            // Set initial letter
            String name = patient.getName();
            if (name != null && !name.isEmpty()) {
                tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            } else {
                tvInitial.setText("?");
            }

            // Set name
            tvName.setText(name);

            // Set age and gender
            String ageGender = patient.getAge() + " yrs";
            if (patient.getGender() != null && !patient.getGender().isEmpty()) {
                ageGender += " • " + patient.getGender();
            }
            tvAge.setText(ageGender);

            // Set phone
            tvPhone.setText(patient.getPhone());

            // Set village
            tvVillage.setText(patient.getVillage());

            // Set category with color
            String category = patient.getCategory();
            tvCategory.setText(category);
            setCategoryColor(category);

            // Set sync status
            String syncStatus = patient.getSyncStatus();
            if ("SYNCED".equals(syncStatus)) {
                tvSyncStatus.setText("Synced");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.success));
                ivSyncIndicator.setColorFilter(context.getResources().getColor(R.color.success));
            } else if ("PENDING".equals(syncStatus)) {
                tvSyncStatus.setText("Pending");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.warning));
                ivSyncIndicator.setColorFilter(context.getResources().getColor(R.color.warning));
            } else {
                tvSyncStatus.setText("Failed");
                tvSyncStatus.setTextColor(context.getResources().getColor(R.color.error));
                ivSyncIndicator.setColorFilter(context.getResources().getColor(R.color.error));
            }

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPatientClick(patient);
                }
            });

            ivCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallClick(patient);
                }
            });
        }

        private void setCategoryColor(String category) {
            int color;
            switch (category) {
                case "Pregnant Woman":
                    color = context.getResources().getColor(R.color.category_pregnant);
                    break;
                case "Lactating Mother":
                    color = context.getResources().getColor(R.color.category_lactating);
                    break;
                case "Child (0-5 yrs)":
                    color = context.getResources().getColor(R.color.category_child);
                    break;
                case "Adolescent Girl":
                    color = context.getResources().getColor(R.color.category_adolescent);
                    break;
                case "General":
                default:
                    color = context.getResources().getColor(R.color.category_general);
                    break;
            }
            categoryIndicator.setBackgroundColor(color);
            tvCategory.setTextColor(color);
        }
    }
}
