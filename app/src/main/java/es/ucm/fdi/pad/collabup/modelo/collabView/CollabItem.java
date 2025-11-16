package es.ucm.fdi.pad.collabup.modelo.collabView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    private List<String> cvAsignadas; //collabViews a los que pertenece el item

    private FirebaseFirestore db;

    //----------------------MÉTODOS---------------------------
    //--------------------- Métodos básicos

    public CollabItem(String nombre, String descripcion, Timestamp fecha,
                      List<String> usuariosAsignados, List<Etiqueta> etiquetasItem, List<String> cvAsignadas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuariosAsignados = usuariosAsignados;
        this.etiquetasItem = etiquetasItem;
        this.cvAsignadas = cvAsignadas;
        db = FirebaseFirestore.getInstance();
    }

    public CollabItem() {
        // Constructor vacío requerido
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

    public List<String> getCollabsAsignadas() {
        return cvAsignadas;
    }

    public String getEjCollabAsignada() { //saco cualquier collab que tenga este item
        return cvAsignadas.get(0);
    }

    public void setCollabsAsignadas(List<String> collabsAsignadas) {
        this.cvAsignadas = collabsAsignadas;
    }

    @Override
    public void obtener(String identificador, OnDataLoadedCallback<CollabItem> callback) {
        db.collection("collabs")
                .document(this.getEjCollabAsignada())
                .collection("collabItems")
                .document(identificador)
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
    public void crear(OnOperationCallback callback) {
        db.collection("collabs")
                .document(this.getEjCollabAsignada())
                .collection("collabItems")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    this.idI = documentReference.getId();
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }


    //todo revisar diferencias con  método obtener
    public void cargarDatosCollabItem(OnDataLoadedCallback<CollabItem> callback) {
        db.collection("collabs")
                .document(this.getEjCollabAsignada())
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
                .document(reemplazo.getEjCollabAsignada())
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
                .document(getEjCollabAsignada())
                .collection("collabItems")
                .document(idI)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<CollabItem>> callback) {

    }

    public void obtenerCollabItemsCollab(String collabId, OnDataLoadedCallback<ArrayList<CollabItem>> callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabItems")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<CollabItem> listaItems = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            // Leemos los campos manualmente
                            String nombre = document.getString("nombre");
                            String descripcion = document.getString("descripcion");
                            Timestamp fecha = document.getTimestamp("fecha");
                            List<String> usuariosAsignados = (List<String>) document.get("usuariosAsignados");
                            List<String> collabsAsignadas = (List<String>) document.get("collabsAsignadas");
                            List<Etiqueta> etiquetasItem = (List<Etiqueta>) document.get("etiquetasItem");

                            // Creamos el objeto manualmente
                            CollabItem item = new CollabItem(nombre, descripcion, fecha, usuariosAsignados, etiquetasItem, collabsAsignadas);
                            item.setIdI(document.getId());

                            listaItems.add(item);
                        }
                        callback.onSuccess(listaItems);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }


}
