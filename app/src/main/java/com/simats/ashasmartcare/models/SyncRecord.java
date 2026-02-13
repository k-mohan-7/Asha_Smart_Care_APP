package com.simats.ashasmartcare.models;

public class SyncRecord {
    private long localId;
    private int serverId;
    private String tableName;
    private long recordId;
    private String action;
    private String dataJson;
    private String syncStatus;
    private String errorMessage;
    private int retryCount;
    private long lastSyncAttempt;
    private String createdAt;
    private String lastUpdated;

    // UI specific fields (optional, can be derived)
    private String title;
    private String timestamp;
    private boolean isSynced;
    private String type;

    public SyncRecord() {
    }

    // Constructor for UI list
    public SyncRecord(String title, String timestamp, boolean isSynced, String type) {
        this.title = title;
        this.timestamp = timestamp;
        this.isSynced = isSynced;
        this.type = type;
        this.syncStatus = isSynced ? "SYNCED" : "PENDING"; // Default fallback
    }

    public SyncRecord(String title, String timestamp, String status, String type) {
        this.title = title;
        this.timestamp = timestamp;
        this.syncStatus = status;
        this.isSynced = "SYNCED".equalsIgnoreCase(status);
        this.type = type;
    }

    // Getters and Setters
    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public long getId() {
        return localId;
    } // Alias for getLocalId as used in SyncService

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
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
        this.isSynced = "SYNCED".equalsIgnoreCase(syncStatus);
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

    public long getLastSyncAttempt() {
        return lastSyncAttempt;
    }

    public void setLastSyncAttempt(long lastSyncAttempt) {
        this.lastSyncAttempt = lastSyncAttempt;
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

    // UI Getters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
