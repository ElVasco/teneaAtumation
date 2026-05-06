package org.tenea.dto;

public class TimeEntryListRequest {
    private String session_id;
    private String fecha_desde; // formato dd/MM/yyyy
    private String fecha_hasta; // formato dd/MM/yyyy

    public TimeEntryListRequest() {}

    public TimeEntryListRequest(String session_id, String fecha_desde, String fecha_hasta) {
        this.session_id = session_id;
        this.fecha_desde = fecha_desde;
        this.fecha_hasta = fecha_hasta;
    }

    // Getters and setters
    public String getSession_id() { return session_id; }
    public void setSession_id(String session_id) { this.session_id = session_id; }

    public String getFecha_desde() { return fecha_desde; }
    public void setFecha_desde(String fecha_desde) { this.fecha_desde = fecha_desde; }

    public String getFecha_hasta() { return fecha_hasta; }
    public void setFecha_hasta(String fecha_hasta) { this.fecha_hasta = fecha_hasta; }
}