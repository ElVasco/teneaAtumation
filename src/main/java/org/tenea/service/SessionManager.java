package org.tenea.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tenea.model.SessionData;
import org.tenea.model.SessionDataSerializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SessionManager {

    private final Map<String, SessionData> sessions = new HashMap<>();
    private static final int SESSION_EXPIRATION_MINUTES = 60;

    @Value("${tenea.sessions.file.path:sessions.json}")
    private String sessionsFilePath;

    private final ObjectMapper objectMapper;

    public SessionManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void loadSessions() {
        try {
            File file = new File(sessionsFilePath);
            if (file.exists()) {
                List<SessionDataSerializable> serializedSessions = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SessionDataSerializable.class));

                for (SessionDataSerializable serialized : serializedSessions) {
                    SessionData sessionData = serialized.toSessionData();
                    if (!sessionData.isExpired(SESSION_EXPIRATION_MINUTES)) {
                        sessions.put(sessionData.getSessionId(), sessionData);
                        System.out.println("✅ Sesión cargada desde archivo: " + sessionData.getUsername());
                    }
                }
                System.out.println("📁 Sesiones cargadas desde " + sessionsFilePath + ": " + sessions.size());
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al cargar sesiones desde archivo: " + e.getMessage());
        }
    }

    @PreDestroy
    public void saveSessions() {
        try {
            List<SessionDataSerializable> serializedSessions = sessions.values().stream()
                .map(SessionDataSerializable::new)
                .collect(Collectors.toList());

            objectMapper.writeValue(new File(sessionsFilePath), serializedSessions);
            System.out.println("💾 Sesiones guardadas en " + sessionsFilePath + ": " + serializedSessions.size());
        } catch (IOException e) {
            System.err.println("⚠️ Error al guardar sesiones en archivo: " + e.getMessage());
        }
    }

    public void storeSession(SessionData sessionData) {
        sessions.put(sessionData.getSessionId(), sessionData);
        System.out.println("✅ Sesión almacenada para usuario: " + sessionData.getUsername() + " | UUID: " + sessionData.getSessionId());
        saveSessions(); // Save immediately after storing
    }

    public Optional<SessionData> getSession(String sessionId) {
        SessionData session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired(SESSION_EXPIRATION_MINUTES)) {
            sessions.remove(sessionId);
            System.out.println("⏱️  Sesión expirada: " + sessionId);
            saveSessions(); // Save after removing expired session
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
        saveSessions(); // Save after removing
    }

    public int getActiveSessions() {
        return sessions.size();
    }
}
