package es.ucm.fdi.pad.collabup.modelo.collabView;


import android.app.Activity;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Clase general de los collabViews para no repetir métodos
public abstract class AbstractCollabView implements CollabView, DAO<AbstractCollabView> {

    private String idV;
    private String nombre;

    private List<CollabItem> listaCollabItems; //lista de eventos

    public abstract AbstractCollabView construir();

    //------------------VISTAS A SACAR

    //PRIORITARIO
    public Activity getVistaCollabView() { //devuelve vista específica general
        // todo hacer la activity con la vista general, con marcos a los lados y
        // arriba el nombre, y usar getVistaGrande
        return null; //todo
    }

    protected abstract Fragment getVistaGrande();

    //NO PRIORITARIO
    // La vista que está en el añadir
    public static Fragment getMiniatura() {
        //todo construir vista pequeñita de lista y usar getPrevisualizacion
        return null;
    }

    protected abstract Fragment getPrevisualizacion();

    //getfragment
    public Fragment getFragmentEnCollab() { //el que sale en el collab

        return null;
    }

    //Vista de los ajustes al añadir un view
    public static Fragment getFragCrearAjustes() { //todo en cada clase
        return null;
    }


    //Vista cuando le das al + y después le das a un View
    public static Activity getVistaCrearCollabView() {
        // todo usar getFragCrearAjustes();
        return null;
    }

    //Vista para modificar los ajustes de un CollabView ya creado
    public Activity getVistaAjustes(){
        //usar getFragment ajustes
        return null;
    }
    protected abstract Fragment getFragmentAjustes();


    //----------------- MÉTODOS DEL DAO, ACCESO A BASE DE DATOS
    @Override
    public void obtener(String identificador, OnDataLoadedCallback<AbstractCollabView> callback) {

    }

    @Override
    public void crear(OnOperationCallback callback) {

    }

    @Override
    public void modificar(AbstractCollabView reemplazo, OnOperationCallback callback) {

    }

    @Override
    public void eliminar(OnOperationCallback callback) {

    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<AbstractCollabView>> callback) {

    }
}
