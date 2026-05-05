package org.tenea.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenea.dto.TimeEntryListRequest;
import org.tenea.dto.TimeEntryListResponse;
import org.tenea.model.SessionData;
import org.tenea.service.HttpClientService;
import org.tenea.service.SessionManager;

@RestController
@RequestMapping("/api/timeentries")
public class TimeEntryListController {

    private final SessionManager sessionManager;
    private final HttpClientService httpClientService;

    public TimeEntryListController(SessionManager sessionManager, HttpClientService httpClientService) {
        this.sessionManager = sessionManager;
        this.httpClientService = httpClientService;
    }

    @PostMapping("/list")
    public ResponseEntity<TimeEntryListResponse> getTimeEntryList(@RequestBody TimeEntryListRequest request) {
        System.out.println("📋 Solicitando listado de imputaciones para sesión: " + request.getSession_id());

        try {
            // Validar sesión (getSession retorna Optional)
            var sessionDataOptional = sessionManager.getSession(request.getSession_id());
            if (!sessionDataOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TimeEntryListResponse(false, "❌ Sesión inválida o expirada", null));
            }

            SessionData sessionData = sessionDataOptional.get();

            // Obtener listado de imputaciones
            var records = httpClientService.getTimeEntryList(
                    sessionData.getCookieStore(),
                    sessionData.getVerificationToken(),
                    request.getFecha_desde(),
                    request.getFecha_hasta()
            );

            System.out.println("✅ Obtenidos " + records.size() + " registros de imputaciones");

            return ResponseEntity.ok(new TimeEntryListResponse(
                    true,
                    "✅ Listado obtenido exitosamente (" + records.size() + " registros)",
                    records
            ));

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo listado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TimeEntryListResponse(false, "❌ Error obteniendo listado: " + e.getMessage(), null));
        }
    }
}
