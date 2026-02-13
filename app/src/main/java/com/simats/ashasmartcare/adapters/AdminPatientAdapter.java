package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;
import com.simats.ashasmartcare.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class AdminPatientAdapter extends RecyclerView.Adapter<AdminPatientAdapter.PatientViewHolder> {

    private Context context;
    private List<Patient> patientList;
    private List<Patient> patientListFull;

    public AdminPatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
        this.patientListFull = new ArrayList<>(patientList);
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        holder.tvPatientName.setText(patient.getName());

        // Set risk badge based on patient for demo
        if (patient.isHighRisk()) {
            holder.tvRiskBadge.setText("High Risk");
            holder.tvRiskBadge.setTextColor(context.getResources().getColor(R.color.error));
            holder.tvRiskBadge.setBackgroundResource(R.drawable.bg_red_light);
        } else if (patient.getName().contains("Rajesh")) {
            holder.tvRiskBadge.setText("Medium Risk");
            holder.tvRiskBadge.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_pending);
        } else {
            holder.tvRiskBadge.setText("Low Risk");
            holder.tvRiskBadge.setTextColor(context.getResources().getColor(R.color.success));
            holder.tvRiskBadge.setBackgroundResource(R.drawable.bg_green_light);
        }

        // Set patient details based on category
        String details = "";
        if ("Pregnant".equalsIgnoreCase(patient.getCategory())) {
            details = "Pregnant (7th Mo) • " + patient.getAddress();
        } else if ("Child".equalsIgnoreCase(patient.getCategory())) {
            details = "Child (" + patient.getAge() + " yrs) • " + patient.getAddress();
        } else if ("Infant".equalsIgnoreCase(patient.getCategory())) {
            details = "Infant (2 weeks) • " + patient.getAddress();
        } else {
            details = patient.getCategory() + " • " + patient.getAddress();
        }
        holder.tvPatientDetails.setText(details);

        // Set alert messages based on patient
        if (patient.getName().contains("Lakshmi")) {
            holder.tvPatientAlert.setText("● Alert: Severe Anemia");
            holder.tvPatientAlert.setTextColor(context.getResources().getColor(R.color.error));
            holder.tvPatientAlert.setVisibility(View.VISIBLE);
        } else if (patient.getName().contains("Rohan")) {
            holder.tvPatientAlert.setText("Due for Vaccination");
            holder.tvPatientAlert.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.tvPatientAlert.setVisibility(View.VISIBLE);
        } else if (patient.getName().contains("Rajesh")) {
            holder.tvPatientAlert.setText("Follow-up Required");
            holder.tvPatientAlert.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.tvPatientAlert.setVisibility(View.VISIBLE);
        } else if (patient.getName().contains("Meena")) {
            holder.tvPatientAlert.setText("Checkup Completed");
            holder.tvPatientAlert.setTextColor(context.getResources().getColor(R.color.success));
            holder.tvPatientAlert.setVisibility(View.VISIBLE);
        } else if (patient.getName().contains("Baby")) {
            holder.tvPatientAlert.setText("● Low Birth Weight");
            holder.tvPatientAlert.setTextColor(context.getResources().getColor(R.color.error));
            holder.tvPatientAlert.setVisibility(View.VISIBLE);
        } else {
            holder.tvPatientAlert.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public void filter(String query) {
        patientList.clear();
        if (query.isEmpty()) {
            patientList.addAll(patientListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Patient patient : patientListFull) {
                if (patient.getName().toLowerCase().contains(lowerCaseQuery)) {
                    patientList.add(patient);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPatientAvatar;
        TextView tvPatientName;
        TextView tvRiskBadge;
        TextView tvPatientDetails;
        TextView tvPatientAlert;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPatientAvatar = itemView.findViewById(R.id.ivPatientAvatar);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvRiskBadge = itemView.findViewById(R.id.tvRiskBadge);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvPatientAlert = itemView.findViewById(R.id.tvPatientAlert);
        }
    }
}
