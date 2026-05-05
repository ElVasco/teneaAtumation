package org.tenea.dto;

public class TimeEntryRecord {
    private String empleado;
    private String fecha;
    private String hora;
    private String tipo;
    private String zona;

    public TimeEntryRecord() {}

    public TimeEntryRecord(String empleado, String fecha, String hora, String tipo, String zona) {
        this.empleado = empleado;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.zona = zona;
    }

    // Getters and setters
    public String getEmpleado() { return empleado; }
    public void setEmpleado(String empleado) { this.empleado = empleado; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    @Override
    public String toString() {
        return String.format("📅 %s - %s %s - %s (%s)", empleado, fecha, hora, tipo, zona);
    }
}

