package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Set;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.fragmento.ListaFragment;

public class Lista extends AbstractCollabView {

    public Lista() {
        this.nombre = "Lista";
    }

    public static CollabView getTemplateInstance() {
        return new Lista();
    }

    @Override
    protected Fragment getVistaGrande(RecyclerView.Adapter<?> adapter) {
        ListaFragment fragment = ListaFragment.newInstance();
        fragment.setAdapter(adapter);
        fragment.setTitulo(this.nombre != null ? this.nombre : "Lista");
        return fragment;
    }

    @Override
    protected RecyclerView.Adapter<?> obtenerAdapter() {
        return new ListaAdapter(getListaCollabItems());
    }

    @Override
    protected View getPrevisualizacion(Context context) {
        ImageView iv = new ImageView(context);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.lista_no_border_collabview);
        iv.setImageBitmap(bmp);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return iv;
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
