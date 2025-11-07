package es.ucm.fdi.pad.collabup.modelo.collabView;

import com.google.firebase.Timestamp;

import java.util.List;

import es.ucm.fdi.pad.collabup.modelo.Etiqueta;

public class CollabItem {

    private String nombre;
    private String descripcion;
    private Timestamp fecha;
    private List<String> usuariosAsignados;
    private List<Etiqueta> etiquetasItem; //lista de etiquetas asignadas al item
    private String idC;
    private String idI;


    //todo asignar id cuando se cree
    //----------------------MÉTODOS---------------------------
    //--------------------- Métodos básicos

    public CollabItem(String nombre, String descripcion, Timestamp fecha,
                      List<String> usuariosAsignados, List<Etiqueta> etiquetasItem, String idC) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuariosAsignados = usuariosAsignados;
        this.etiquetasItem = etiquetasItem;
        this.idC = idC;
    }

    public String getIdC() {
        return idC;
    }

    public void setIdC(String idC) {
        this.idC = idC;
    }

    public String getIdI() {
        return idI;
    }

    public void setIdI(String idI) {
        this.idI = idI;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public List<String> getUsuariosAsignados() {
        return usuariosAsignados;
    }

    public void setUsuariosAsignados(List<String> usuariosAsignados) {
        this.usuariosAsignados = usuariosAsignados;
    }

    public List<Etiqueta> getEtiquetasItem() {
        return etiquetasItem;
    }

    public void setEtiquetasItem(List<Etiqueta> etiquetasItem) {
        this.etiquetasItem = etiquetasItem;
    }


}
