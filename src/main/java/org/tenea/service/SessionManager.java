package org.tenea.service;

import org.springframework.stereotype.Service;
import org.tenea.model.SessionData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SessionManager {

    private final Map<String, SessionData> sessions = new HashMap<>();
    private static final int SESSION_EXPIRATION_MINUTES = 60;

    public void storeSession(SessionData sessionData) {
        sessions.put(sessionData.getSessionId(), sessionData);
        System.out.println("✅ Sesión almacenada para usuario: " + sessionData.getUsername() + " | UUID: " + sessionData.getSessionId());
    }

    public Optional<SessionData> getSession(String sessionId) {
        SessionData session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired(SESSION_EXPIRATION_MINUTES)) {
            sessions.remove(sessionId);
            System.out.println("⏱️  Sesión expirada: " + sessionId);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public Optional<SessionData> getSessionByUsername(String username) {
        return sessions.values().stream()
                .filter(session -> session.getUsername().equals(username) && !session.isExpired(SESSION_EXPIRATION_MINUTES))
                .findFirst();
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        System.out.println("❌ Sesión removida: " + sessionId);
    }

    public int getActiveSessions() {
        return sessions.size();
    }
}

