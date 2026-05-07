package org.tenea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "fecha", "hora_IN", "hora_OUT", "id_registro_entrada", "id_registro_salida", "zona"})
public class TimeEntryRecord {
    private String id;

    @JsonProperty("id_registro_entrada")
    private String idRegistroEntrada;

    @JsonProperty("id_registro_salida")
    private String idRegistroSalida;

    @JsonProperty("edit_href")
    private String editHref;

    private String empleado;
    private String fecha;
    private String hora;

    @JsonProperty("hora_IN")
    private String horaIn;

    @JsonProperty("hora_OUT")
    private String horaOut;

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
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdRegistroEntrada() { return idRegistroEntrada; }
    public void setIdRegistroEntrada(String idRegistroEntrada) { this.idRegistroEntrada = idRegistroEntrada; }

    public String getIdRegistroSalida() { return idRegistroSalida; }
    public void setIdRegistroSalida(String idRegistroSalida) { this.idRegistroSalida = idRegistroSalida; }

    public String getEditHref() { return editHref; }
    public void setEditHref(String editHref) { this.editHref = editHref; }

    public String getEmpleado() { return empleado; }
    public void setEmpleado(String empleado) { this.empleado = empleado; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getHoraIn() { return horaIn; }
    public void setHoraIn(String horaIn) { this.horaIn = horaIn; }

    public String getHoraOut() { return horaOut; }
    public void setHoraOut(String horaOut) { this.horaOut = horaOut; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    @Override
    public String toString() {
        return String.format("📅 %s - %s %s - %s (%s)", empleado, fecha, hora, tipo, zona);
    }
}

