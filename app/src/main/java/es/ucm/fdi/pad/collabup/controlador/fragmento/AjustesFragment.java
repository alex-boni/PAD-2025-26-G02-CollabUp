package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.MainActivity;
import es.ucm.fdi.pad.collabup.controlador.EditProfileActivity;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class AjustesFragment extends Fragment {

    private Button btnLogout;
    private Button btnEditProfile;
    private FirebaseAuth mAuth;

    // Vistas de perfil
    private ImageView imgProfileSettings;
    private TextView tvProfileName;
    private TextView tvProfileUsername;
    private TextView tvProfileBio;
    private TextView tvProfileLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajustes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // vistas de botones
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // vistas de perfil
        imgProfileSettings = view.findViewById(R.id.imgProfileSettings);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        tvProfileLocation = view.findViewById(R.id.tvProfileLocation);

        // Listeners
        btnLogout.setOnClickListener(v -> logout());
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // datos del perfil
        loadUserProfile();
    }

    /**
     * Carga los datos del usuario actual de Firestore y los muestra en la UI.
     */
    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            logout(); // Si no hay usuario, no deberíamos estar aquí
            return;
        }

        Usuario dao = new Usuario();
        dao.obtener(user.getUid(), new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                if (!isAdded()) return; // se comprueba si el fragmento sigue activo

                // datos de perfil
                tvProfileName.setText(data.getNombre());
                tvProfileUsername.setText("@" + data.getUsuario()); // '@'

                // campos opcionales (bio, ubicación)
                if (data.getPresentacion() != null && !data.getPresentacion().isEmpty()) {
                    tvProfileBio.setText(data.getPresentacion());
                } else {
                    tvProfileBio.setText("Añade una biografía para describirte.");
                }

                if (data.getUbicacion() != null && !data.getUbicacion().isEmpty()) {
                    tvProfileLocation.setText(data.getUbicacion());
                } else {
                    tvProfileLocation.setText("Sin ubicación");
                }

                // foto de perfil
                if (data.getUrlFoto() != null && !data.getUrlFoto().isEmpty()) {
                    Glide.with(getContext())
                            .load(data.getUrlFoto())
                            .placeholder(android.R.drawable.ic_menu_gallery) // Imagen mientras carga
                            .error(android.R.drawable.ic_dialog_alert) // Imagen si falla
                            .into(imgProfileSettings);
                } else {
                    imgProfileSettings.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}