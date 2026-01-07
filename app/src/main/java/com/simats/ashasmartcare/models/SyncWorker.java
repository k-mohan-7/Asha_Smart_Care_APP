package com.simats.ashasmartcare.models;

public class SyncWorker {
    private String name;
    private int pendingRecords;
    private String lastSync;
    private boolean isDelayed;

    public SyncWorker() {
    }

    public SyncWorker(String name, int pendingRecords, String lastSync, boolean isDelayed) {
        this.name = name;
        this.pendingRecords = pendingRecords;
        this.lastSync = lastSync;
        this.isDelayed = isDelayed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPendingRecords() {
        return pendingRecords;
    }

    public void setPendingRecords(int pendingRecords) {
        this.pendingRecords = pendingRecords;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    public void setDelayed(boolean delayed) {
        isDelayed = delayed;
    }
}
