package es.ucm.fdi.pad.collabup.modelo.interfaz;

import java.util.ArrayList;

/**
 * Interfaz genérica de Acceso a Datos (DAO) adaptada para operaciones asíncronas
 * como las de Firebase. En lugar de devolver valores directamente, usa callbacks.
 *
 * @param <T> El tipo de objeto del modelo que manejará este DAO (ej. Usuario)
 */
public interface DAO<T> {

    /**
     * Obtiene un objeto específico por su identificador.
     * @param identificador El ID del documento.
     * @param callback El listener que será notificado con el resultado.
     */
    void obtener(String identificador, OnDataLoadedCallback<T> callback);

    /**
     * Crea un nuevo objeto en la base de datos.
     * @param callback El listener que será notificado sobre el éxito o fracaso.
     */
    void crear(OnOperationCallback callback);

    /**
     * Modifica un objeto existente en la base de datos.
     * @param reemplazo El objeto con los datos actualizados.
     * @param callback El listener que será notificado sobre el éxito o fracaso.
     */
    void modificar(T reemplazo, OnOperationCallback callback);

    /**
     * Elimina un objeto de la base de datos.
     * @param callback El listener que será notificado sobre el éxito o fracaso.
     */
    void eliminar(OnOperationCallback callback);

    /**
     * Obtiene una lista de todos los objetos de una colección.
     * @param callback El listener que será notificado con la lista de resultados.
     */
    void obtenerListado(OnDataLoadedCallback<ArrayList<T>> callback);
}
