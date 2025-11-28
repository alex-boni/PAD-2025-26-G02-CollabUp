package es.ucm.fdi.pad.collabup.modelo.collabView;

import java.util.List;

public class CollabViewSetting {

    private final String id; // identificador único e inmutable para indexación
    private final CollabViewSettingsType type;
    private final String name;
    private final boolean isRequired;
    private final List<String> constraints;

    CollabViewSetting(String id, CollabViewSettingsType type, String name, boolean isRequired, List<String> constraints) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.isRequired = isRequired;
        this.constraints = constraints;
    }

    public String getId() {
        return id;
    }

    public CollabViewSettingsType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollabViewSetting that = (CollabViewSetting) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public enum CollabViewSettingsType {
        TEXTO,
        NUMERO,
        LISTA_OPCIONES,
        BOOLEANO

    }

}

