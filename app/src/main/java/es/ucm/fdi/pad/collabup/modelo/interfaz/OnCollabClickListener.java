package es.ucm.fdi.pad.collabup.modelo.interfaz;

import es.ucm.fdi.pad.collabup.modelo.Collab;

public interface OnCollabClickListener {

    void onCollabClick(Collab collab);
    void onFavoriteClick(Collab collab, int position);
}
