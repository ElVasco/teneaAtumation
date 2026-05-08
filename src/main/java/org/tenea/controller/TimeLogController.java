package org.tenea.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenea.dto.TimeLogRequest;
import org.tenea.dto.TimeLogResponse;
import org.tenea.model.SessionData;
import org.tenea.service.SessionManager;
import org.tenea.service.HttpClientService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
@RequestMapping("/api/timelog")
public class TimeLogController {

    private final SessionManager sessionManager;
    private final HttpClientService httpClientService;

    public TimeLogController(SessionManager sessionManager, HttpClientService httpClientService) {
        this.sessionManager = sessionManager;
        this.httpClientService = httpClientService;
    }

    @PostMapping
    public ResponseEntity<TimeLogResponse> logTime(@RequestBody TimeLogRequest request) {
        if (request == null || isBlank(request.getSessionId()) || isBlank(request.getDateTimeInicio())
                || isBlank(request.getDateTimeFin()) || isBlank(request.getUbicacion())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new TimeLogResponse(false, "Campos requeridos: session_id, date_time_inicio, date_time_fin, ubicacion", 400)
            );
        }

        System.out.println("📅 Recibida solicitud de logging con sessionId: " + request.getSessionId());

        try {
            // Obtener sesión por UUID
            Optional<SessionData> sessionOptional = sessionManager.getSession(request.getSessionId());
            if (sessionOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new TimeLogResponse(false, "❌ Sesión no encontrada o expirada", 401)
                );
            }

            SessionData session = sessionOptional.get();

            // Convertir ubicación a código
            String locationCode = convertToLocationCode(request.getUbicacion());

            // Parsear fechas-horas
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime inicio = LocalDateTime.parse(request.getDateTimeInicio(), formatter);
            LocalDateTime fin = LocalDateTime.parse(request.getDateTimeFin(), formatter);
            if (!fin.isAfter(inicio)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new TimeLogResponse(false, "date_time_fin debe ser posterior a date_time_inicio", 400)
                );
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            String date = inicio.format(dateFormatter);
            String startTime = inicio.format(timeFormatter);
            String endTime = fin.format(timeFormatter);

            // Registrar entrada de tiempo
            int statusCode = httpClientService.logTimeEntry(
                    session.getCookieStore(),
                    session.getVerificationToken(),
                    date,
                    startTime,
                    endTime,
                    locationCode
            );

            return ResponseEntity.ok(
                    new TimeLogResponse(true, "Entrada de tiempo registrada exitosamente", statusCode)
            );

        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new TimeLogResponse(false, "Formato de fecha invalido. Use yyyy-MM-dd HH:mm", 400)
            );
        } catch (Exception e) {
            System.err.println("❌ Error al registrar entrada de tiempo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new TimeLogResponse(false, "❌ Error: " + e.getMessage(), 500)
            );
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

