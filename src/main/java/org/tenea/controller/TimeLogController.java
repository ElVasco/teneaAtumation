package org.tenea.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenea.dto.BatchTimeLogResponse;
import org.tenea.dto.TimeLogRequest;
import org.tenea.dto.TimeLogResponse;
import org.tenea.dto.UserInfoResponse;
import org.tenea.model.SessionData;
import org.tenea.service.HttpClientService;
import org.tenea.service.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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

    @PostMapping("/userinfo")
    public ResponseEntity<UserInfoResponse> getUserInfo(@RequestBody TimeLogRequest request) {
        if (isBlank(request.getSessionId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Optional<SessionData> sessionOptional = sessionManager.getSession(request.getSessionId());
            if (sessionOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            SessionData session = sessionOptional.get();
            UserInfoResponse userInfo = httpClientService.getUserInfo(session.getCookieStore());
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo UserInfo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            String locationCode = convertToLocationCode(getUserInfo(request), request.getUbicacion());

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

    @PostMapping("/batch")
    public ResponseEntity<BatchTimeLogResponse> logTimeBatch(@RequestBody List<TimeLogRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new BatchTimeLogResponse(0, 0, 0, List.of())
            );
        }

        List<TimeLogResponse> results = new ArrayList<>();

        for (TimeLogRequest request : requests) {
            if (isBlank(request.getSessionId()) || isBlank(request.getDateTimeInicio())
                    || isBlank(request.getDateTimeFin()) || isBlank(request.getUbicacion())) {
                results.add(new TimeLogResponse(false, "Campos requeridos: session_id, date_time_inicio, date_time_fin, ubicacion", 400));
                continue;
            }

            try {
                Optional<SessionData> sessionOptional = sessionManager.getSession(request.getSessionId());
                if (sessionOptional.isEmpty()) {
                    results.add(new TimeLogResponse(false, "❌ Sesión no encontrada o expirada", 401));
                    continue;
                }

                SessionData session = sessionOptional.get();
                String locationCode = convertToLocationCode(getUserInfo(request), request.getUbicacion());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime inicio = LocalDateTime.parse(request.getDateTimeInicio(), formatter);
                LocalDateTime fin = LocalDateTime.parse(request.getDateTimeFin(), formatter);
                Result result = new Result(session, locationCode, inicio, fin);
                if (!result.fin().isAfter(result.inicio())) {
                    results.add(new TimeLogResponse(false, "date_time_fin debe ser posterior a date_time_inicio", 400));
                    continue;
                }

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                int statusCode = httpClientService.logTimeEntry(
                        result.session().getCookieStore(),
                        result.session().getVerificationToken(),
                        result.inicio().format(dateFormatter),
                        result.inicio().format(timeFormatter),
                        result.fin().format(timeFormatter),
                        result.locationCode()
                );

                results.add(new TimeLogResponse(true, "Entrada de tiempo registrada exitosamente", statusCode));

            } catch (DateTimeParseException e) {
                results.add(new TimeLogResponse(false, "Formato de fecha inválido. Use yyyy-MM-dd HH:mm", 400));
            } catch (Exception e) {
                System.err.println("❌ Error al registrar entrada de tiempo: " + e.getMessage());
                results.add(new TimeLogResponse(false, "❌ Error: " + e.getMessage(), 500));
            }
        }

        long successCount = results.stream().filter(TimeLogResponse::isSuccess).count();
        long failedCount = results.size() - successCount;

        return ResponseEntity.ok(new BatchTimeLogResponse(results.size(), (int) successCount, (int) failedCount, results));
    }

    private record Result(SessionData session, String locationCode, LocalDateTime inicio, LocalDateTime fin) {
    }

    private String convertToLocationCode(ResponseEntity<UserInfoResponse> request, String ubicacion) {
        assert request.getBody() != null;
        int vIdCard = request.getBody().getIdVirtualCard();

        if (ubicacion == null) {
            return vIdCard + "#1#1";
        }
        switch (ubicacion.toLowerCase()) {
            case "teletrabajo":
                return vIdCard+"#3#2";
            case "onsite":
                return vIdCard+"#4#4";
            case "oficina":
            default:
                return vIdCard+"#1#1";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

