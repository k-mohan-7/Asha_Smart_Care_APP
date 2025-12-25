package com.simats.ashasmartcare.models;

import com.google.gson.annotations.SerializedName;

/**
 * Vaccination model class for tracking vaccinations
 */
public class Vaccination {

    // Vaccination status constants
    public static final String STATUS_DUE = "DUE";
    public static final String STATUS_UPCOMING = "UPCOMING";
    public static final String STATUS_OVERDUE = "OVERDUE";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @SerializedName("local_id")
    private long localId;

    @SerializedName("server_id")
    private int serverId;

    @SerializedName("patient_id")
    private long patientId;

    @SerializedName("vaccine_name")
    private String vaccineName;

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("given_date")
    private String givenDate;

    @SerializedName("status")
    private String status;

    @SerializedName("batch_number")
    private String batchNumber;

    @SerializedName("notes")
    private String notes;
    
    @SerializedName("side_effects")
    private String sideEffects;

    @SerializedName("sync_status")
    private String syncStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_updated")
    private String lastUpdated;

    // Patient name for display (not stored in DB)
    private String patientName;

    // Constructors
    public Vaccination() {
        this.syncStatus = "PENDING";
        this.status = STATUS_UPCOMING;
    }

    public Vaccination(long patientId, String vaccineName, String dueDate) {
        this.patientId = patientId;
        this.vaccineName = vaccineName;
        this.dueDate = dueDate;
        this.status = STATUS_UPCOMING;
        this.syncStatus = "PENDING";
    }

    // Getters and Setters
    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    // Alias for scheduled_date compatibility
    public String getScheduledDate() {
        return dueDate;
    }
    
    public void setScheduledDate(String scheduledDate) {
        this.dueDate = scheduledDate;
    }

    public String getGivenDate() {
        return givenDate;
    }

    public void setGivenDate(String givenDate) {
        this.givenDate = givenDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getSideEffects() {
        return sideEffects;
    }
    
    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }
    
    // Alias methods for ID compatibility
    public long getId() {
        return localId;
    }
    
    public void setId(long id) {
        this.localId = id;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    // Helper methods
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isOverdue() {
        return STATUS_OVERDUE.equals(status);
    }

    public boolean isDue() {
        return STATUS_DUE.equals(status);
    }

    public boolean isUpcoming() {
        return STATUS_UPCOMING.equals(status);
    }

    public boolean isSynced() {
        return "SYNCED".equals(syncStatus);
    }

    /**
     * Mark vaccination as completed
     */
    public void markCompleted(String givenDate, String batchNumber) {
        this.givenDate = givenDate;
        this.batchNumber = batchNumber;
        this.status = STATUS_COMPLETED;
        this.syncStatus = "PENDING";
    }

    @Override
    public String toString() {
        return "Vaccination{" +
                "localId=" + localId +
                ", patientId=" + patientId +
                ", vaccineName='" + vaccineName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
