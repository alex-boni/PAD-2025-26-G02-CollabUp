package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.util.Map;
import java.util.Set;

import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;

//Interfaz collabViews
public interface CollabView extends DAO<CollabView> {

    String getUid();

    void setUid(String uid);

    /**
     * Obtiene el nombre del CollabView.
     *
     * @return el nombre del CollabView
     */
    String getName();

    void setName(String name);

    String getCollabId();

    void setCollabId(String collabId);

    /**
     * Devuelve una nueva instancia estática de un CollabView.
     * para invocar metodos pseudo-estaticos
     * Este método DEBE ser implementado en cada subclase.
     *
     * @return una nueva instancia de la CollabView
     */
    static CollabView getStaticInstance() {
        throw new AssertionError("Este método debe ser implementado en cada subclase de CollabView");
    }

    /**
     * Construye una instancia usable de CollabView a partir de un collabId y mapa de configuraciones.
     *
     * @param settings especificas para cada CollabView.
     * @return una nueva instancia de CollabView configurada.
     */
    CollabView build(String collabId, String uid, String name, Map<String, Object> settings);

    /**
     * Popula la CollabView con el CollabItem proporcionado.
     *
     * @param item el CollabItem que contiene los datos para poblar la vista
     */
    void populate(CollabItem item);

    void remove(CollabItem item);

    /**
     * Obtiene la actividad que muestra la vista completa de la CollabView.
     *
     * @return la actividad de vista completa
     */
    Activity getFullViewActivity();

    /**
     * Obtiene la vista a añadir a la lista de CollabViews cuando se quiere agregar
     * una nueva CollabView a un Collab
     *
     * @param context el contexto para crear la vista
     * @return la vista para añadir en la lista de CollabViews disponibles
     */
    View getStaticAddCollabViewInListEntry(Context context);

    /**
     * Obtiene el fragmento que se utiliza para mostrar la CollabView dentro la activity
     * de un Collab.
     *
     * @return el fragmento de la CollabView en el Collab
     */
    Fragment getInCollabFragment();

    /**
     * Obtiene la actividad que permite editar los ajustes de una instancia de CollabView.
     *
     * @return la actividad de edición de ajustes
     */
    Activity getEditSettingsActivity();

    /**
     * Obtiene los ajustes disponibles para la creación de una nueva instancia de CollabView.
     * Boolean indica si el ajuste es obligatorio (true) o opcional (false).
     * List<String> contiene las posibles opciones para ajustes que requieran selección.
     *
     * @return un conjunto de pares clave-obligatoriedad que representan los ajustes de creación
     */
    Set<CollabViewSetting> getStaticCreationSettings();

    Map<CollabViewSetting, Object> getSettings();


}
