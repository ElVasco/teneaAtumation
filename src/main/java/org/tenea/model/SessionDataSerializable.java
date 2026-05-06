package org.tenea.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDataSerializable {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("verificationToken")
    private String verificationToken;

    @JsonProperty("createdAt")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonProperty("cookies")
    private List<CookieData> cookies;

    public SessionDataSerializable() {}

    public SessionDataSerializable(SessionData sessionData) {
        this.sessionId = sessionData.getSessionId();
        this.username = sessionData.getUsername();
        this.verificationToken = sessionData.getVerificationToken();
        this.createdAt = sessionData.getCreatedAt();

        this.cookies = new ArrayList<>();
        if (sessionData.getCookieStore() != null) {
            sessionData.getCookieStore().getCookies().forEach(cookie ->
                this.cookies.add(new CookieData(
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.getDomain(),
                    cookie.getPath()
                ))
            );
        }
    }

    public SessionData toSessionData() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        if (this.cookies != null) {
            this.cookies.forEach(cookieData -> {
                BasicClientCookie cookie = new BasicClientCookie(cookieData.getName(), cookieData.getValue());
                cookie.setDomain(cookieData.getDomain());
                cookie.setPath(cookieData.getPath());
                cookieStore.addCookie(cookie);
            });
        }

        return new SessionData(this.sessionId, this.username, cookieStore, this.verificationToken, this.createdAt);
    }

    // Getters and setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<CookieData> getCookies() { return cookies; }
    public void setCookies(List<CookieData> cookies) { this.cookies = cookies; }

    public static class CookieData {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;

        @JsonProperty("domain")
        private String domain;

        @JsonProperty("path")
        private String path;

        public CookieData() {}

        public CookieData(String name, String value, String domain, String path) {
            this.name = name;
            this.value = value;
            this.domain = domain;
            this.path = path;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}
