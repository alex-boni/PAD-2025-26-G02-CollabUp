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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.controlador.fragmento.FullViewHostFragment;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

/**
 * Implementación base de CollabView con funcionalidad común.
 * Delega en las subclases solo la creación de vistas y adapters específicos, y
 * los settings que cada una quiera definir.
 */
public abstract class AbstractCollabView implements CollabView {

    private String collabId; //id del collab al que pertenece
    private String uid; //id del collabview
    protected String nombre;
    private Map<CollabViewSetting, Object> settings; //ajustes del collabview
    private Map<String, Object> settingsById; //índice por id para acceso O(1)
    private List<CollabItem> listaCollabItems; //lista de eventos
    protected RecyclerView.Adapter<?> adapter; //adapter singleton que se reutiliza en los fragments

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AbstractCollabView() {
        this.settings = new HashMap<>();
        this.settingsById = new HashMap<>();
        this.listaCollabItems = new ArrayList<>();
        if (getStaticCreationSettings() != null) {
            getStaticCreationSettings().forEach((s) -> {
                this.settings.put(s, null);
                this.settingsById.put(s.getId(), null);
            });
        }
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

            actualizarCollabViewEnBD(new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    if (uid == null || uid.isEmpty() || item.getIdI() == null) {
                        onOperationCallback.onSuccess();
                        return;
                    }

                    db.collection("collabs")
                            .document(collabId)
                            .collection("collabItems")
                            .document(item.getIdI())
                            .update("cvAsignadas", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener(aVoid -> {
                                List<String> cvs = item.getcvAsignadas();
                                if (cvs == null) cvs = new ArrayList<>();
                                if (!cvs.contains(uid)) {
                                    cvs.add(uid);
                                    item.setcvAsignadas(cvs);
                                }
                                onOperationCallback.onSuccess();
                            })
                            .addOnFailureListener(e -> onOperationCallback.onFailure(e));
                }

                @Override
                public void onFailure(Exception e) {
                    onOperationCallback.onFailure(e);
                }
            });
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

            actualizarCollabViewEnBD(new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    if (uid == null || uid.isEmpty() || item.getIdI() == null) {
                        onOperationCallback.onSuccess();
                        return;
                    }

                    db.collection("collabs")
                            .document(collabId)
                            .collection("collabItems")
                            .document(item.getIdI())
                            .update("cvAsignadas", FieldValue.arrayRemove(uid))
                            .addOnSuccessListener(aVoid -> {
                                List<String> cvs = item.getcvAsignadas();
                                if (cvs != null && cvs.contains(uid)) {
                                    cvs.remove(uid);
                                    item.setcvAsignadas(cvs);
                                }
                                onOperationCallback.onSuccess();
                            })
                            .addOnFailureListener(e -> onOperationCallback.onFailure(e));
                }

                @Override
                public void onFailure(Exception e) {
                    onOperationCallback.onFailure(e);
                }
            });
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
                .update("ciAsignados", itemIds)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }


    @Override
    public Fragment getFullViewFragment() {
        // Crear el adapter si aún no existe (singleton)
        if (adapter == null) {
            adapter = obtenerAdapter();
        }
        Fragment content = getVistaGrande(adapter);
        // Usar un host con toolbar para mostrar el título y contener la vista grande
        FullViewHostFragment host = FullViewHostFragment.newInstance(this.nombre != null ? this.nombre : (this.getClass().getSimpleName()));
        host.setContent(content);
        host.setCollabView(this);
        return host;
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

    /**
     * Obtiene el valor de un setting de forma eficiente por su id en O(1).
     *
     * @param settingId El id único del setting
     * @return El valor del setting o null si no existe
     */
    protected Object getSettingValue(String settingId) {
        return settingsById.get(settingId);
    }

    @Override
    public Map<CollabViewSetting, Object> getSettings() {
        return new HashMap<>(settings);
    }

    @Override
    public CollabView build(String collabId, String uid, String name, Map<String, Object> settings, List<CollabItem> items) {
        AbstractCollabView cv;
        try {
            cv = (AbstractCollabView) this.getClass().getMethod("getTemplateInstance").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        cv.collabId = collabId;
        cv.uid = uid;
        cv.nombre = name;
        for (CollabViewSetting s : cv.settings.keySet()) {
            Object value = settings.getOrDefault(s.getName(), null);
            cv.settings.put(s, value);
            cv.settingsById.put(s.getId(), value);
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
                        Registry<String, CollabView> reg = Registry.getRegistry(CollabView.class);
                        try {
                            CollabView cvStatic = reg.createTemplate(t.type);
                            assert cvStatic != null;

                            // Cargar los items desde la BD si existen
                            if (t.ciAsignados != null && !t.ciAsignados.isEmpty()) {
                                cargarItemsDesdeDB(collabId, t.ciAsignados, new OnDataLoadedCallback<List<CollabItem>>() {
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
                        } catch (Exception e) {
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

                    List<String> itemIds = Arrays.asList(listaCollabItems.stream().map(CollabItem::getIdI).toArray(String[]::new));

                    if (itemIds.isEmpty()) {
                        // No hay items que actualizar
                        callback.onSuccess();
                        return;
                    }

                    AtomicInteger processed = new AtomicInteger(0);
                    AtomicInteger failures = new AtomicInteger(0);

                    for (String ciId : itemIds) {
                        db.collection("collabs")
                                .document(collabId)
                                .collection("collabItems")
                                .document(ciId)
                                .update("cvAsignadas", FieldValue.arrayUnion(this.uid))
                                .addOnSuccessListener(aVoid -> {
                                    for (CollabItem localItem : listaCollabItems) {
                                        if (localItem != null && ciId.equals(localItem.getIdI())) {
                                            List<String> cvs = localItem.getcvAsignadas();
                                            if (cvs == null) cvs = new ArrayList<>();
                                            if (!cvs.contains(this.uid)) {
                                                cvs.add(this.uid);
                                                localItem.setcvAsignadas(cvs);
                                            }
                                            break;
                                        }
                                    }

                                    if (processed.incrementAndGet() == itemIds.size()) {
                                        if (failures.get() == 0) {
                                            callback.onSuccess();
                                        } else {
                                            callback.onFailure(new Exception("Algunos CollabItems no se pudieron actualizar"));
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    failures.incrementAndGet();
                                    if (processed.incrementAndGet() == itemIds.size()) {
                                        if (failures.get() == 0) {
                                            callback.onSuccess();
                                        } else {
                                            callback.onFailure(new Exception("Algunos CollabItems no se pudieron actualizar"));
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public List<CollabItem> getItems() {
        return Collections.unmodifiableList(listaCollabItems);
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
                Arrays.asList(reemplazo.getItems().stream().map(CollabItem::getIdI).toArray(String[]::new))
        );

        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(reemplazo.getUid())
                .set(t)
                .addOnSuccessListener(v -> {
                    // Tras actualizar el documento del CollabView, debemos sincronizar cada CollabItem:
                    // - obtener la lista previa de ids (antes de modificar)
                    // - obtener la lista nueva de ids (desde 'reemplazo')
                    // - calcular added = new - prev ; removed = prev - new
                    List<String> prevIds = new ArrayList<>();
                    for (CollabItem it : this.listaCollabItems) {
                        if (it != null && it.getIdI() != null) prevIds.add(it.getIdI());
                    }

                    List<String> newIds = new ArrayList<>();
                    List<CollabItem> newItems = reemplazo.getItems();
                    if (newItems != null) {
                        for (CollabItem it : newItems) {
                            if (it != null && it.getIdI() != null) newIds.add(it.getIdI());
                        }
                    }

                    List<String> toAdd = new ArrayList<>(newIds);
                    toAdd.removeAll(prevIds);
                    List<String> toRemove = new ArrayList<>(prevIds);
                    toRemove.removeAll(newIds);

                    // Si no hay cambios en asignación de items, actualizar la lista local y terminar
                    if (toAdd.isEmpty() && toRemove.isEmpty()) {
                        // Actualizar lista local con los objetos proporcionados en reemplazo
                        this.listaCollabItems.clear();
                        if (newItems != null) this.listaCollabItems.addAll(newItems);
                        if (adapter != null) adapter.notifyDataSetChanged();
                        callback.onSuccess();
                        return;
                    }

                    // Procesar cambios en CollabItems (añadir/quitar la referencia a esta collabView)
                    AtomicInteger processed = new AtomicInteger(0);
                    AtomicInteger failures = new AtomicInteger(0);
                    int totalOps = toAdd.size() + toRemove.size();

                    // Helper para comprobar finalización
                    Runnable checkFinish = () -> {
                        if (processed.get() == totalOps) {
                            // Tras procesar, actualizar la lista local para reflejar el reemplazo
                            this.listaCollabItems.clear();
                            if (newItems != null) this.listaCollabItems.addAll(newItems);
                            if (adapter != null) adapter.notifyDataSetChanged();

                            if (failures.get() == 0) {
                                callback.onSuccess();
                            } else {
                                callback.onFailure(new Exception("Algunos CollabItems no se pudieron actualizar"));
                            }
                        }
                    };

                    // Añadir uid a los CollabItems nuevos
                    for (String ciId : toAdd) {
                        db.collection("collabs")
                                .document(collabId)
                                .collection("collabItems")
                                .document(ciId)
                                .update("cvAsignadas", FieldValue.arrayUnion(reemplazo.getUid()))
                                .addOnSuccessListener(aVoid -> {
                                    // Actualizar objeto local si existe en listaCollabItems
                                    for (CollabItem local : listaCollabItems) {
                                        if (local != null && ciId.equals(local.getIdI())) {
                                            List<String> cvs = local.getcvAsignadas();
                                            if (cvs == null) cvs = new ArrayList<>();
                                            if (!cvs.contains(reemplazo.getUid())) {
                                                cvs.add(reemplazo.getUid());
                                                local.setcvAsignadas(cvs);
                                            }
                                            break;
                                        }
                                    }
                                    // También actualizar en newItems (por si el CollabItem es uno de los recién asignados)
                                    if (newItems != null) {
                                        for (CollabItem ni : newItems) {
                                            if (ni != null && ciId.equals(ni.getIdI())) {
                                                List<String> cvs2 = ni.getcvAsignadas();
                                                if (cvs2 == null) cvs2 = new ArrayList<>();
                                                if (!cvs2.contains(reemplazo.getUid())) {
                                                    cvs2.add(reemplazo.getUid());
                                                    ni.setcvAsignadas(cvs2);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    processed.incrementAndGet();
                                    checkFinish.run();
                                })
                                .addOnFailureListener(e -> {
                                    failures.incrementAndGet();
                                    processed.incrementAndGet();
                                    checkFinish.run();
                                });
                    }

                    // Quitar uid de los CollabItems eliminados
                    for (String ciId : toRemove) {
                        db.collection("collabs")
                                .document(collabId)
                                .collection("collabItems")
                                .document(ciId)
                                .update("cvAsignadas", FieldValue.arrayRemove(reemplazo.getUid()))
                                .addOnSuccessListener(aVoid -> {
                                    // Actualizar objeto local si existe
                                    for (CollabItem local : listaCollabItems) {
                                        if (local != null && ciId.equals(local.getIdI())) {
                                            List<String> cvs = local.getcvAsignadas();
                                            if (cvs != null && cvs.contains(reemplazo.getUid())) {
                                                cvs.remove(reemplazo.getUid());
                                                local.setcvAsignadas(cvs);
                                            }
                                            break;
                                        }
                                    }
                                    // También quitar de newItems si aparece allí
                                    if (newItems != null) {
                                        for (CollabItem ni : newItems) {
                                            if (ni != null && ciId.equals(ni.getIdI())) {
                                                List<String> cvs2 = ni.getcvAsignadas();
                                                if (cvs2 != null && cvs2.contains(reemplazo.getUid())) {
                                                    cvs2.remove(reemplazo.getUid());
                                                    ni.setcvAsignadas(cvs2);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    processed.incrementAndGet();
                                    checkFinish.run();
                                })
                                .addOnFailureListener(e -> {
                                    failures.incrementAndGet();
                                    processed.incrementAndGet();
                                    checkFinish.run();
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void eliminar(OnOperationCallback callback) {
        // Primero leer la lista de collabItems asignados (ciAsignados) a esta CollabView
        if (collabId == null || uid == null) {
            callback.onFailure(new Exception("collabId o uid no definidos"));
            return;
        }

        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Si no existe el documento, nada que limpiar; intentar eliminar (idempotente)
                        db.collection("collabs")
                                .document(collabId)
                                .collection("collabViews")
                                .document(uid)
                                .delete()
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                        return;
                    }

                    List<String> ciAsignados = documentSnapshot.contains("ciAsignados")
                            ? (List<String>) documentSnapshot.get("ciAsignados")
                            : new ArrayList<>();

                    if (ciAsignados.isEmpty()) {
                        // No hay items que actualizar, eliminar directamente la collabView
                        db.collection("collabs")
                                .document(collabId)
                                .collection("collabViews")
                                .document(uid)
                                .delete()
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                        return;
                    }

                    // Actualizar cada CollabItem: quitar esta collabView de su array "cvAsignadas"
                    AtomicInteger processed = new AtomicInteger(0);
                    AtomicInteger failures = new AtomicInteger(0);

                    for (String ciId : ciAsignados) {
                        db.collection("collabs")
                                .document(collabId)
                                .collection("collabItems")
                                .document(ciId)
                                .update("cvAsignadas", FieldValue.arrayRemove(uid))
                                .addOnSuccessListener(aVoid -> {
                                    if (processed.incrementAndGet() == ciAsignados.size()) {
                                        // Una vez actualizados todos (incluso si hubo fallos individuales), intentar eliminar la collabView
                                        db.collection("collabs")
                                                .document(collabId)
                                                .collection("collabViews")
                                                .document(uid)
                                                .delete()
                                                .addOnSuccessListener(v -> callback.onSuccess())
                                                .addOnFailureListener(callback::onFailure);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    failures.incrementAndGet();
                                    if (processed.incrementAndGet() == ciAsignados.size()) {
                                        // Tras procesar todos, intentar eliminar la collabView a pesar de fallos en algunos items
                                        db.collection("collabs")
                                                .document(collabId)
                                                .collection("collabViews")
                                                .document(uid)
                                                .delete()
                                                .addOnSuccessListener(v -> callback.onSuccess())
                                                .addOnFailureListener(callback::onFailure);
                                    }
                                });
                    }
                })
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
                    Registry<String, CollabView> reg = Registry.getRegistry(CollabView.class);
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
                                CollabView cvStatic = reg.createTemplate(t.type);
                                assert cvStatic != null;

                                // Cargar los items desde la BD si existen
                                if (t.ciAsignados != null && !t.ciAsignados.isEmpty()) {
                                    cargarItemsDesdeDB(collabId, t.ciAsignados, new OnDataLoadedCallback<List<CollabItem>>() {
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
                            } catch (Exception e) {
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

    public static class CollabViewTransfer {
        public CollabViewTransfer() {
            // Necesario para Firebase
        }

        public CollabViewTransfer(String name, String type, Map<String, Object> settings, List<String> ciAsignados) {
            this.name = name;
            this.type = type;
            this.settings = settings;
            this.ciAsignados = ciAsignados;
        }

        public String name;
        public String type;
        public Map<String, Object> settings;
        public List<String> ciAsignados;
    }
}
