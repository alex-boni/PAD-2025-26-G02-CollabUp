package es.ucm.fdi.pad.collabup.modelo.interfaz;


// Este callback nos dirá si una operación (como crear, borrar) tuvo éxito o falló.
public interface OnOperationCallback {
    void onSuccess();
    void onFailure(Exception e);
}