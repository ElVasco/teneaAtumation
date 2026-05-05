package org.tenea.dto;

import java.util.List;

public class TimeEntryListResponse {
    private boolean success;
    private String message;
    private List<TimeEntryRecord> records;

    public TimeEntryListResponse() {}

    public TimeEntryListResponse(boolean success, String message, List<TimeEntryRecord> records) {
        this.success = success;
        this.message = message;
        this.records = records;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<TimeEntryRecord> getRecords() { return records; }
    public void setRecords(List<TimeEntryRecord> records) { this.records = records; }
}
