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
import com.simats.ashasmartcare.models.SyncWorker;

import java.util.ArrayList;
import java.util.List;

public class SyncWorkerAdapter extends RecyclerView.Adapter<SyncWorkerAdapter.SyncViewHolder> {

    private Context context;
    private List<SyncWorker> workerList;
    private List<SyncWorker> workerListFull;

    public SyncWorkerAdapter(Context context, List<SyncWorker> workerList) {
        this.context = context;
        this.workerList = workerList;
        this.workerListFull = new ArrayList<>(workerList);
    }

    public void setData(List<SyncWorker> newData) {
        this.workerListFull = new ArrayList<>(newData);
        this.workerList.clear();
        this.workerList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SyncViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sync_worker, parent, false);
        return new SyncViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SyncViewHolder holder, int position) {
        SyncWorker worker = workerList.get(position);

        holder.tvWorkerName.setText(worker.getName());
        holder.tvPendingRecords.setText("Pending Records: " + worker.getPendingRecords());
        holder.tvLastSync.setText("Last Sync: " + worker.getLastSync());

        // Set status indicator color
        if (worker.isDelayed()) {
            holder.viewStatusIndicator.setBackgroundResource(R.drawable.circle_orange_bg);
        } else {
            holder.viewStatusIndicator.setBackgroundResource(R.drawable.circle_blue_bg);
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
            for (SyncWorker worker : workerListFull) {
                if (worker.getName().toLowerCase().contains(lowerCaseQuery)) {
                    workerList.add(worker);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        workerList.clear();
        if (status.equals("all")) {
            workerList.addAll(workerListFull);
        } else if (status.equals("synced")) {
            for (SyncWorker worker : workerListFull) {
                if (worker.getPendingRecords() == 0) {
                    workerList.add(worker);
                }
            }
        } else if (status.equals("delayed")) {
            for (SyncWorker worker : workerListFull) {
                if (worker.isDelayed()) {
                    workerList.add(worker);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class SyncViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkerAvatar;
        TextView tvWorkerName;
        TextView tvPendingRecords;
        TextView tvLastSync;
        View viewStatusIndicator;

        public SyncViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkerAvatar = itemView.findViewById(R.id.ivWorkerAvatar);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvPendingRecords = itemView.findViewById(R.id.tvPendingRecords);
            tvLastSync = itemView.findViewById(R.id.tvLastSync);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
        }
    }
}
