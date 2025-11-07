package es.ucm.fdi.pad.collabup.modelo.collabView;

import com.google.firebase.firestore.FirebaseFirestore;

import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class DAOCollabItemImp {

    private FirebaseFirestore db;

    public DAOCollabItemImp() {
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
    }


    public void crear(CollabItem citem, OnOperationCallback callback) {
        // Guardar en Firestore en la colección "collabItem" dentro de su "collabs"
        // .add() crea un documento con un ID automático
        db.collection("collabs")
                .document(citem.getIdC())
                .collection("collabItem")
                .add(citem)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);

    }

    public void modificar(CollabItem citem, OnOperationCallback callback) {

    }
}
