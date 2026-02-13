package com.simats.ashasmartcare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.PatientProfileActivity;
import com.simats.ashasmartcare.R;

import java.util.ArrayList;
import java.util.List;

public class GroupedPatientAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_PATIENT = 1;

    private Context context;
    private List<Object> items; // Mix of WorkerHeader and PatientItem

    public static class WorkerHeader {
        public String workerName;
        public int patientCount;

        public WorkerHeader(String workerName, int patientCount) {
            this.workerName = workerName;
            this.patientCount = patientCount;
        }
    }

    public static class PatientItem {
        public int id;
        public String name;
        public int age;
        public String category;
        public String address;

        public PatientItem(int id, String name, int age, String category, String address) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.category = category;
            this.address = address;
        }
    }

    public GroupedPatientAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    public void setData(List<Object> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof WorkerHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_PATIENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_worker_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_grouped_patient, parent, false);
            return new PatientViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            WorkerHeader header = (WorkerHeader) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof PatientViewHolder) {
            PatientItem patient = (PatientItem) items.get(position);
            ((PatientViewHolder) holder).bind(patient);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkerName;
        TextView tvPatientCount;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvPatientCount = itemView.findViewById(R.id.tvPatientCount);
        }

        void bind(WorkerHeader header) {
            tvWorkerName.setText(header.workerName);
            tvPatientCount.setText(header.patientCount + " patients");
        }
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName;
        TextView tvPatientAge;
        TextView tvPatientCategory;

        PatientViewHolder(View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientAge = itemView.findViewById(R.id.tvPatientAge);
            tvPatientCategory = itemView.findViewById(R.id.tvPatientCategory);
        }

        void bind(PatientItem patient) {
            tvPatientName.setText(patient.name);
            tvPatientAge.setText(patient.age + " years");
            tvPatientCategory.setText(patient.category);

            // Add click listener to navigate to patient profile
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PatientProfileActivity.class);
                intent.putExtra("patient_id", patient.id);
                context.startActivity(intent);
            });
        }
    }
}
