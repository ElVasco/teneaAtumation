package org.tenea.model;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import java.time.LocalDateTime;
import java.util.UUID;

public class SessionData {
    private final String sessionId;
    private final BasicCookieStore cookieStore;
    private final String verificationToken;
    private final String username;
    private final LocalDateTime createdAt;

    public SessionData(String username, BasicCookieStore cookieStore, String verificationToken) {
        this.sessionId = UUID.randomUUID().toString();
        this.username = username;
        this.cookieStore = cookieStore;
        this.verificationToken = verificationToken;
        this.createdAt = LocalDateTime.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired(int expirationMinutes) {
        return LocalDateTime.now().isAfter(createdAt.plusMinutes(expirationMinutes));
    }
}
