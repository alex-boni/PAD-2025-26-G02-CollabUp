package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.pad.collabup.R;

public class Calendario extends AbstractCollabView {

    public Calendario() {
        this.nombre = "Calendario";
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
    public CollabView build(Map<String, Object> settings) {
        return null;
    }

    @Override
    public void populate(CollabItem item) {

    }

    @Override
    public List<CollabViewSetting> getCreationSettings() {
        // Lista de prueba con un ajuste de cada tipo
        List<CollabViewSetting> settings = new ArrayList<>();

        // TEXTO: nombre/título del calendario, obligatorio, máximo 50 caracteres (convención)
        settings.add(new CollabViewSetting(CollabViewSetting.CollabViewSettingsType.TEXTO,
                "Título", true, Collections.singletonList("50")));

        // NUMERO: número máximo de eventos a mostrar, opcional, rango 0-100
        settings.add(new CollabViewSetting(CollabViewSetting.CollabViewSettingsType.NUMERO,
                "Máx. eventos", false, Arrays.asList("0", "100")));

        // LISTA_OPCIONES: visibilidad, obligatorio, opciones: Público/Privado/Compartido
        settings.add(new CollabViewSetting(CollabViewSetting.CollabViewSettingsType.LISTA_OPCIONES,
                "Visibilidad", true, Arrays.asList("Público", "Privado", "Compartido")));

        // BOOLEANO: activar notificaciones, opcional
        settings.add(new CollabViewSetting(CollabViewSetting.CollabViewSettingsType.BOOLEANO,
                "Notificaciones activas", false, null));

        settings.add(new CollabViewSetting(CollabViewSetting.CollabViewSettingsType.BOOLEANO,
                "Mostrar festivos", false, null));
        return settings;
    }
}
