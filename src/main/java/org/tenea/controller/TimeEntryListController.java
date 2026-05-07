package org.tenea.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenea.dto.TimeEntryListRequest;
import org.tenea.dto.TimeEntryListResponse;
import org.tenea.dto.TimeEntryUpdateRequest;
import org.tenea.dto.TimeLogResponse;
import org.tenea.model.SessionData;
import org.tenea.service.HttpClientService;
import org.tenea.service.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @PostMapping("/update")
    public ResponseEntity<TimeLogResponse> updateTimeEntry(@RequestBody TimeEntryUpdateRequest request) {
        System.out.println("Actualizando imputacion con sessionId: " + request.getSessionId());

        try {
            var sessionDataOptional = sessionManager.getSession(request.getSessionId());
            if (!sessionDataOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TimeLogResponse(false, "Sesion no encontrada o expirada", 401));
            }

            SessionData sessionData = sessionDataOptional.get();
            String locationCode = convertToLocationCode(request.getUbicacion());

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
            LocalDateTime inicio = LocalDateTime.parse(request.getDateTimeInicio(), inputFormatter);
            LocalDateTime fin = LocalDateTime.parse(request.getDateTimeFin(), inputFormatter);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

            int statusCode = httpClientService.updateTimeEntry(
                    sessionData.getCookieStore(),
                    sessionData.getVerificationToken(),
                    request.getId(),
                    request.getIdRegistroEntrada(),
                    request.getIdRegistroSalida(),
                    inicio.format(dateFormatter),
                    inicio.format(timeFormatter),
                    fin.format(dateFormatter),
                    fin.format(timeFormatter),
                    locationCode,
                    request.getObservaciones(),
                    request.getIp()
            );

            return ResponseEntity.ok(new TimeLogResponse(true, "Imputacion actualizada", statusCode));
        } catch (Exception e) {
            System.err.println("Error actualizando imputacion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TimeLogResponse(false, "Error: " + e.getMessage(), 500));
        }
    }

    private String convertToLocationCode(String ubicacion) {
        if (ubicacion == null) {
            return "26#1#1";
        }
        switch (ubicacion.toLowerCase()) {
            case "teletrabajo":
                return "26#3#2";
            case "onsite":
                return "26#4#4";
            case "oficina":
            default:
                return "26#1#1";
        }
    }
}
