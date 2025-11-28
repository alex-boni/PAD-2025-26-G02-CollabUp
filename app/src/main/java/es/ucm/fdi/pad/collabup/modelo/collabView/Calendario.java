package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CalendarioFragment;

public class Calendario extends AbstractCollabView {

    public Calendario() {
        this.nombre = "Calendario";
    }

    public static CollabView getStaticInstance() {
        return new Calendario();
    }

    @Override
    protected Fragment getVistaGrande(RecyclerView.Adapter<?> adapter) {
        //el calendario es un poco diferente y no puede definir el adapter aqui
        ArrayList<String> ids = new ArrayList<>();
        for (CollabItem item : getListaCollabItems()) {
            ids.add(item.getIdI());
        }

        CalendarioFragment cal = CalendarioFragment.newInstance(getCollabId(), getUid(), ids);
        cal.setAdapter(adapter);
        cal.setTitulo(this.nombre);
        return cal; //abro el calendario
    }

    @Override
    protected RecyclerView.Adapter<?> obtenerAdapter() {
        return null;
    }

    @Override
    protected View getPrevisualizacion(Context context) {
        ImageView iv = new ImageView(context);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.calendario_no_border_collabview);
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
