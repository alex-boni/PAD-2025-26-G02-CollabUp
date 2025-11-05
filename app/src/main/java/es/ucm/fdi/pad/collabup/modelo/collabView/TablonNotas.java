package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Map;

import es.ucm.fdi.pad.collabup.R;

public class TablonNotas extends AbstractCollabView {

    public TablonNotas() {
        this.nombre = "Tablón de Notas";
    }

    @Override
    protected Fragment getVistaGrande() {
        return null;
    }

    @Override
    protected View getPrevisualizacion(Context context) {
        ImageView iv = new ImageView(context);

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.tablon_no_border_collabview);

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
    public CollabView build(Map<String, Object> settings) {
        return null;
    }

    @Override
    public void populate(CollabItem item) {

    }

    @Override
    public List<CollabViewSetting> getCreationSettings() {
        return null;
    }
}
