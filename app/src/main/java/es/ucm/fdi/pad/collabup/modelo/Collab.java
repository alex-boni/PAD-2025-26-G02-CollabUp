package es.ucm.fdi.pad.collabup.modelo;

import java.util.Date;

/**
 * Clase simple para representar un Collab.
 */
public class Collab {

    private String nombreCollab;
    private String descripcion;
    private String usuarioCreador; // Guardaremos el UID del creador
    private Date fechaCreacion;

    // Constructor vacío requerido por Firestore para la deserialización
    public Collab() {}

    public Collab(String nombreCollab, String descripcion, String usuarioCreador, Date fechaCreacion) {
        this.nombreCollab = nombreCollab;
        this.descripcion = descripcion;
        this.usuarioCreador = usuarioCreador;
        this.fechaCreacion = fechaCreacion;
    }

    // --- Getters y Setters ---
    // (Firestore los necesita para rellenar el objeto)

    public String getNombreCollab() {
        return nombreCollab;
    }

    public void setNombreCollab(String nombreCollab) {
        this.nombreCollab = nombreCollab;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(String usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}