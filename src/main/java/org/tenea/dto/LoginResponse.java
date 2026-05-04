package org.tenea.dto;

public class LoginResponse {
    private String sessionId;
    private String message;
    private boolean success;

    public LoginResponse(String sessionId, String message, boolean success) {
        this.sessionId = sessionId;
        this.message = message;
        this.success = success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

