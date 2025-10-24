package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.app.Activity;

import androidx.fragment.app.Fragment;

//Interfaz collabViews
public interface CollabView {
    Activity getVistaCollabView();
    Fragment getFragmentEnCollab();
    Activity getVistaAjustes();
    AbstractCollabView construir();
    //TODO Añadir getMiniatura
}
