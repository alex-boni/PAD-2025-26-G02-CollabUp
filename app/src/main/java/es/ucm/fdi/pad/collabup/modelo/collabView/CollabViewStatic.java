package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Set;

public class CollabViewStatic extends AbstractCollabView {

    public CollabViewStatic() {
        this.nombre = "Vista Estática";
    }

    public static CollabView getTemplateInstance() {
        throw new IllegalStateException("Esta clase no debe ser instanciada directamente.");
    }


    @Override
    protected Fragment getVistaGrande(RecyclerView.Adapter<?> adapter) {
        return null;
    }

    @Override
    protected RecyclerView.Adapter<?> obtenerAdapter() {
        return null;
    }

    @Override
    protected View getPrevisualizacion(Context context) {
        return null;
    }

    @Override
    protected Fragment getFragmentAjustes() {
        return null;
    }

    @Override
    public Set<CollabViewSetting> getStaticCreationSettings() {
        return Collections.emptySet();
    }
}
