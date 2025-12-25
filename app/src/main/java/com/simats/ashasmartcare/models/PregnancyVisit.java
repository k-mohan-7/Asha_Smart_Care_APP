package com.simats.ashasmartcare.models;

import com.google.gson.annotations.SerializedName;

/**
 * PregnancyVisit model class for tracking pregnancy visits
 */
public class PregnancyVisit {

    @SerializedName("local_id")
    private long localId;

    @SerializedName("server_id")
    private int serverId;

    @SerializedName("patient_id")
    private long patientId;

    @SerializedName("visit_date")
    private String visitDate;

    @SerializedName("gestational_weeks")
    private int gestationalWeeks;

    @SerializedName("weight")
    private double weight;

    @SerializedName("blood_pressure")
    private String bloodPressure;

    @SerializedName("hemoglobin")
    private double hemoglobin;

    @SerializedName("fetal_heart_rate")
    private int fetalHeartRate;

    @SerializedName("fundal_height")
    private double fundalHeight;

    @SerializedName("urine_protein")
    private String urineProtein;

    @SerializedName("urine_sugar")
    private String urineSugar;

    @SerializedName("urine_albumin")
    private String urineAlbumin;

    @SerializedName("is_high_risk")
    private boolean isHighRisk;

    @SerializedName("high_risk_reason")
    private String highRiskReason;

    @SerializedName("advice")
    private String advice;

    @SerializedName("next_visit_date")
    private String nextVisitDate;

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

    // Constructors
    public PregnancyVisit() {
        this.syncStatus = "PENDING";
    }

    public PregnancyVisit(long patientId, String visitDate) {
        this.patientId = patientId;
        this.visitDate = visitDate;
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

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public int getGestationalWeeks() {
        return gestationalWeeks;
    }

    public void setGestationalWeeks(int gestationalWeeks) {
        this.gestationalWeeks = gestationalWeeks;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public double getHemoglobin() {
        return hemoglobin;
    }

    public void setHemoglobin(double hemoglobin) {
        this.hemoglobin = hemoglobin;
    }

    public int getFetalHeartRate() {
        return fetalHeartRate;
    }

    public void setFetalHeartRate(int fetalHeartRate) {
        this.fetalHeartRate = fetalHeartRate;
    }

    public double getFundalHeight() {
        return fundalHeight;
    }

    public void setFundalHeight(double fundalHeight) {
        this.fundalHeight = fundalHeight;
    }

    public String getUrineProtein() {
        return urineProtein;
    }

    public void setUrineProtein(String urineProtein) {
        this.urineProtein = urineProtein;
    }

    public String getUrineSugar() {
        return urineSugar;
    }

    public void setUrineSugar(String urineSugar) {
        this.urineSugar = urineSugar;
    }

    public String getUrineAlbumin() {
        return urineAlbumin;
    }

    public void setUrineAlbumin(String urineAlbumin) {
        this.urineAlbumin = urineAlbumin;
    }

    public boolean isHighRisk() {
        return isHighRisk;
    }

    public void setHighRisk(boolean highRisk) {
        isHighRisk = highRisk;
    }

    public String getHighRiskReason() {
        return highRiskReason;
    }

    public void setHighRiskReason(String highRiskReason) {
        this.highRiskReason = highRiskReason;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
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

    // ID aliases for compatibility
    public long getId() {
        return localId;
    }
    
    public void setId(long id) {
        this.localId = id;
    }
    
    // Compatibility methods (for older code using different field names)
    public void setServerId(String id) {
        try {
            this.serverId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            this.serverId = 0;
        }
    }
    
    public void setPatientServerId(String id) {
        // Compatibility method - no-op
    }
    
    public void setWeeksPregnant(int weeks) {
        this.gestationalWeeks = weeks;
    }
    
    public int getWeeksPregnant() {
        return gestationalWeeks;
    }
    
    public void setBloodPressureSystolic(int systolic) {
        // Store in bloodPressure field
        String current = bloodPressure != null ? bloodPressure : "";
        String[] parts = current.split("/");
        String diastolic = parts.length > 1 ? parts[1] : "0";
        this.bloodPressure = systolic + "/" + diastolic;
    }
    
    public void setBloodPressureDiastolic(int diastolic) {
        // Store in bloodPressure field
        String current = bloodPressure != null ? bloodPressure : "";
        String[] parts = current.split("/");
        String systolic = parts.length > 0 ? parts[0] : "0";
        this.bloodPressure = systolic + "/" + diastolic;
    }
    
    public int getBloodPressureSystolic() {
        if (bloodPressure == null) return 0;
        String[] parts = bloodPressure.split("/");
        try {
            return parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public int getBloodPressureDiastolic() {
        if (bloodPressure == null) return 0;
        String[] parts = bloodPressure.split("/");
        try {
            return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void setComplaints(String complaints) {
        // Store in notes for now
        if (complaints != null && !complaints.isEmpty()) {
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Complaints: " + complaints;
        }
    }
    
    public String getComplaints() {
        return notes; // Return notes as complaints for compatibility
    }
    
    public void setRiskFactors(String riskFactors) {
        this.highRiskReason = riskFactors;
    }
    
    public String getRiskFactors() {
        return highRiskReason;
    }

    // Helper methods
    public String getWeightFormatted() {
        return weight + " kg";
    }

    public boolean isSynced() {
        return "SYNCED".equals(syncStatus);
    }

    @Override
    public String toString() {
        return "PregnancyVisit{" +
                "localId=" + localId +
                ", patientId=" + patientId +
                ", visitDate='" + visitDate + '\'' +
                ", gestationalWeeks=" + gestationalWeeks +
                ", isHighRisk=" + isHighRisk +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
