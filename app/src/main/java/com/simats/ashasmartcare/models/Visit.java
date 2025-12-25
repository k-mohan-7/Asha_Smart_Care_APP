package com.simats.ashasmartcare.models;

import com.google.gson.annotations.SerializedName;

/**
 * Visit model class for tracking general visits
 */
public class Visit {

    @SerializedName("local_id")
    private long localId;

    @SerializedName("server_id")
    private int serverId;

    @SerializedName("patient_id")
    private long patientId;

    @SerializedName("visit_date")
    private String visitDate;

    @SerializedName("visit_type")
    private String visitType;

    @SerializedName("purpose")
    private String purpose;

    @SerializedName("findings")
    private String findings;

    @SerializedName("recommendations")
    private String recommendations;

    @SerializedName("next_visit_date")
    private String nextVisitDate;

    @SerializedName("notes")
    private String notes;
    
    @SerializedName("description")
    private String description;

    @SerializedName("sync_status")
    private String syncStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_updated")
    private String lastUpdated;

    // Patient name for display (not stored in DB)
    private String patientName;

    // Constructors
    public Visit() {
        this.syncStatus = "PENDING";
    }

    public Visit(long patientId, String visitDate, String visitType, String purpose) {
        this.patientId = patientId;
        this.visitDate = visitDate;
        this.visitType = visitType;
        this.purpose = purpose;
        this.syncStatus = "PENDING";
    }

    // Getters and Setters
    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }
    
    // ID alias
    public long getId() {
        return localId;
    }
    
    public void setId(long id) {
        this.localId = id;
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

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(String nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    public boolean isSynced() {
        return "SYNCED".equals(syncStatus);
    }

    @Override
    public String toString() {
        return "Visit{" +
                "localId=" + localId +
                ", patientId=" + patientId +
                ", visitDate='" + visitDate + '\'' +
                ", visitType='" + visitType + '\'' +
                '}';
    }
}
