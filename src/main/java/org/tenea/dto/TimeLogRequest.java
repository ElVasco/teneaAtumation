package org.tenea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeLogRequest {
    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("date_time_inicio")
    private String dateTimeInicio; // formato: yyyy-MM-dd HH:mm

    @JsonProperty("date_time_fin")
    private String dateTimeFin; // formato: yyyy-MM-dd HH:mm

    @JsonProperty("ubicacion")
    private String ubicacion; // "oficina", "teletrabajo", "onsite"

    public TimeLogRequest() {
    }

    public TimeLogRequest(String sessionId, String dateTimeInicio, String dateTimeFin, String ubicacion) {
        this.sessionId = sessionId;
        this.dateTimeInicio = dateTimeInicio;
        this.dateTimeFin = dateTimeFin;
        this.ubicacion = ubicacion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDateTimeInicio() {
        return dateTimeInicio;
    }

    public void setDateTimeInicio(String dateTimeInicio) {
        this.dateTimeInicio = dateTimeInicio;
    }

    public String getDateTimeFin() {
        return dateTimeFin;
    }

    public void setDateTimeFin(String dateTimeFin) {
        this.dateTimeFin = dateTimeFin;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}

