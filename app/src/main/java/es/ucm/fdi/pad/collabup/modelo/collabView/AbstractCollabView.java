package es.ucm.fdi.pad.collabup.modelo.collabView;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public abstract class AbstractCollabView implements CollabView {

    private String collabId; //id del collab al que pertenece
    private String uid; //id del collabview
    protected String nombre;
    private Map<CollabViewSetting, Object> settings; //ajustes del collabview
    private List<CollabItem> listaCollabItems; //lista de eventos
    protected RecyclerView.Adapter<?> adapter; //adapter singleton que se reutiliza en los fragments

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AbstractCollabView() {
        this.settings = new HashMap<>();
        this.listaCollabItems = new ArrayList<>();
        getStaticCreationSettings().forEach((s) -> this.settings.put(s, null));
    }

    public String getCollabId() {
        return collabId;
    }

    public void setCollabId(String collabId) {
        this.collabId = collabId;
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

    @Override
    public final void populate(CollabItem item, OnOperationCallback onOperationCallback) {
        if (!listaCollabItems.contains(item)) {
            listaCollabItems.add(item);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            actualizarCollabViewEnBD(onOperationCallback);
        } else {
            onOperationCallback.onFailure(new Exception("El CollabItem ya existe en la CollabView."));
        }
    }

    @Override
    public final void remove(CollabItem item, OnOperationCallback onOperationCallback) {
        if (listaCollabItems.remove(item)) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            actualizarCollabViewEnBD(onOperationCallback);
        } else {
            onOperationCallback.onFailure(new Exception("El CollabItem no existe en la CollabView."));
        }
    }

    /**
     * Carga los CollabItems desde la base de datos dado sus IDs
     */
    private void cargarItemsDesdeDB(String collabId, List<String> itemIds, OnDataLoadedCallback<List<CollabItem>> callback) {
        if (itemIds == null || itemIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<CollabItem> items = new ArrayList<>();
        AtomicInteger contador = new AtomicInteger(0);
        AtomicInteger totalCargados = new AtomicInteger(0);

        for (String itemId : itemIds) {
            CollabItem item = new CollabItem();
            item.setIdC(collabId);
            item.setIdI(itemId);

            item.obtener(itemId, new OnDataLoadedCallback<>() {
                @Override
                public void onSuccess(CollabItem loadedItem) {
                    if (loadedItem != null) {
                        items.add(loadedItem);
                        totalCargados.incrementAndGet();
                    }
                    if (contador.incrementAndGet() == itemIds.size()) {
                        callback.onSuccess(items);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Continuar aunque falle la carga de un item individual
                    if (contador.incrementAndGet() == itemIds.size()) {
                        callback.onSuccess(items);
                    }
                }
            });
        }
    }

    /**
     * Actualiza el CollabView en la BD con la lista actual de items
     */
    private void actualizarCollabViewEnBD(OnOperationCallback callback) {
        List<String> itemIds = new ArrayList<>();
        for (CollabItem item : listaCollabItems) {
            if (item.getIdI() != null) {
                itemIds.add(item.getIdI());
            }
        }

        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(uid)
                .update("items", itemIds)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }



    @Override
    public Fragment getFullViewFragment() {
        // Crear el adapter si aún no existe (singleton)
        if (adapter == null) {
            adapter = obtenerAdapter();
        }
        return getVistaGrande(adapter);
    }

    /**
     * Devuelve un Fragment personalizado para mostrar la vista completa del CollabView.
     *
     * @param adapter El adapter ya creado por AbstractCollabView (this.adapter)
     * @return Fragment personalizado
     */
    protected abstract Fragment getVistaGrande(RecyclerView.Adapter<?> adapter);

    /**
     * Las subclases deben proporcionar el adapter
     */
    protected abstract RecyclerView.Adapter<?> obtenerAdapter();

    public List<CollabItem> getListaCollabItems() {
        // Devolver la lista interna para que los adapters que la referencien se actualicen cuando
        // se añadan/eliminar items y se invoque adapter.notifyDataSetChanged().
        return listaCollabItems;
    }

    //NO PRIORITARIO
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

    public Fragment getInCollabFragment() {
        return null;
    }

    public Fragment getEditSettingsFragment() {
        return getFragmentAjustes();
    }

    protected abstract Fragment getFragmentAjustes();

    @Override
    public Map<CollabViewSetting, Object> getSettings() {
        return new HashMap<>(settings);
    }

    @Override
    public CollabView build(String collabId, String uid, String name, Map<String, Object> settings, List<CollabItem> items) {
        AbstractCollabView cv;
        try {
            cv = (AbstractCollabView) this.getClass().getMethod("getStaticInstance").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        cv.collabId = collabId;
        cv.uid = uid;
        cv.nombre = name;
        for (CollabViewSetting s : cv.settings.keySet()) {
            cv.settings.put(s, settings.getOrDefault(s.getName(), null));
        }
        if (items != null) {
            cv.listaCollabItems.addAll(items);
        }
        return cv;
    }

    //----------------- MÉTODOS DEL DAO

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

                            // Cargar los items desde la BD si existen
                            if (t.items != null && !t.items.isEmpty()) {
                                cargarItemsDesdeDB(collabId, t.items, new OnDataLoadedCallback<List<CollabItem>>() {
                                    @Override
                                    public void onSuccess(List<CollabItem> items) {
                                        CollabView cv = cvStatic.build(collabId, documentSnapshot.getId(), t.name, t.settings, items);
                                        callback.onSuccess(cv);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Si falla la carga de items, crear el CollabView sin items
                                        CollabView cv = cvStatic.build(collabId, documentSnapshot.getId(), t.name, t.settings, new ArrayList<>());
                                        callback.onSuccess(cv);
                                    }
                                });
                            } else {
                                CollabView cv = cvStatic.build(collabId, documentSnapshot.getId(), t.name, t.settings, new ArrayList<>());
                                callback.onSuccess(cv);
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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

        CollabViewTransfer t = new CollabViewTransfer(nombre, this.getClass().getSimpleName(), settings, Arrays.asList(listaCollabItems.stream().map(CollabItem::getIdI).toArray(String[]::new)));

        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .add(t)
                .addOnSuccessListener(documentReference -> {
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
                reemplazo.getName(),
                reemplazo.getClass().getSimpleName(),
                settings,
                Arrays.asList(listaCollabItems.stream().map(CollabItem::getIdI).toArray(String[]::new))
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
                    AtomicInteger totalDocs = new AtomicInteger(queryDocumentSnapshots.size());
                    AtomicInteger procesados = new AtomicInteger(0);

                    if (totalDocs.get() == 0) {
                        callback.onSuccess(collabViews);
                        return;
                    }

                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        CollabViewTransfer t = doc.toObject(CollabViewTransfer.class);
                        if (t != null) {
                            try {
                                CollabView cvStatic = (CollabView) reg.get(t.type).getMethod("getStaticInstance").invoke(null);
                                assert cvStatic != null;

                                // Cargar los items desde la BD si existen
                                if (t.items != null && !t.items.isEmpty()) {
                                    cargarItemsDesdeDB(collabId, t.items, new OnDataLoadedCallback<List<CollabItem>>() {
                                        @Override
                                        public void onSuccess(List<CollabItem> items) {
                                            CollabView cv = cvStatic.build(collabId, doc.getId(), t.name, t.settings, items);
                                            collabViews.add(cv);
                                            if (procesados.incrementAndGet() == totalDocs.get()) {
                                                callback.onSuccess(collabViews);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            // Si falla la carga de items, crear el CollabView sin items
                                            CollabView cv = cvStatic.build(collabId, doc.getId(), t.name, t.settings, new ArrayList<>());
                                            collabViews.add(cv);
                                            if (procesados.incrementAndGet() == totalDocs.get()) {
                                                callback.onSuccess(collabViews);
                                            }
                                        }
                                    });
                                } else {
                                    CollabView cv = cvStatic.build(collabId, doc.getId(), t.name, t.settings, new ArrayList<>());
                                    collabViews.add(cv);
                                    if (procesados.incrementAndGet() == totalDocs.get()) {
                                        callback.onSuccess(collabViews);
                                    }
                                }
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            if (procesados.incrementAndGet() == totalDocs.get()) {
                                callback.onSuccess(collabViews);
                            }
                        }
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void obtenerNombresCollabViewdeCollab(List<String> idsCV, OnDataLoadedCallback<Map<String, String>> callback) {
        Map<String, String> resultados = new HashMap<>();
        AtomicInteger contador = new AtomicInteger(0);

        if (idsCV.isEmpty()) {
            callback.onSuccess(resultados);
            return;
        }

        for (String id : idsCV) {
            CollabView aux = new Calendario();
            aux.obtener(id, new OnDataLoadedCallback<CollabView>() {
                @Override
                public void onSuccess(CollabView cv) {
                    resultados.put(id, cv.getName());
                    if (contador.incrementAndGet() == idsCV.size()) {
                        callback.onSuccess(resultados);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }
    }

    public static class CollabViewTransfer {
        public CollabViewTransfer() {
        }

        public CollabViewTransfer(String name, String type, Map<String, Object> settings, List<String> items) {
            this.name = name;
            this.type = type;
            this.settings = settings;
            this.items = items;
        }

        public String name;
        public String type;
        public Map<String, Object> settings;

        public List<String> items;
    }
}
