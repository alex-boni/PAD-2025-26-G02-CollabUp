package es.ucm.fdi.pad.collabup.modelo;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class Collab implements DAO<Collab> {

    private transient FirebaseFirestore db;
    
    private String id;
    private String nombre;
    private String descripcion;
    private String imageUri;
    private String creadorId;
    private Date fechaCreacion;
    private ArrayList<String> miembros;

    private String estado;

    public Collab() {
        db = FirebaseFirestore.getInstance();
        this.miembros = new ArrayList<>();
        this.fechaCreacion = new Date();
        this.estado = "all";
    }

    public Collab(String nombre, String descripcion, String imageUri, String creadorId) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imageUri = imageUri;
        this.creadorId = creadorId;
        this.miembros = new ArrayList<>();
        this.miembros.add(creadorId);
        this.estado = "all";
    }

    @Override
    public void obtener(String identificador, OnDataLoadedCallback<Collab> callback) {
        db.collection("collabs").document(identificador).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Collab collab = documentSnapshot.toObject(Collab.class);
                            if (collab != null) {
                                collab.setId(documentSnapshot.getId());
                            }
                            callback.onSuccess(collab);
                        } else {
                            callback.onFailure(new Exception("No se encontró el Collab."));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void crear(OnOperationCallback callback) {
        Map<String, Object> collabData = new HashMap<>();
        collabData.put("nombre", this.nombre);
        collabData.put("descripcion", this.descripcion);
        collabData.put("imageUri", this.imageUri);
        collabData.put("creadorId", this.creadorId);
        collabData.put("fechaCreacion", this.fechaCreacion);
        collabData.put("miembros", this.miembros);
        collabData.put("estado", this.estado);

        db.collection("collabs").add(collabData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        id = documentReference.getId();
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void modificar(Collab reemplazo, OnOperationCallback callback) {
        if (reemplazo.getId() == null || reemplazo.getId().isEmpty()) {
            callback.onFailure(new Exception("ID de Collab no válido"));
            return;
        }

        Map<String, Object> collabData = new HashMap<>();
        collabData.put("nombre", reemplazo.getNombre());
        collabData.put("descripcion", reemplazo.getDescripcion());
        collabData.put("imageUri", reemplazo.getImageUri());
        collabData.put("miembros", reemplazo.getMiembros());
        collabData.put("estado", reemplazo.getEstado());

        db.collection("collabs").document(reemplazo.getId()).update(collabData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void eliminar(OnOperationCallback callback) {
        if (this.id == null || this.id.isEmpty()) {
            callback.onFailure(new Exception("ID de Collab no válido"));
            return;
        }

        db.collection("collabs").document(this.id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<Collab>> callback) {
        db.collection("collabs").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Collab> listaCollabs = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Collab collab = document.toObject(Collab.class);
                            if (collab != null) {
                                collab.setId(document.getId());
                                listaCollabs.add(collab);
                            }
                        }
                        callback.onSuccess(listaCollabs);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void obtenerCollabsDelUsuario(String usuarioId, OnDataLoadedCallback<ArrayList<Collab>> callback) {
        db.collection("collabs")
                .whereArrayContains("miembros", usuarioId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Collab> listaCollabs = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Collab collab = document.toObject(Collab.class);
                            if (collab != null) {
                                collab.setId(document.getId());
                                listaCollabs.add(collab);
                            }
                        }
                        callback.onSuccess(listaCollabs);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void agregarMiembro(String usuarioId, OnOperationCallback callback) {
        if (this.id == null || this.id.isEmpty()) {
            callback.onFailure(new Exception("ID de Collab no válido"));
            return;
        }

        if (this.miembros.contains(usuarioId)) {
            callback.onFailure(new Exception("El usuario ya es miembro del Collab"));
            return;
        }

        this.miembros.add(usuarioId);

        db.collection("collabs").document(this.id)
                .update("miembros", this.miembros)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        miembros.remove(usuarioId);
                        callback.onFailure(e);
                    }
                });
    }

    public void removerMiembro(String usuarioId, OnOperationCallback callback) {
        if (this.id == null || this.id.isEmpty()) {
            callback.onFailure(new Exception("ID de Collab no válido"));
            return;
        }

        if (!this.miembros.contains(usuarioId)) {
            callback.onFailure(new Exception("El usuario no es miembro del Collab"));
            return;
        }

        this.miembros.remove(usuarioId);

        db.collection("collabs").document(this.id)
                .update("miembros", this.miembros)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        miembros.add(usuarioId);
                        callback.onFailure(e);
                    }
                });
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(String creadorId) {
        this.creadorId = creadorId;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public ArrayList<String> getMiembros() {
        return miembros;
    }

    public void setMiembros(ArrayList<String> miembros) {
        this.miembros = miembros;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean esFavorito() {
        return "favorito".equalsIgnoreCase(this.estado);
    }
    public boolean estaEliminado() {
        return "eliminado".equalsIgnoreCase(this.estado);
    }
    public boolean estaActivo() {
        return !"eliminado".equalsIgnoreCase(this.estado);
    }
    
    @Override
    public String toString() {
        return "Collab{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
