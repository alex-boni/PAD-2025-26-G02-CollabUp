package es.ucm.fdi.pad.collabup.modelo.collabView;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabItem implements DAO<CollabItem> {

    private String idI;
    private String nombre;
    private String descripcion;
    private Timestamp fecha;
    private List<String> usuariosAsignados;
    private List<Etiqueta> etiquetasItem; //lista de etiquetas asignadas al item

    //Métodos básicos
    public CollabItem(String idI, String nombre, String descripcion, Timestamp fecha,
                      List<String> usuariosAsignados, List<Etiqueta> etiquetasItem) {
        this.idI = idI;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuariosAsignados = usuariosAsignados;
        this.etiquetasItem = etiquetasItem;
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

    //---------------- FUNCIONES BASE DE DATOS
    @Override
    public void obtener(String identificador, OnDataLoadedCallback<CollabItem> callback) {

    }

    @Override
    public void crear(OnOperationCallback callback) {

    }

    @Override
    public void modificar(CollabItem reemplazo, OnOperationCallback callback) {

    }

    @Override
    public void eliminar(OnOperationCallback callback) {

    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<CollabItem>> callback) {

    }


}
