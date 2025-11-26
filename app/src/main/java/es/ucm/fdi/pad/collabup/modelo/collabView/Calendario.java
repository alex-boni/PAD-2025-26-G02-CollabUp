package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.Set;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class Calendario extends AbstractCollabView {

    public Calendario() {
        this.nombre = "Calendario";
    }

    public static CollabView getStaticInstance() {
        return new Calendario();
    }

    @Override
    protected Fragment getVistaGrande() {
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
    public void populate(CollabItem item, OnOperationCallback callback) {
        //lega un item a este view -> añadir el collab item a la lista correspondiente
        this.listaCollabItems.add(item);
        //Actualizar firebase
        this.modificar(this, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });

    }

    @Override
    public void remove(CollabItem item, OnOperationCallback callback) {
        this.listaCollabItems.remove(item);
        this.modificar(this, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public Set<CollabViewSetting> getStaticCreationSettings() {
        return Collections.emptySet();
    }


}
