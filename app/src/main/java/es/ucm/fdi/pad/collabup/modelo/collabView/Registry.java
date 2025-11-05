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
 * Clase para registrar subclases del tipo T
 *
 * @param <T> tipo de clases a registrar
 */
public final class Registry<T> {
    private final Map<String, Class<? extends T>> registry = new HashMap<>();

    public Registry() {
    }

    /**
     * Registra una clase que implementa/extends T.
     */
    public void register(Class<? extends T> clazz) {
        if (clazz != null) {
            registry.put(clazz.getSimpleName(), clazz);
        }
    }

    /**
     * Elimina una clase registrada.
     */
    public void unregister(Class<? extends T> clazz) {
        registry.remove(clazz.getSimpleName());
    }

    /**
     * Comprueba si una clase está registrada.
     */
    public boolean contains(Class<? extends T> clazz) {
        return registry.containsValue(clazz);
    }

    /**
     * Obtiene la clase que esta registrada con el nombre
     */
    public Class<? extends T> get(String classSimpleName) {
        return registry.getOrDefault(classSimpleName, null);
    }

    /**
     * Devuelve las clases registradas (vista inmutable).
     */
    public Set<Class<? extends T>> types() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(registry.values()));
    }

    /**
     * Itera sobre las clases registradas.
     */
    public void forEach(Consumer<? super Class<? extends T>> action) {
        registry.values().forEach(action);
    }

    /**
     * Devuelve todas las instancias de las clases registradas.
     */
    public Set<Class<? extends T>> getAll() {
        return new LinkedHashSet<>(registry.values());
    }

    // Mapa estático que guarda registries por una clase clave (interfaz/superclase)
    private static final Map<Class<?>, Registry<?>> REGISTRIES = new ConcurrentHashMap<>();

    /**
     * Crea y guarda una Registry asociada a la clase keyType.
     */
    public static <K> Registry<K> createRegistry(Class<K> keyType) {
        Registry<K> r = new Registry<>();
        REGISTRIES.put(keyType, r);
        return r;
    }

    /**
     * Devuelve la Registry asociada a keyType o null si no existe.
     */
    @SuppressWarnings("unchecked")
    public static <K> Registry<K> getRegistry(Class<K> keyType) {
        return (Registry<K>) REGISTRIES.get(keyType);
    }

    /**
     * Devuelve la Registry existente o crea/guarda una nueva si no existe.
     */
    @SuppressWarnings("unchecked")
    public static <K> Registry<K> getOrCreateRegistry(Class<K> keyType) {
        return (Registry<K>) REGISTRIES.computeIfAbsent(keyType, k -> new Registry<>());
    }
}