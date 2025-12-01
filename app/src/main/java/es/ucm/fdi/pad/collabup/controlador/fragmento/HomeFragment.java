package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.LoginController;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.adapters.CardAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnCollabClickListener;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class HomeFragment extends Fragment implements OnCollabClickListener {

    // Vistas
    private TextView tvWelcomeName;
    private ImageView imgProfileHome;
    private TextView tvSummaryText;
    private RecyclerView rvFeaturedCollabs;
    private MaterialButton btnQuickCreate, btnQuickCalendar;
    private TextView tvViewAll;

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
        tvSummaryText = view.findViewById(R.id.tvSummaryText);
        rvFeaturedCollabs = view.findViewById(R.id.rvFeaturedCollabs);
        btnQuickCreate = view.findViewById(R.id.btnQuickCreate);
        btnQuickCalendar = view.findViewById(R.id.btnQuickCalendar);
        tvViewAll = view.findViewById(R.id.tvViewAll);

        mAuth = FirebaseAuth.getInstance();

        // Configurar RecyclerView (Horizontal)
        collabList = new ArrayList<>();
        // NOTA: Para que se vea bien, el CardAdapter debería inflar 'item_collab_horizontal'
        // Si tu CardAdapter solo infla el vertical, se verán tarjetas grandes.
        // Lo ideal sería modificar CardAdapter para aceptar un layoutId en el constructor.
        // Por ahora usamos el existente, asumiendo que se adaptará o usaremos el horizontal layout si modificas el adapter.
        // *Recomendación*: Crea un 'HorizontalCollabAdapter' simple o pasa el layout al constructor.
        adapter = new CardAdapter(collabList, this);

        // Configuramos el LayoutManager en HORIZONTAL
        rvFeaturedCollabs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeaturedCollabs.setAdapter(adapter);

        setupListeners();
        cargarDatosUsuario();
        cargarCollabsDestacados();
    }

    private void setupListeners() {
        btnQuickCreate.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentApp, CreateCollabFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        btnQuickCalendar.setOnClickListener(v -> {
            // Navegar al fragmento calendario
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentApp, new CalendarioFragment()) // Asegúrate de tener CalendarioFragment
                    .addToBackStack(null)
                    .commit();
        });

        tvViewAll.setOnClickListener(v -> {
            // Navegar a la lista completa (CollabListFragment)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentApp, new CollabListFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
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
                    tvWelcomeName.setText(data.getNombre() + "!");

                    if (data.getUrlFoto() != null && !data.getUrlFoto().isEmpty()) {
                        Glide.with(getContext())
                                .load(data.getUrlFoto())
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(imgProfileHome);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) tvWelcomeName.setText("Usuario!");
            }
        });
    }

    private void cargarCollabsDestacados() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Collab collabDAO = new Collab();
        collabDAO.obtenerCollabsDelUsuario(currentUser.getUid(), new OnDataLoadedCallback<ArrayList<Collab>>() {
            @Override
            public void onSuccess(ArrayList<Collab> collabs) {
                if (!isAdded()) return;

                collabList.clear();
                int count = 0;
                // Filtramos solo los favoritos o los primeros 5 activos
                for (Collab collab : collabs) {
                    if (collab.estaActivo() && count < 5) { // Límite de 5 para el carrusel
                        collabList.add(collab);
                        count++;
                    }
                }

                adapter.notifyDataSetChanged();

                // Actualizar el resumen del día (Simulado por ahora)
                if (collabList.isEmpty()) {
                    tvSummaryText.setText("No tienes Collabs activos");
                } else {
                    tvSummaryText.setText("Tienes " + collabList.size() + " Collabs activos hoy");
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Manejar error
            }
        });
    }

    @Override
    public void onCollabClick(Collab collab) {
        CollabDetailFragment detailFragment = CollabDetailFragment.newInstance(collab.getId());
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentApp, detailFragment)
                    .addToBackStack("home")
                    .commit();
        }
    }

    @Override
    public void onFavoriteClick(Collab collab, int position) {
        // Lógica de favorito igual que antes
        boolean noEraFavorito = !collab.esFavorito();
        String nuevoEstado = (noEraFavorito) ? "favorito" : "activo";
        collab.setEstado(nuevoEstado);
        collab.modificar(collab, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                adapter.notifyItemChanged(position);
                Toast.makeText(getContext(), "Actualizado", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}