// java
package es.ucm.fdi.pad.collabup.modelo.collabView;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registry genérica que puede registrar clases (por nombre) y además guardar instancias
 * indexadas por una clave tipada K y valor tipado V.
 *
 * @param <K> tipo de la clave usada para instancias (por ejemplo String para uid)
 * @param <V> tipo de valor/instancia (por ejemplo CollabView)
 */
public final class Registry<K, V> {
    // Mapa local de nombres de tipo -> factory que crea instancias de V
    private final Map<String, java.util.function.Supplier<? extends V>> registry = new HashMap<>();

    public Registry() {
    }

    /**
     * Registra una factory para crear instancias del tipo V asociada a la clave "typeKey".
     * No almacenamos ni guardamos Class en la registry.
     */
    public void register(String typeKey, java.util.function.Supplier<? extends V> factory) {
        if (typeKey == null || typeKey.isEmpty() || factory == null) return;
        registry.put(typeKey, factory);
    }

    /**
     * Elimina una factory registrada por su clave.
     */
    public void unregister(String typeKey) {
        if (typeKey == null || typeKey.isEmpty()) return;
        registry.remove(typeKey);
    }

    /**
     * Comprueba si existe una factory registrada para la clave indicada.
     */
    public boolean contains(String typeKey) {
        return typeKey != null && registry.containsKey(typeKey);
    }

    /**
     * Devuelve la factory registrada para la clave indicada o null.
     */
    public java.util.function.Supplier<? extends V> getFactory(String typeKey) {
        if (typeKey == null) return null;
        return registry.get(typeKey);
    }

    /**
     * Crea (mediante la factory) una instancia plantilla para el tipo identificado por typeKey.
     */
    public V createTemplate(String typeKey) {
        java.util.function.Supplier<? extends V> f = getFactory(typeKey);
        if (f == null) return null;
        return f.get();
    }

    /**
     * Devuelve las claves registradas (type keys) como vista inmutable.
     */
    public Set<String> getRegisteredKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(registry.keySet()));
    }

    /**
     * Itera sobre las factories registradas.
     */
    public void forEachFactory(Consumer<? super java.util.function.Supplier<? extends V>> action) {
        registry.values().forEach(action);
    }

    // ----------------- Gestión global de registries tipadas -----------------
    // La estructura guarda registries por par (valueType -> (keyType -> Registry))
    private static final Map<Class<?>, Map<Class<?>, Registry<?, ?>>> REGISTRIES = new ConcurrentHashMap<>();

    /**
     * Devuelve la Registry<K,V> asociada a (keyType, valueType) o null si no existe.
     */
    @SuppressWarnings("unchecked")
    public static <K2, V2> Registry<K2, V2> getRegistry(Class<K2> keyType, Class<V2> valueType) {
        if (keyType == null || valueType == null) return null;
        Map<Class<?>, Registry<?, ?>> inner = REGISTRIES.get(valueType);
        if (inner == null) return null;
        return (Registry<K2, V2>) inner.get(keyType);
    }

    /**
     * Devuelve la Registry<K,V> asociada a (keyType, valueType), creando una nueva si no existe.
     */
    @SuppressWarnings("unchecked")
    public static <K2, V2> Registry<K2, V2> getOrCreateRegistry(Class<K2> keyType, Class<V2> valueType) {
        if (keyType == null || valueType == null) return null;
        Map<Class<?>, Registry<?, ?>> inner = REGISTRIES.computeIfAbsent(valueType, v -> new ConcurrentHashMap<>());
        return (Registry<K2, V2>) inner.computeIfAbsent(keyType, k -> new Registry<K2, V2>());
    }

    /**
     * Conveniencia: asumimos clave String si sólo se pasa valueType (mantener compatibilidad).
     */
    public static <V2> Registry<String, V2> getRegistry(Class<V2> valueType) {
        return getRegistry(String.class, valueType);
    }

    /**
     * Conveniencia: crear/get con clave String por defecto.
     */
    public static <V2> Registry<String, V2> getOrCreateRegistry(Class<V2> valueType) {
        return getOrCreateRegistry(String.class, valueType);
    }

    // ----------------- Instancias tipadas por Registry -----------------
    // Instancias por clave para este Registry<K,V>
    private final Map<K, V> instances = new ConcurrentHashMap<>();

    /**
     * Registra una instancia en esta Registry (tipada) con la clave uid.
     */
    public void registerInstance(K key, V instance) {
        if (key == null || instance == null) return;
        instances.put(key, instance);
    }

    /**
     * Recupera una instancia registrada localmente por clave.
     */
    public V getInstance(K key) {
        if (key == null) return null;
        return instances.get(key);
    }

    /**
     * Elimina una instancia registrada localmente por clave.
     */
    public void unregisterInstance(K key) {
        if (key == null) return;
        instances.remove(key);
    }

    /**
     * Devuelve una vista inmutable del mapa key->instancia para esta Registry tipada.
     */
    public Map<K, V> allInstances() {
        return Collections.unmodifiableMap(new HashMap<>(instances));
    }

    // ----------------- Métodos estáticos de conveniencia que delegan en la registry tipada -----------------

    public static <K2, V2> void registerInstance(Class<K2> keyType, Class<V2> valueType, K2 key, V2 instance) {
        if (keyType == null || valueType == null || key == null || instance == null) return;
        Registry<K2, V2> r = getOrCreateRegistry(keyType, valueType);
        if (r != null) r.registerInstance(key, instance);
    }

    // Conveniencia cuando la clave es String (caso más habitual para uid)
    public static <V2> void registerInstance(Class<V2> valueType, String key, V2 instance) {
        registerInstance(String.class, valueType, key, instance);
    }

    @SuppressWarnings("unchecked")
    public static <K2, V2> V2 getInstance(Class<K2> keyType, Class<V2> valueType, K2 key) {
        if (keyType == null || valueType == null || key == null) return null;
        Registry<K2, V2> r = getRegistry(keyType, valueType);
        if (r == null) return null;
        return r.getInstance(key);
    }

    // Conveniencia String-key
    public static <V2> V2 getInstance(Class<V2> valueType, String key) {
        return getInstance(String.class, valueType, key);
    }

    public static <K2, V2> void unregisterInstance(Class<K2> keyType, Class<V2> valueType, K2 key) {
        if (keyType == null || valueType == null || key == null) return;
        Registry<K2, V2> r = getRegistry(keyType, valueType);
        if (r == null) return;
        r.unregisterInstance(key);
    }

    // Conveniencia String-key
    public static <V2> void unregisterInstance(Class<V2> valueType, String key) {
        unregisterInstance(String.class, valueType, key);
    }

    @SuppressWarnings("unchecked")
    public static <K2, V2> Map<K2, V2> allInstances(Class<K2> keyType, Class<V2> valueType) {
        Registry<K2, V2> r = getRegistry(keyType, valueType);
        if (r == null) return Collections.emptyMap();
        return r.allInstances();
    }

    // Conveniencia String-key
    public static <V2> Map<String, V2> allInstances(Class<V2> valueType) {
        return allInstances(String.class, valueType);
    }
}