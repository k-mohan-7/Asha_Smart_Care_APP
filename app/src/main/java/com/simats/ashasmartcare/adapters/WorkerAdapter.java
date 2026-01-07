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
import com.simats.ashasmartcare.models.Worker;

import java.util.ArrayList;
import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    private Context context;
    private List<Worker> workerList;
    private List<Worker> workerListFull; // For search
    private OnWorkerActionListener listener;

    public interface OnWorkerActionListener {
        void onActivateClick(Worker worker);

        void onDeactivateClick(Worker worker);

        void onApproveClick(Worker worker);
    }

    public WorkerAdapter(Context context, List<Worker> workerList, OnWorkerActionListener listener) {
        this.context = context;
        this.workerList = workerList;
        this.workerListFull = new ArrayList<>(workerList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);

        holder.tvWorkerName.setText(worker.getName());
        holder.tvWorkerId.setText("Worker ID: " + worker.getWorkerId());
        holder.tvWorkerVillage.setText("Village: " + worker.getVillage());

        // Card click to open profile
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context,
                    com.simats.ashasmartcare.activities.WorkerProfileActivity.class);
            intent.putExtra("workerName", worker.getName());
            intent.putExtra("workerId", worker.getWorkerId());
            intent.putExtra("village", worker.getVillage());
            intent.putExtra("status", worker.getStatus());
            context.startActivity(intent);
        });

        // Update status badge and action button based on worker status
        if (worker.isPending()) {
            holder.btnActive.setText("Pending");
            holder.btnActive.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.btnActive.setBackgroundResource(R.drawable.bg_badge_pending);

            holder.btnAction.setText("Approve");
            holder.btnAction.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_primary);

            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApproveClick(worker);
                }
            });
        } else if (worker.isActive()) {
            holder.btnActive.setText("Active");
            holder.btnActive.setTextColor(context.getResources().getColor(R.color.success));
            holder.btnActive.setBackgroundResource(R.drawable.bg_green_light);

            holder.btnAction.setText("Deactivate");
            holder.btnAction.setTextColor(context.getResources().getColor(R.color.error));
            holder.btnAction.setBackgroundResource(R.drawable.bg_red_light);

            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeactivateClick(worker);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    public void filter(String query) {
        workerList.clear();
        if (query.isEmpty()) {
            workerList.addAll(workerListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Worker worker : workerListFull) {
                if (worker.getName().toLowerCase().contains(lowerCaseQuery) ||
                        worker.getWorkerId().toLowerCase().contains(lowerCaseQuery)) {
                    workerList.add(worker);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateWorkerList(List<Worker> newList) {
        this.workerList = newList;
        this.workerListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class WorkerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkerAvatar;
        TextView tvWorkerName;
        TextView tvWorkerId;
        TextView tvWorkerVillage;
        TextView btnActive;
        TextView btnAction;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkerAvatar = itemView.findViewById(R.id.ivWorkerAvatar);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvWorkerId = itemView.findViewById(R.id.tvWorkerId);
            tvWorkerVillage = itemView.findViewById(R.id.tvWorkerVillage);
            btnActive = itemView.findViewById(R.id.btnActive);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
