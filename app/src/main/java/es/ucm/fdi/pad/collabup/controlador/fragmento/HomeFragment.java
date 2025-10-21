package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.LoginController;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class HomeFragment extends Fragment {

    private TextView txtDatosUsuario;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el layout para este fragmento
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa las vistas y Firebase
        txtDatosUsuario = view.findViewById(R.id.txtDatosUsuario);
        mAuth = FirebaseAuth.getInstance();

        // Carga los datos
        cargarDatosUsuario();
    }

    /**
     * Obtiene el usuario actual de FirebaseAuth y carga sus datos de Firestore
     * usando la clase DAO (Usuario).
     */
    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            txtDatosUsuario.setText("Cargando datos..."); // Mensaje temporal

            // Usamos la instancia de Usuario como DAO para llamar al método 'obtener'
            Usuario dao = new Usuario();
            dao.obtener(uid, new OnDataLoadedCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario data) {
                    if (isAdded()) { // Comprueba si el fragmento sigue "vivo"
                        String datos = "¡Bienvenido, " + data.getNombre() + "!" +
                                "\n\nEmail: " + data.getEmail() +
                                "\nUsuario: " + data.getUsuario() +
                                "\nUID: " + data.getUID();
                        txtDatosUsuario.setText(datos);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        txtDatosUsuario.setText("Error al cargar datos de Firestore: " + e.getMessage());
                    }
                }
            });

        } else {
            // Si no hay usuario, vuelve al Login
            Toast.makeText(getContext(), "No hay sesión iniciada.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), LoginController.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}