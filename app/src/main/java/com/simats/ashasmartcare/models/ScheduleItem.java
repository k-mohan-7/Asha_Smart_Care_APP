package com.simats.ashasmartcare.models;

public class ScheduleItem {
    private String title;
    private String dueDate;
    private String status; // e.g., "Due Soon", "Upcoming"

    public ScheduleItem(String title, String dueDate, String status) {
        this.title = title;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }
}
