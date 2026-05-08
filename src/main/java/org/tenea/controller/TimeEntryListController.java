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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        if (request == null || isBlank(request.getSession_id()) || isBlank(request.getFecha_desde()) || isBlank(request.getFecha_hasta())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TimeEntryListResponse(false, "Campos requeridos: session_id, fecha_desde, fecha_hasta", null));
        }

        System.out.println("📋 Solicitando listado de imputaciones para sesión: " + request.getSession_id());

        try {
            // Validar sesión (getSession retorna Optional)
            DateTimeFormatter listDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaDesde = LocalDate.parse(request.getFecha_desde(), listDateFormatter);
            LocalDate fechaHasta = LocalDate.parse(request.getFecha_hasta(), listDateFormatter);
            if (fechaHasta.isBefore(fechaDesde)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new TimeEntryListResponse(false, "fecha_hasta debe ser igual o posterior a fecha_desde", null));
            }

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

        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TimeEntryListResponse(false, "Formato de fecha invalido. Use dd/MM/yyyy", null));
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo listado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TimeEntryListResponse(false, "❌ Error obteniendo listado: " + e.getMessage(), null));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<TimeLogResponse> updateTimeEntry(@RequestBody TimeEntryUpdateRequest request) {
        if (request == null || isBlank(request.getSessionId()) || isBlank(request.getId())
                || isBlank(request.getIdRegistroEntrada()) || isBlank(request.getIdRegistroSalida())
                || isBlank(request.getDateTimeInicio()) || isBlank(request.getDateTimeFin())
                || isBlank(request.getUbicacion())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new TimeLogResponse(false, "Campos requeridos: session_id, id, id_registro_entrada, id_registro_salida, date_time_inicio, date_time_fin, ubicacion", 400)
            );
        }

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
            if (!fin.isAfter(inicio)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new TimeLogResponse(false, "date_time_fin debe ser posterior a date_time_inicio", 400)
                );
            }

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
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TimeLogResponse(false, "Formato de fecha invalido. Use yyyy-MM-dd H:mm", 400));
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
