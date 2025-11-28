package es.ucm.fdi.pad.collabup.modelo;

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
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
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
    private List<String> cvAsignadas; //collabViews a los que pertenece el item
    private String idC;

    private FirebaseFirestore db;

    //----------------------MÉTODOS---------------------------
    //--------------------- Métodos básicos

    public CollabItem(String nombre, String descripcion, Timestamp fecha,
                      List<String> usuariosAsignados, String idC, List<String> cvAsignadas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuariosAsignados = usuariosAsignados;
        this.cvAsignadas = cvAsignadas;
        this.idC = idC;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CollabItem that = (CollabItem) o;
        return Objects.equals(idI, that.idI) && Objects.equals(idC, that.idC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idI, idC);
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

    public List<String> getcvAsignadas() {
        return cvAsignadas;
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
        updates.put("cvAsignadas", reemplazo.getcvAsignadas());

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

                            // Creamos el objeto manualmente
                            CollabItem item = new CollabItem(nombre, descripcion, fecha, usuariosAsignados, collabId, cvAsignadas);
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

    public void obtenerCollabItemsUsr(String usuarioId, String collabId, OnDataLoadedCallback<List<CollabItem>> callback) {
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
                        itemsFiltrados.add(buildItemDocument(doc, collabId, fechaItem));
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
                collabId,
                cvAsignadas
        );
        item.setIdI(doc.getId());

        return item;
    }


    //---------- SACAR NOMBRES A PARTIR DE IDS

    public void obtenerNombreMiembroCollab(String idU, OnDataLoadedCallback<Map<String, String>> callback) {
        new Usuario().obtener(idU, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                Map<String, String> result = new HashMap<>();
                result.put(data.getUID(), data.getNombre());
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    //Devuelve un mapa de los ids del usuario con su nombre
    public void obtenerNombresMiembrosCollab(List<String> idsUsuarios, OnDataLoadedCallback<Map<String, String>> callback) {
        Map<String, String> resultados = new HashMap<>();
        AtomicInteger contador = new AtomicInteger(0);

        if (idsUsuarios.isEmpty()) {
            callback.onSuccess(resultados);
            return;
        }

        for (String idU : idsUsuarios) {
            new Usuario().obtener(idU, new OnDataLoadedCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario usuario) {
                    resultados.put(usuario.getUID(), usuario.getNombre());
                    if (contador.incrementAndGet() == idsUsuarios.size()) {
                        callback.onSuccess(resultados);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }
    }

    //Obtiene los ids de un array de seleccionados (miembros y collab views)
    public List<String> obtenerIdsObjSeleccionados(List<String> idsMiembros, boolean[] seleccionados) {
        List<String> resultado = new ArrayList<>();
        for (int i = 0; i < seleccionados.length; i++) {
            if (seleccionados[i]) resultado.add(idsMiembros.get(i));
        }
        return resultado;
    }


    //Para las actualizaciones en pantalla (sirve para los miembros y las collabviews)
    public List<String> obtenerNombresDeMapaId(Map<String, String> mapaIdsNombres, List<String> ids) {
        List<String> nombres = new ArrayList<>();
        for (String id : ids) {
            nombres.add(mapaIdsNombres.getOrDefault(id, id));
        }
        return nombres;
    }

    public List<String> obtenerNombresDeMapaCVId(Map<String, CollabView> mapaIdsCV, List<String> ids) {
        List<String> nombres = new ArrayList<>();
        for (String id : ids) {
            CollabView cv = mapaIdsCV.get(id);
            if (cv != null) {
                nombres.add(cv.getName());
            } else {
                nombres.add(id); //por si no se encuentra el collabview
            }
        }
        return nombres;
    }

    //Clase con constantes para no poner nada a mano. Estática porque no necesito un objeto para acceder a ella
    public static class CollabItemConstants {
        // Títulos y botones
        public static final String TOOLBAR_TITLE = "Crear nuevo Collab Item";
        public static final String BTN_SELECCION_MIEMBROS = "Seleccionar miembros";
        public static final String BTN_SELECCION_CV = "Seleccionar CollabViews";
        public static final String BTN_ELIMINAR_ITEM = "Eliminar item";

        // VALIDACIONES Y ERRORES
        public static final String ERROR_NOMBRE_REQUERIDO = "El nombre es requerido";
        public static final String ERROR_FECHA_CALENDARIO = "No puedes añadir un CollabItem sin fecha a un Calendario";

        public static final String ERROR_CARGA_ITEMS = "Error al cargar items";
        public static final String ERROR_CARGA_MIEMBROS = "Error al cargar miembros";
        public static final String ERROR_UPDATE_ITEMS_CV = "Error al actualizar los items de la collab view";
        public static final String ERROR_CREAR_ITEM = "No se ha podido crear el collab item";
        public static final String ERROR_MODIFICAR_ITEM = "No se ha podido modificar el collab item";
        public static final String ERROR_ELIMINAR_ITEM = "No se ha podido eliminar el collab item";
        public static final String ERROR_CARGAR_ITEM = "No se han podido cargar los collab items";


        // CONFIRMACIONES
        public static final String CONF_COLLABITEM_CREADO = "CollabItem creado correctamente";
        public static final String CONF_COLLABITEM_ACT = "CollabItem actualizado correctamente";
        public static final String CONF_COLLABITEM_ELIM = "CollabItem eliminado correctamente";
        public static final String CONF_COLLABITEM_CARGADOS = "CollabItems cargados correctamente";

        //PREGUNTAS
        public static final String PREG_ELIMINAR_ITEM = "¿Estás seguro de que quieres eliminar este CollabItem?";
    }


}
