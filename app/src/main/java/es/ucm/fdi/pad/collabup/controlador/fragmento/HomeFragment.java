package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Importar
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Importar
import androidx.recyclerview.widget.RecyclerView; // Importar

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList; // Importar
import java.util.List; // Importar

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.LoginController;
import es.ucm.fdi.pad.collabup.modelo.Collab; // Importar
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.adapters.CardAdapter; // Importar
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnCollabClickListener; // Importar
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback; // Importar

// Implementar OnCollabClickListener para manejar clics en la lista
public class HomeFragment extends Fragment implements OnCollabClickListener {

    // Vistas de la UI
    private TextView tvWelcomeName;
    private ImageView imgProfileHome; // O CircleImageView
    private ImageButton btnFriends;
    private RecyclerView rvRecentCollabs;

    // Firebase y Datos
    private FirebaseAuth mAuth;
    private CardAdapter adapter;
    private List<Collab> collabList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        tvWelcomeName = view.findViewById(R.id.tvWelcomeName);
        imgProfileHome = view.findViewById(R.id.imgProfileHome);
        btnFriends = view.findViewById(R.id.btnFriends);
        rvRecentCollabs = view.findViewById(R.id.rvRecentCollabs);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configurar RecyclerView
        collabList = new ArrayList<>();
        adapter = new CardAdapter(collabList, this); // 'this' es el OnCollabClickListener
        rvRecentCollabs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentCollabs.setAdapter(adapter);

        // Listeners
        btnFriends.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Actividad de Amigos (Próximamente)", Toast.LENGTH_SHORT).show();
        });

        // Cargar los datos (perfil y collabs)
        cargarDatosUsuario();
        cargarCollabsActivos();
    }

    /**
     * Carga el perfil del usuario (nombre y foto) en la barra superior.
     */
    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Si no hay usuario, ir a Login
            startActivity(new Intent(getContext(), LoginController.class));
            if (getActivity() != null) getActivity().finish();
            return;
        }

        String uid = user.getUid();
        Usuario dao = new Usuario();
        dao.obtener(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                if (isAdded()) {
                    // Saludar al usuario
                    tvWelcomeName.setText("¡Hola, " + data.getNombre() + "!");

                    // Cargar la imagen de perfil
                    if (data.getUrlFoto() != null && !data.getUrlFoto().isEmpty()) {
                        Glide.with(getContext())
                                .load(data.getUrlFoto())
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(imgProfileHome);
                    } else {
                        imgProfileHome.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    tvWelcomeName.setText("¡Hola!");
                    Toast.makeText(getContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Carga los collabs del usuario y los filtra para mostrar solo los activos.
     */
    private void cargarCollabsActivos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Usar temp_user_dev si estás en modo desarrollo, o el UID real
        // String userId = "temp_user_dev";
        String userId = currentUser.getUid();

        Collab collabDAO = new Collab();
        collabDAO.obtenerCollabsDelUsuario(userId, new OnDataLoadedCallback<ArrayList<Collab>>() {
            @Override
            public void onSuccess(ArrayList<Collab> collabs) {
                if (!isAdded()) return;

                collabList.clear();
                // Filtrar para mostrar solo activos
                for (Collab collab : collabs) {
                        collabList.add(collab);
                }

                // TODO: Ordenar por fecha más reciente (si 'Collab' tiene fecha)

                adapter.notifyDataSetChanged();

                if (collabList.isEmpty()) {
                    Toast.makeText(getContext(), "No tienes Collabs activos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar Collabs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // --- Implementación de OnCollabClickListener ---

    @Override
    public void onCollabClick(Collab collab) {
        // Abrir el fragmento de detalle
        CollabDetailFragment detailFragment = CollabDetailFragment.newInstance(collab.getId());

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentApp, detailFragment)
                    .addToBackStack("home") // Para poder volver atrás
                    .commit();
        }
    }

    @Override
    public void onFavoriteClick(Collab collab, int position) {
        // Marcar como favorito (copiado de CollabListFragment)
            boolean noEraFavorito = !collab.esFavorito();
            String nuevoEstado = (noEraFavorito) ? "favorito" : "activo";
            collab.setEstado(nuevoEstado);
            collab.modificar(collab, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    adapter.notifyItemChanged(position);
                    String mensaje = noEraFavorito ? "Marcado como favorito" : "Desmarcado";
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                    // Refrescar la lista si ya no es "activo" (opcional)
                    // cargarCollabsActivos();
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            });
    }
}