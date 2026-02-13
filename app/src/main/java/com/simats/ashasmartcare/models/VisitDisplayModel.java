package com.simats.ashasmartcare.models;

public class VisitDisplayModel {
    private String title;
    private String date;
    private String status;
    private VisitType type;

    public enum VisitType {
        PREGNANCY,
        CHILD,
        GENERAL,
        VACCINATION,
        OTHER
    }

    public VisitDisplayModel(String title, String date, String status, VisitType type) {
        this.title = title;
        this.date = date;
        this.status = status;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public VisitType getType() {
        return type;
    }
}
