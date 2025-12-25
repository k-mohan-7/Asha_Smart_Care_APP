package com.simats.ashasmartcare.models;

import com.google.gson.annotations.SerializedName;

/**
 * SyncRecord model class for tracking sync queue items
 */
public class SyncRecord {

    @SerializedName("local_id")
    private long localId;

    @SerializedName("table_name")
    private String tableName;

    @SerializedName("record_id")
    private long recordId;

    @SerializedName("action")
    private String action;

    @SerializedName("data_json")
    private String dataJson;

    @SerializedName("sync_status")
    private String syncStatus;

    @SerializedName("error_message")
    private String errorMessage;

    @SerializedName("retry_count")
    private int retryCount;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_updated")
    private String lastUpdated;

    // Constructors
    public SyncRecord() {
        this.syncStatus = "PENDING";
        this.retryCount = 0;
    }

    public SyncRecord(String tableName, long recordId, String action, String dataJson) {
        this.tableName = tableName;
        this.recordId = recordId;
        this.action = action;
        this.dataJson = dataJson;
        this.syncStatus = "PENDING";
        this.retryCount = 0;
    }

    // Getters and Setters
    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public void setLastSyncAttempt(long timestamp) {
        // Compatibility method - uses lastUpdated
        this.lastUpdated = String.valueOf(timestamp);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // ID alias methods
    public long getId() {
        return localId;
    }
    
    public void setId(long id) {
        this.localId = id;
    }

    // Helper methods
    public boolean isPending() {
        return "PENDING".equals(syncStatus);
    }

    public boolean isFailed() {
        return "FAILED".equals(syncStatus);
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public String toString() {
        return "SyncRecord{" +
                "localId=" + localId +
                ", tableName='" + tableName + '\'' +
                ", recordId=" + recordId +
                ", action='" + action + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
