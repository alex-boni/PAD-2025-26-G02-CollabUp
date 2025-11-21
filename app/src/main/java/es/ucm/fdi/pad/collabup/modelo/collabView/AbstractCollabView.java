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

import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Clase general de los collabViews para no repetir métodos
public abstract class AbstractCollabView implements CollabView {

    private String collabId; //id del collab al que pertenece
    private String uid; //id del collabview
    protected String nombre;
    private Map<CollabViewSetting, Object> settings; //ajustes del collabview
    private List<CollabItem> listaCollabItems; //lista de eventos

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AbstractCollabView() {
        this.settings = new HashMap<>();
        getStaticCreationSettings().forEach((s) -> this.settings.put(s, null));
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String getName() {
        return nombre;
    }

    @Override
    public void setName(String name) {
        this.nombre = name;
    }

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

    @Override
    public Map<CollabViewSetting, Object> getSettings() {
        return new HashMap<>(settings);
    }

    @Override
    public CollabView build(String collabId, String uid, String name, Map<String, Object> settings) {
        AbstractCollabView cv;
        try {
            cv = (AbstractCollabView) this.getClass().getMethod("getStaticInstance").invoke(null);
        } catch (IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        cv.collabId = collabId;
        cv.uid = uid;
        cv.nombre = name;
        for (CollabViewSetting s : cv.settings.keySet()) {
            cv.settings.put(s, settings.getOrDefault(s.getName(), null));
        }
        return cv;
    }


//----------------- MÉTODOS DEL DAO, ACCESO A BASE DE DATOS


    @Override
    public void obtener(String identificador, OnDataLoadedCallback<CollabView> callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(identificador)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    CollabViewTransfer t = documentSnapshot.toObject(CollabViewTransfer.class);
                    if (t != null) {
                        Registry<CollabView> reg = Registry.getRegistry(CollabView.class);
                        try {
                            CollabView cvStatic = (CollabView) reg.get(t.type).getMethod("getStaticInstance").invoke(null);

                            assert cvStatic != null;
                            CollabView cv = cvStatic.build(collabId, documentSnapshot.getId(), t.name, t.settings);

                            callback.onSuccess(cv);
                        } catch (IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void crear(OnOperationCallback callback) {
        Map<String, Object> settings = new HashMap<>();
        for (Map.Entry<CollabViewSetting, Object> entry : this.settings.entrySet()) {
            settings.put(entry.getKey().getName(), entry.getValue());
        }

        CollabViewTransfer t = new CollabViewTransfer(null, nombre, this.getClass().getSimpleName(), settings);

        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .add(t)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar el uid del CollabView con el ID generado por Firestore
                    this.uid = documentReference.getId();
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void modificar(CollabView reemplazo, OnOperationCallback callback) {
        Map<String, Object> settings = new HashMap<>();
        for (Map.Entry<CollabViewSetting, Object> entry : reemplazo.getSettings().entrySet()) {
            settings.put(entry.getKey().getName(), entry.getValue());
        }
        CollabViewTransfer t = new CollabViewTransfer(
                reemplazo.getUid(),
                reemplazo.getName(),
                reemplazo.getClass().getSimpleName(),
                settings
        );
        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(reemplazo.getUid())
                .set(t)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void eliminar(OnOperationCallback callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(uid)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void obtenerListado(OnDataLoadedCallback<ArrayList<CollabView>> callback) {
        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<CollabView> collabViews = new ArrayList<>();
                    Registry<CollabView> reg = Registry.getRegistry(CollabView.class);
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        CollabViewTransfer t = doc.toObject(CollabViewTransfer.class);
                        if (t != null) {
                            try {
                                CollabView cvStatic = (CollabView) reg.get(t.type).getMethod("getStaticInstance").invoke(null);

                                assert cvStatic != null;
                                CollabView cv = cvStatic.build(collabId, doc.getId(), t.name, t.settings);
                                collabViews.add(cv);
                            } catch (IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    callback.onSuccess(collabViews);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static class CollabViewTransfer {
        public CollabViewTransfer(String uid, String name, String type, Map<String, Object> settings) {
            this.uid = uid;
            this.name = name;
            this.type = type;
            this.settings = settings;
        }

        public String uid;
        public String name;
        public String type;
        public Map<String, Object> settings;
    }
}
