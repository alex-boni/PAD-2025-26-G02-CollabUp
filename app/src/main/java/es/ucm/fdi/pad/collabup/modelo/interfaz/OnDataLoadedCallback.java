package es.ucm.fdi.pad.collabup.modelo.interfaz;

// Este callback nos devolverá los datos que pedimos (un objeto o una lista) o un error.
public interface OnDataLoadedCallback<T> {
    void onSuccess(T data);
    void onFailure(Exception e);
}
