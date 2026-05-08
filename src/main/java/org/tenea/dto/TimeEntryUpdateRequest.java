package org.tenea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeEntryUpdateRequest {
    @JsonProperty("session_id")
    private String sessionId;

    private String id;

    @JsonProperty("id_registro_entrada")
    private String idRegistroEntrada;

    @JsonProperty("id_registro_salida")
    private String idRegistroSalida;

    @JsonProperty("date_time_inicio")
    private String dateTimeInicio; // formato: yyyy-MM-dd HH:mm

    @JsonProperty("date_time_fin")
    private String dateTimeFin; // formato: yyyy-MM-dd HH:mm

    private String ubicacion; // "oficina", "teletrabajo", "onsite"
    private String observaciones;
    private String ip;

    public TimeEntryUpdateRequest() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdRegistroEntrada() {
        return idRegistroEntrada;
    }

    public void setIdRegistroEntrada(String idRegistroEntrada) {
        this.idRegistroEntrada = idRegistroEntrada;
    }

    public String getIdRegistroSalida() {
        return idRegistroSalida;
    }

    public void setIdRegistroSalida(String idRegistroSalida) {
        this.idRegistroSalida = idRegistroSalida;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
