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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
    private String idC;

    private FirebaseFirestore db;

    //----------------------MÉTODOS---------------------------
    //--------------------- Métodos básicos

    public CollabItem(String nombre, String descripcion, Timestamp fecha,
                      List<String> usuariosAsignados, List<Etiqueta> etiquetasItem, String idC, List<String> cvAsignadas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuariosAsignados = usuariosAsignados;
        this.etiquetasItem = etiquetasItem;
        this.cvAsignadas = cvAsignadas;
        this.idC = idC;
        db = FirebaseFirestore.getInstance();
    }

    public CollabItem() {
        // Constructor vacío requerido
        db = FirebaseFirestore.getInstance();
    }

    public String getIdC() {
        return idC;
    }

    public void setIdC(String idC) {
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

    public List<String> getcvAsignadas() {
        return cvAsignadas;
    }

    @Exclude
    public String getEjCollabAsignada() { //saco cualquier collab que tenga este item
        if (cvAsignadas == null || cvAsignadas.isEmpty()) return null;
        return cvAsignadas.get(0);
    }

    public void setcvAsignadas(List<String> collabsAsignadas) {
        this.cvAsignadas = collabsAsignadas;
    }

    @Override
    public void obtener(String identificador, OnDataLoadedCallback<CollabItem> callback) {
        db.collection("collabs")
                .document(this.getIdC())
                .collection("collabItems")
                .document(identificador)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        this.setDescripcion(documentSnapshot.getString("descripcion"));
                        this.setEtiquetasItem((List<Etiqueta>) documentSnapshot.get("etiquetas"));
                        this.setFecha(documentSnapshot.getTimestamp("fecha"));
                        this.setNombre(documentSnapshot.getString("nombre"));
                        this.setUsuariosAsignados((List<String>) documentSnapshot.get("usuariosAsignados"));
                        this.setcvAsignadas((List<String>) documentSnapshot.get("cvAsignadas"));
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
                .document(this.getIdC())
                .collection("collabItems")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    this.idI = documentReference.getId();
                    callback.onSuccess();
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
                            List<String> usuariosAsignados = document.contains("usuariosAsignados")
                                    ? (List<String>) document.get("usuariosAsignados")
                                    : new ArrayList<>();
                            List<String> cvAsignadas = new ArrayList<>();
                            //collabsAsignadas = (List<String>) document.get("collabsAsignadas"); //todo
                            List<Etiqueta> etiquetasItem = (List<Etiqueta>) document.get("etiquetasItem");

                            // Creamos el objeto manualmente
                            CollabItem item = new CollabItem(nombre, descripcion, fecha, usuariosAsignados, etiquetasItem, collabId, cvAsignadas);
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

    //Saca una lista de Strings con los id de los collab items de esa collab view
    public void obtenerCollabItemsCollabView(String collabId, String collabViewId, OnDataLoadedCallback<List<String>> callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(collabViewId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onSuccess(new ArrayList<>()); // No existe la collabView
                        return;
                    }

                    // Sacamos la lista de los ids de collabItems
                    List<String> idsItems = documentSnapshot.contains("ciAsignados")
                            ? (List<String>) documentSnapshot.get("ciAsignados")
                            : new ArrayList<>();

                    callback.onSuccess(idsItems);
                })
                .addOnFailureListener(callback::onFailure);
    }

    //Obtiene los collabItems asignados a un usuario que tienen fecha (para el calendario general)
    public void obtenerCollabItemsAsigUsrFecha(String collabId, String usuarioId,
                                               OnDataLoadedCallback<List<CollabItem>> callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabItems")
                .whereArrayContains("usuariosAsignados", usuarioId) // Filtramos items donde el usuario esta asignado
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CollabItem> collabItems = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Timestamp fechaItem = doc.getTimestamp("fecha");
                        if (fechaItem != null) { // Solo me interesan los que tienen fecha
                            collabItems.add(buildItemDocument(doc, collabId, fechaItem));
                        }
                    }
                    callback.onSuccess(collabItems);
                })
                .addOnFailureListener(callback::onFailure);
    }

    //Obtiene una lista de strings con los ids de los collabitems de un usuario que sean de una fecha concreta
    public void obtenerCollabItemsUsrFecha(String usuarioId, String collabId, Timestamp fecha,
                                           OnDataLoadedCallback<List<CollabItem>> callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabItems")
                .whereArrayContains("usuariosAsignados", usuarioId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CollabItem> itemsFiltrados = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Timestamp fechaItem = doc.getTimestamp("fecha");
                        if (fechaItem == null) continue; // Solo me interesan los que tienen fecha

                        // Solo voy a comparar día mes y año.

                        Calendar calItem = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calItem.setTime(fechaItem.toDate());

                        Calendar calSel = Calendar.getInstance();
                        calSel.setTime(fecha.toDate());

                        if (calItem.get(Calendar.YEAR) == calSel.get(Calendar.YEAR) &&
                                calItem.get(Calendar.MONTH) == calSel.get(Calendar.MONTH) &&
                                calItem.get(Calendar.DAY_OF_MONTH) == calSel.get(Calendar.DAY_OF_MONTH)) {
                            itemsFiltrados.add(buildItemDocument(doc, collabId, fechaItem));
                        }
                    }
                    callback.onSuccess(itemsFiltrados);
                })
                .addOnFailureListener(callback::onFailure);
    }

    //Funcion que dado un documento de firebase construye el objeto. Prefiero no usar la función
    //directa porque puede causar excepciones difíciles de controlar.
    private CollabItem buildItemDocument(DocumentSnapshot doc, String collabId, Timestamp fechaItem) {
        String nombre = doc.getString("nombre");
        String descripcion = doc.getString("descripcion");
        List<String> usuariosAsignados = doc.contains("usuariosAsignados")
                ? (List<String>) doc.get("usuariosAsignados")
                : new ArrayList<>();
        List<Etiqueta> etiquetasItem = doc.contains("etiquetas")
                ? (List<Etiqueta>) doc.get("etiquetas")
                : new ArrayList<>();
        List<String> cvAsignadas = doc.contains("cvAsignadas")
                ? (List<String>) doc.get("cvAsignadas")
                : new ArrayList<>();

        CollabItem item = new CollabItem(
                nombre,
                descripcion,
                fechaItem,
                usuariosAsignados,
                etiquetasItem,
                collabId,
                cvAsignadas
        );
        item.setIdI(doc.getId());

        return item;
    }
}
