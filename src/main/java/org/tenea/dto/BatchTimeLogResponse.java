package org.tenea.dto;

import java.util.List;

public class BatchTimeLogResponse {
    private int totalEntries;
    private int successCount;
    private int failedCount;
    private List<TimeLogResponse> results;

    public BatchTimeLogResponse(int totalEntries, int successCount, int failedCount, List<TimeLogResponse> results) {
        this.totalEntries = totalEntries;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.results = results;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<TimeLogResponse> getResults() {
        return results;
    }

    public void setResults(List<TimeLogResponse> results) {
        this.results = results;
    }
}
