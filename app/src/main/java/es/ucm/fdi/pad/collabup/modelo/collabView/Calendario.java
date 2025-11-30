package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.fragmento.CalendarioFragment;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.adapters.CollabItemAdapter;

public class Calendario extends AbstractCollabView {

    public Calendario() {
        this.nombre = "Calendario";
    }

    public static CollabView getTemplateInstance() {
        return new Calendario();
    }

    @Override
    protected Fragment getVistaGrande(RecyclerView.Adapter<?> adapter) {
        //el calendario es un poco diferente y no puede definir el adapter aqui
        ArrayList<String> ids = new ArrayList<>();
        for (CollabItem item : getListaCollabItems()) {
            ids.add(item.getIdI());
        }

        // Obtener el día de inicio de semana desde los settings (en español)
        String diaInicio = (String) getSettingValue("calendario_dia_inicio_semana");
        if (diaInicio == null) {
            diaInicio = "Lunes";
        }

        CalendarioFragment cal = CalendarioFragment.newInstance(getCollabId(), getUid(), ids, diaInicio);
        cal.setAdapter(adapter);
        if (adapter instanceof CollabItemAdapter) {
            ((CollabItemAdapter) adapter).setListener(cal.cambiarAItem());
        }
        cal.setTitulo(this.nombre);
        return cal; //abro el calendario
    }


    @Override
    protected RecyclerView.Adapter<?> obtenerAdapter() {
        return new CollabItemAdapter(new ArrayList<>());
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
        return Set.of(new CollabViewSetting("calendario_dia_inicio_semana", CollabViewSetting.CollabViewSettingsType.LISTA_OPCIONES, "Dia de inicio de semana", true, List.of("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")));
    }


}
