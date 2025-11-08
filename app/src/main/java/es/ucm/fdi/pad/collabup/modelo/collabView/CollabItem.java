package es.ucm.fdi.pad.collabup.modelo.collabView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabItem implements Serializable, DAO<CollabItem> {

    @Exclude
    private transient String idI; //para que no se guarde el campo en firebase

    private String nombre;
    private String descripcion;
    private Timestamp fecha;
    private List<String> usuariosAsignados;
    private List<Etiqueta> etiquetasItem; //lista de etiquetas asignadas al item
    private String idC;


    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();
    }

    public String getIdC() {
        return idC;
    }

    public void setId(String idC) {
        this.idC = idC;
    }

    @Exclude
    public String getIdI() {
        return idI;
    }

    @Exclude
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


    @Override
    public void obtener(String identificador, OnDataLoadedCallback<CollabItem> callback) {

    }

    @Override
    public void crear(OnOperationCallback callback) {
        db.collection("collabs")
                .document(this.getIdC())
                .collection("collabItems")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    this.idI = documentReference.getId();
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void cargarDatosCollabItem(OnDataLoadedCallback<CollabItem> callback) {
        db.collection("collabs")
                .document(this.getIdC())
                .collection("collabItems")
                .document(this.idI)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        this.setDescripcion(documentSnapshot.getString("descripcion"));
                        this.setEtiquetasItem((List<Etiqueta>) documentSnapshot.get("etiquetas"));
                        this.setFecha(documentSnapshot.getTimestamp("fecha"));
                        this.setNombre(documentSnapshot.getString("nombre"));
                        this.setUsuariosAsignados((List<String>) documentSnapshot.get("usuarios"));
                        callback.onSuccess(this); // Llama al callback con el objeto cargado
                    } else {
                        callback.onSuccess(null); // No existe el documento
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void modificar(CollabItem reemplazo, OnOperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", reemplazo.nombre);
        updates.put("descripcion", reemplazo.descripcion);
        updates.put("fecha", reemplazo.fecha);
        updates.put("usuariosAsignados", reemplazo.getUsuariosAsignados());
        updates.put("etiquetasItem", reemplazo.getEtiquetasItem());

        db.collection("collabs")
                .document(reemplazo.getIdC())
                .collection("collabItems")
                .document(reemplazo.getIdI())
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void eliminar(OnOperationCallback callback) {
        if (idI == null || idI.isEmpty()) {
            callback.onFailure(new Exception("ID del CollabItem no válido"));
            return;
        }

        db.collection("collabs")
                .document(idC)
                .collection("collabItems")
                .document(idI)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<CollabItem>> callback) {

    }


}
