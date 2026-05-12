package org.tenea.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tenea.model.SessionData;
import org.tenea.model.SessionDataSerializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class SessionManager {
    private static final Logger logger = LogManager.getLogger(SessionManager.class);

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    private final ReentrantLock persistenceLock = new ReentrantLock();

    @Value("${tenea.sessions.file.path:sessions.json}")
    private String sessionsFilePath;

    @Value("${tenea.session.expiration.minutes:60}")
    private int sessionExpirationMinutes;

    private final ObjectMapper objectMapper;

    public SessionManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void loadSessions() {
        persistenceLock.lock();
        try {
            File file = new File(sessionsFilePath);
            if (!file.exists()) {
                return;
            }

            List<SessionDataSerializable> serializedSessions = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SessionDataSerializable.class));

            for (SessionDataSerializable serialized : serializedSessions) {
                SessionData sessionData = serialized.toSessionData();
                if (!sessionData.isExpired(sessionExpirationMinutes)) {
                    sessions.put(sessionData.getSessionId(), sessionData);
                    logger.info("Sesion cargada desde archivo: " + sessionData.getUsername());
                }
            }
            logger.info("Sesiones cargadas desde " + sessionsFilePath + ": " + sessions.size());
        } catch (IOException e) {
            logger.warn("Error al cargar sesiones desde archivo: " + e.getMessage());
        } finally {
            persistenceLock.unlock();
        }
    }

    @PreDestroy
    public void saveSessions() {
        persistenceLock.lock();
        try {
            List<SessionDataSerializable> serializedSessions = sessions.values().stream()
                    .map(SessionDataSerializable::new)
                    .collect(Collectors.toList());

            objectMapper.writeValue(new File(sessionsFilePath), serializedSessions);
            logger.info("Sesiones guardadas en " + sessionsFilePath + ": " + serializedSessions.size());
        } catch (IOException e) {
            logger.warn("Error al guardar sesiones en archivo: " + e.getMessage());
        } finally {
            persistenceLock.unlock();
        }
    }

    public void storeSession(SessionData sessionData) {
        sessions.put(sessionData.getSessionId(), sessionData);
        logger.info("Sesion almacenada para usuario: " + sessionData.getUsername() + " | UUID: " + sessionData.getSessionId());
        saveSessions();
    }

    public Optional<SessionData> getSession(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }

        SessionData session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired(sessionExpirationMinutes)) {
            sessions.remove(sessionId);
            logger.info("Sesion expirada: " + sessionId);
            saveSessions();
            return Optional.empty();
        }
        session.touch();
        saveSessions();
        return Optional.of(session);
    }

    public Optional<SessionData> getSessionByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }

        Optional<SessionData> sessionData = sessions.values().stream()
                .filter(session -> username.equals(session.getUsername()) && !session.isExpired(sessionExpirationMinutes))
                .findFirst();

        sessionData.ifPresent(session -> {
            session.touch();
            saveSessions();
        });

        return sessionData;
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        logger.info("Sesion removida: " + sessionId);
        saveSessions();
    }

    public int getActiveSessions() {
        return (int) sessions.values().stream()
                .filter(session -> !session.isExpired(sessionExpirationMinutes))
                .count();
    }
}
