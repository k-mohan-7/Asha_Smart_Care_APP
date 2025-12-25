package com.simats.ashasmartcare.models;

import com.google.gson.annotations.SerializedName;

/**
 * Patient model class representing a person in the healthcare system
 * Categories: Child, Pregnant Woman, Adult
 */
public class Patient {

    @SerializedName("local_id")
    private long localId;

    @SerializedName("server_id")
    private int serverId;

    @SerializedName("name")
    private String name;

    @SerializedName("age")
    private int age;

    @SerializedName("dob")
    private String dob;

    @SerializedName("gender")
    private String gender;

    @SerializedName("phone")
    private String phone;

    @SerializedName("state")
    private String state;

    @SerializedName("district")
    private String district;

    @SerializedName("area")
    private String area;

    @SerializedName("category")
    private String category;

    @SerializedName("medical_notes")
    private String medicalNotes;

    @SerializedName("photo_path")
    private String photoPath;
    
    @SerializedName("is_high_risk")
    private boolean isHighRisk;
    
    @SerializedName("high_risk_reason")
    private String highRiskReason;
    
    @SerializedName("blood_group")
    private String bloodGroup;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("village")
    private String village;
    
    @SerializedName("emergency_contact")
    private String emergencyContact;
    
    @SerializedName("medical_history")
    private String medicalHistory;
    
    @SerializedName("asha_id")
    private String ashaId;
    
    @SerializedName("registration_date")
    private String registrationDate;

    @SerializedName("sync_status")
    private String syncStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_updated")
    private String lastUpdated;

    // Constructors
    public Patient() {
        this.syncStatus = "PENDING";
    }

    public Patient(String name, int age, String gender, String phone, String category) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.category = category;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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

    // Helper method to get full location
    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        if (area != null && !area.isEmpty()) {
            location.append(area);
        }
        if (district != null && !district.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(district);
        }
        if (state != null && !state.isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        return location.toString();
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
    
    public String getBloodGroup() {
        return bloodGroup;
    }
    
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getVillage() {
        return village;
    }
    
    public void setVillage(String village) {
        this.village = village;
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getMedicalHistory() {
        return medicalHistory;
    }
    
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
    public String getAshaId() {
        return ashaId;
    }
    
    public void setAshaId(String ashaId) {
        this.ashaId = ashaId;
    }
    
    public String getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    // ID alias
    public long getId() {
        return localId;
    }
    
    public void setId(long id) {
        this.localId = id;
    }

    // Helper method to check if synced
    public boolean isSynced() {
        return "SYNCED".equals(syncStatus);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "localId=" + localId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
