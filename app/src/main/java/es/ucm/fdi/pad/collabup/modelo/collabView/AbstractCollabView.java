package es.ucm.fdi.pad.collabup.modelo.collabView;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.modelo.interfaz.DAO;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Clase general de los collabViews para no repetir métodos
public abstract class AbstractCollabView implements CollabView, DAO<AbstractCollabView> {

    private String idV;
    protected String nombre;

    private List<CollabItem> listaCollabItems; //lista de eventos

    public String getName() {
        return nombre;
    }

    //------------------VISTAS A SACAR

    //PRIORITARIO
    public Activity getFullViewActivity() { //devuelve vista específica general
        // todo hacer la activity con la vista general, con marcos a los lados y
        // arriba el nombre, y usar getVistaGrande
        return null; //todo
    }

    protected abstract Fragment getVistaGrande();

    //NO PRIORITARIO
    // La vista que está en el añadir
    public View getStaticAddCollabViewInListEntry(Context context) {
        // Vista horizontal con texto a la izquierda (75%) y area derecha (25%)
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int) (6 * density);
        int paddingStart = (int) (18 * density);

        container.setPaddingRelative(paddingStart, padding, padding, padding);

        TypedValue tv = new TypedValue();
        int bgColor = 0xFFE0E0E0; // gris claro por defecto
        if (context.getTheme().resolveAttribute(android.R.attr.colorPrimary, tv, true)) {
            bgColor = tv.data;
        }
        // Drawable con esquinas redondeadas
        int cornerRadius = (int) (8 * density);
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setShape(GradientDrawable.RECTANGLE);
        bgDrawable.setColor(bgColor);
        bgDrawable.setCornerRadius(cornerRadius);
        container.setBackground(bgDrawable);

        int itemHeight = (int) (112 * density); // 112dp

        // Asegura que la miniatura ocupa el ancho del parent y tiene altura fija
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemHeight
        );
        int margin = (int) (4 * density);
        lp.setMargins(margin, margin, margin, margin);
        container.setLayoutParams(lp);
        container.setGravity(Gravity.CENTER_VERTICAL);

        // Título
        TextView title = new TextView(context);
        title.setText((nombre != null && !nombre.isEmpty()) ? nombre : this.getClass().getSimpleName());
        title.setTextSize(24);

        TypedValue tvText = new TypedValue();
        if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, tvText, true)) {
            title.setTextColor(tvText.data);
        }

        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                3f // weight 3 -> 75% si el otro ocupa weight 1
        );
        titleLp.setMargins(0, 0, (int) (8 * density), 0); // separación derecha
        title.setLayoutParams(titleLp);

        // Área derecha
        LinearLayout rightArea = new LinearLayout(context);
        rightArea.setOrientation(LinearLayout.VERTICAL);
        rightArea.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
        );
        rightArea.setLayoutParams(rightLp);
        View preview = getPrevisualizacion(context);
        if (preview != null) {
            LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            preview.setLayoutParams(previewLp);
            rightArea.addView(preview);
        }

        container.addView(title);
        container.addView(rightArea);

        return container;
    }

    protected abstract View getPrevisualizacion(Context context);

    //getfragment
    public Fragment getInCollabFragment() { //el que sale en el collab
        return null; //TODO
    }

    //Vista para modificar los ajustes de un CollabView ya creado
    public Activity getEditSettingsActivity() {
        // TODO usar getFragment ajustes
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
