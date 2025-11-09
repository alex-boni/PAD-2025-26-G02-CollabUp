package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.adapters.CardAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnCollabClickListener;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabListFragment extends Fragment implements OnCollabClickListener {

    private static final String RESULT_KEY = "collab_created";
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private List<Collab> originalCollabs;

    private String currentFilter = "all";

    public CollabListFragment() {
    }

    public static CollabListFragment newInstance(String param1, String param2) {
        CollabListFragment fragment = new CollabListFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<Collab> collabs;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collab_list_layout, container, false);

        mAuth = FirebaseAuth.getInstance();
        
        searchEditText = view.findViewById(R.id.searchEditText);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        recyclerView = view.findViewById(R.id.recyclerViewCollab);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        collabs = new ArrayList<>();
        originalCollabs= new ArrayList<>();
        adapter = new CardAdapter(collabs, this);
        recyclerView.setAdapter(adapter);

        setupSearchListener();
        setupFilterListener();
        cargarCollabsDesdeFirestore();
        setupFragmentResultListener();

        FloatingActionButton fab = view.findViewById(R.id.fabCreateCollab);
        fab.setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentApp, CreateCollabFragment.newInstance())
                .addToBackStack(null)
                .commit()
        );
        return view;
    }

    private void cargarCollabsDesdeFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        Collab collabDAO = new Collab();
        collabDAO.obtenerCollabsDelUsuario(userId, new OnDataLoadedCallback<ArrayList<Collab>>() {
            @Override
            public void onSuccess(ArrayList<Collab> collabs) {
                CollabListFragment.this.collabs.clear();
                CollabListFragment.this.collabs.addAll(collabs);
                originalCollabs.clear();
                originalCollabs.addAll(collabs);
                adapter.notifyDataSetChanged();
                
                if (collabs.isEmpty()) {
                    Toast.makeText(getContext(), "No tienes Collabs aún. ¡Crea uno!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error al cargar Collabs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY, this, (requestKey, result) -> {
            String collabId = result.getString("collabId");
            
            if (collabId != null) {
                cargarCollabsDesdeFirestore();
            }
        });
    }

    @Override
    public void onCollabClick(Collab collab) {
        Bundle args = new Bundle();
        args.putString("collab_id", collab.getId());

        CollabDetailFragment detailFragment = new CollabDetailFragment();
        detailFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentApp, detailFragment)
                .addToBackStack("collab_list")
                .commit();
        Toast.makeText(getContext(), "Abriendo Collab: " + collab.getNombre(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onFavoriteClick(Collab collab, int position) {
        if(!collab.estaEliminado()){
            boolean noEraFavorito = !collab.esFavorito();
            String nuevoEstado = (noEraFavorito) ? "favorito" : "activo";
            collab.setEstado(nuevoEstado);
            collab.modificar(collab, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    adapter.notifyItemChanged(position);
                    String mensaje = noEraFavorito ? "Marcado como favorito" : "Desmarcado como favorito";
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error al actualizar favorito: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(getContext(), "No se puede cambiar favorito de un Collab eliminado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Aplicar el filtro de búsqueda cada vez que el texto cambia
                applyFilterAndSearch(s.toString(), currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterListener() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // checkedIds es una lista de IDs. Como tenemos singleSelection="true", solo nos interesa el primero.
            if (checkedIds.isEmpty()) {
                // Si el usuario deselecciona un chip (raro en singleSelection), no hacemos nada
                return;
            }

            int checkedId = checkedIds.get(0); // Obtenemos el ID del chip seleccionado

            // 1. Determinar el nuevo filtro
            if (checkedId == R.id.chipAll) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipFavorites) {
                currentFilter = "favorites";
            } else if (checkedId == R.id.chipActives) {
                currentFilter = "actives";
            } else if (checkedId == R.id.chipDeleted) {
                currentFilter = "deleted";
            }

            String query = searchEditText.getText() != null ? searchEditText.getText().toString() : "";
            applyFilterAndSearch(query, currentFilter);
        });
    }
    private void applyFilterAndSearch(String query, String filterType) {
        List<Collab> filteredList = new ArrayList<>();

        for (Collab collab : originalCollabs) {
            boolean matchesFilter = false;
            switch (filterType) {
                case "all":
                    matchesFilter = true; // El filtro "todos" no excluye nada
                    break;
                case "actives":
                    matchesFilter = !collab.estaEliminado();
                    break;
                case "favorites":
                    matchesFilter = collab.getEstado().toLowerCase().contains("favorito");
                    break;
                case "deleted":
                    matchesFilter = collab.getEstado().toLowerCase().contains("eliminado");
                    break;
            }

            if (matchesFilter) {
                if (query.isEmpty() || collab.getNombre().toLowerCase().contains(query.toLowerCase()) || collab.getDescripcion().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(collab);
                }
            }
        }
        collabs.clear();
        collabs.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}

