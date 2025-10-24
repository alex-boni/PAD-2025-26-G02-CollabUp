package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.CreateCollabActivity; // Importa la nueva Activity

public class CollabsFragment extends Fragment {

    private FloatingActionButton fabCrearCollab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_collabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Encontrar el botón FAB
        fabCrearCollab = view.findViewById(R.id.fabCrearCollab);

        // Configurar el OnClickListener
        fabCrearCollab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la nueva actividad para crear un Collab
                Intent intent = new Intent(getActivity(), CreateCollabActivity.class);
                startActivity(intent);
            }
        });
    }
}