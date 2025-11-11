package es.ucm.fdi.pad.collabup.modelo;

import androidx.annotation.NonNull;

// Imports de la interfaz DAO y los nuevos callbacks
import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

// Imports de Firebase Firestore
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase de modelo Usuario.
 * Ahora implementa la interfaz DAO con callbacks para operaciones asíncronas de Firebase.
 */
public class Usuario implements DAO<Usuario> {

    // Instancia de la base de datos de Firestore
    // Nota: @Exclude se usaría si subiéramos el objeto entero, pero creando un Map es más limpio.
    private transient FirebaseFirestore db;

    private String UID = "defaultValue";
    private String email = "defaultValue";
    private String nombre = "defaultValue";
    private String usuario = "defaultValue";
    private String ubicacion;
    private Date fechaNacimiento;
    private String presentacion;
    private String urlFoto;

    // Constructor vacío
    public Usuario() {
        db = FirebaseFirestore.getInstance();
    }

    // --- Implementación de los métodos de la interfaz DAO ---

    @Override
    public void obtener(String identificador, OnDataLoadedCallback<Usuario> callback) {
        db.collection("usuarios").document(identificador).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Convierte el documento en un objeto Usuario
                            Usuario usuarioObtenido = documentSnapshot.toObject(Usuario.class);
                            callback.onSuccess(usuarioObtenido);
                        } else {
                            callback.onFailure(new Exception("No se encontró el usuario."));
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
        // Creamos un mapa con los datos que queremos guardar
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", this.UID); // Asegúrate de que el campo coincida
        userData.put("email", this.email);
        userData.put("nombre", this.nombre);
        userData.put("usuario", this.usuario);
        // Dejamos los otros campos como nulos (no se añaden al mapa)

        // Usamos el UID de Firebase Authentication como ID del documento
        db.collection("usuarios").document(this.UID).set(userData)
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
    public void modificar(Usuario reemplazo, OnOperationCallback callback) {
        // TODO: Implementar lógica de modificación (similar a 'crear' pero con update)
        // db.collection("usuarios").document(reemplazo.getUID()).update(mapaDeDatos)
        //     .addOnSuccessListener...
        //     .addOnFailureListener...
        callback.onFailure(new Exception("Modificar no implementado"));
    }

    @Override
    public void eliminar(OnOperationCallback callback) {
        // TODO: Implementar lógica de eliminación
        // db.collection("usuarios").document(this.UID).delete()
        //     .addOnSuccessListener...
        //     .addOnFailureListener...
        callback.onFailure(new Exception("Eliminar no implementado"));
    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<Usuario>> callback) {
        db.collection("usuarios").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Usuario> listaUsuarios = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Usuario u = document.toObject(Usuario.class);
                            listaUsuarios.add(u);
                        }
                        callback.onSuccess(listaUsuarios);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void buscarPorNombreUsuario(String nombreUsuario, OnDataLoadedCallback<Usuario> callback) {
        db.collection("usuarios")
                .whereEqualTo("usuario", nombreUsuario)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Usuario usuario = queryDocumentSnapshots.getDocuments().get(0).toObject(Usuario.class);
                            callback.onSuccess(usuario);
                        } else {
                            callback.onFailure(new Exception("Usuario no encontrado"));
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

    // --- Getters y Setters ---
    public String getUID() { return UID; }
    public void setUID(String UID) { this.UID = UID; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }

    @Override
    public String toString() {
        return "Usuario{" +
                "email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}