package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.MainActivity; // Importar MainActivity

public class AjustesFragment extends Fragment {

    private Button btnLogout;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout que acabamos de crear
        return inflater.inflate(R.layout.fragment_ajustes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase Auth y el botón
        mAuth = FirebaseAuth.getInstance();
        btnLogout = view.findViewById(R.id.btnLogout);

        // Configurar el OnClickListener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    /**
     * Cierra la sesión del usuario en Firebase y lo devuelve a la pantalla principal.
     */
    private void logout() {
        // 1. Cerrar la sesión en Firebase
        mAuth.signOut();

        // 2. Crear un intent para volver a MainActivity
        // Usamos getActivity() para obtener el contexto de la actividad contenedora (AppController)
        Intent intent = new Intent(getActivity(), MainActivity.class);

        // 3. Limpiamos la pila de actividades
        // Esto evita que el usuario pueda "volver" a la app (AppController)
        // después de cerrar sesión usando el botón 'Atrás'.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 4. Iniciar la actividad y finalizar la actual
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish(); // Cierra AppController
        }
    }
}