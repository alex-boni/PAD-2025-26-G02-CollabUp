package es.ucm.fdi.pad.collabup.modelo.collabView;

import java.util.List;

public class CollabViewSetting {

    private final CollabViewSettingsType type;

    private final String name;
    private final boolean isRequired;
    private final List<String> constraints;

    CollabViewSetting(CollabViewSettingsType type, String name, boolean isRequired, List<String> constraints) {
        this.type = type;
        this.name = name;
        this.isRequired = isRequired;
        this.constraints = constraints;
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

    public enum CollabViewSettingsType {
        TEXTO,
        NUMERO,
        LISTA_OPCIONES,
        BOOLEANO

    }

}

