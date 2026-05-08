package org.tenea.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tenea.service.SessionManager;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final SessionManager sessionManager;

    public HealthController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "✅ UP");
        response.put("service", "TeneaAutomation API Server");
        response.put("active_sessions", sessionManager.getActiveSessions());
        response.put("endpoints", new HashMap<String, String>() {{
            put("login", "POST /teneator/api/auth/login");
            put("timelog", "POST /teneator/api/timelog");
            put("timeentries_list", "POST /teneator/api/timeentries/list");
            put("timeentries_update", "POST /teneator/api/timeentries/update");
            put("health", "GET /teneator/api/health");
        }});
        return ResponseEntity.ok(response);
    }
}
