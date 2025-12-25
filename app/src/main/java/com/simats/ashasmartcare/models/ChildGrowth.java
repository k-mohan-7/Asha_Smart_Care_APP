package com.simats.ashasmartcare.models;

import com.google.gson.annotations.SerializedName;

/**
 * ChildGrowth model class for tracking child growth records
 */
public class ChildGrowth {

    @SerializedName("local_id")
    private long localId;

    @SerializedName("server_id")
    private int serverId;

    @SerializedName("patient_id")
    private long patientId;

    @SerializedName("record_date")
    private String recordDate;

    @SerializedName("weight")
    private float weight;

    @SerializedName("height")
    private float height;

    @SerializedName("head_circumference")
    private float headCircumference;
    
    @SerializedName("muac")
    private float muac;
    
    @SerializedName("age_months")
    private int ageMonths;
    
    @SerializedName("nutritional_status")
    private String nutritionalStatus;
    
    @SerializedName("milestones")
    private String milestones;

    @SerializedName("growth_status")
    private String growthStatus;

    @SerializedName("notes")
    private String notes;

    @SerializedName("sync_status")
    private String syncStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_updated")
    private String lastUpdated;

    // Patient name for display (not stored in DB)
    private String patientName;
    private int patientAge;

    // Constructors
    public ChildGrowth() {
        this.syncStatus = "PENDING";
    }

    public ChildGrowth(long patientId, String recordDate, float weight, float height) {
        this.patientId = patientId;
        this.recordDate = recordDate;
        this.weight = weight;
        this.height = height;
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

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }
    
    // Alias for recordDate
    public String getMeasurementDate() {
        return recordDate;
    }
    
    public void setMeasurementDate(String date) {
        this.recordDate = date;
    }
    
    // Compatibility methods
    public void setPatientServerId(String id) {
        // Compatibility method - no-op
    }
    
    public void setPatientServerId(int id) {
        // Compatibility method - no-op
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getHeadCircumference() {
        return headCircumference;
    }

    public void setHeadCircumference(float headCircumference) {
        this.headCircumference = headCircumference;
    }
    
    public float getMuac() {
        return muac;
    }
    
    public void setMuac(float muac) {
        this.muac = muac;
    }
    
    public int getAgeMonths() {
        return ageMonths;
    }
    
    public void setAgeMonths(int ageMonths) {
        this.ageMonths = ageMonths;
    }
    
    public String getNutritionalStatus() {
        return nutritionalStatus;
    }
    
    public void setNutritionalStatus(String nutritionalStatus) {
        this.nutritionalStatus = nutritionalStatus;
    }
    
    public String getMilestones() {
        return milestones;
    }
    
    public void setMilestones(String milestones) {
        this.milestones = milestones;
    }
    
    // ID alias
    public long getId() {
        return localId;
    }
    
    public void setId(long id) {
        this.localId = id;
    }

    public String getGrowthStatus() {
        return growthStatus;
    }

    public void setGrowthStatus(String growthStatus) {
        this.growthStatus = growthStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public int getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    // Helper methods
    public String getWeightFormatted() {
        return weight + " kg";
    }

    public String getHeightFormatted() {
        return height + " cm";
    }

    public String getHeadCircumferenceFormatted() {
        return headCircumference + " cm";
    }

    public boolean isSynced() {
        return "SYNCED".equals(syncStatus);
    }

    /**
     * Calculate BMI for children
     */
    public float calculateBMI() {
        if (height > 0) {
            float heightInMeters = height / 100;
            return weight / (heightInMeters * heightInMeters);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "ChildGrowth{" +
                "localId=" + localId +
                ", patientId=" + patientId +
                ", recordDate='" + recordDate + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                '}';
    }
}
