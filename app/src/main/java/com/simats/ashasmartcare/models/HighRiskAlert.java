package com.simats.ashasmartcare.models;

public class HighRiskAlert {
    private String patientName;
    private String village;
    private String alertType;
    private boolean isReviewed;

    public HighRiskAlert() {
        this.isReviewed = false;
    }

    public HighRiskAlert(String patientName, String village, String alertType) {
        this.patientName = patientName;
        this.village = village;
        this.alertType = alertType;
        this.isReviewed = false;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public boolean isReviewed() {
        return isReviewed;
    }

    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }
}
