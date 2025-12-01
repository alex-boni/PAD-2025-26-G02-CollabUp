package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.controlador.AddCollabViewActivity;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.adapters.TabCollabItemViewAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabItemViewListFragment extends Fragment {

    private static final String ARG_COLLAB_ID = "collab_id";
    private static final String RESULT_KEY = "collab_updated";
    private String collabId;
    private Collab currentCollab;

    // Componentes de la UI
    private Toolbar detailToolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCreate;

    public CollabItemViewListFragment() {
    }

    public static CollabItemViewListFragment newInstance(String collabId) {
        CollabItemViewListFragment fragment = new CollabItemViewListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLAB_ID, collabId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collabId = getArguments().getString(ARG_COLLAB_ID);
        }
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY, this, (requestKey, result) -> {
            if (requestKey.equals(RESULT_KEY) && collabId != null) {
                // El Collab ha sido actualizado, recargamos los datos desde Firestore
                Toast.makeText(getContext(), "Detectada actualización de Collab. Recargando datos...", Toast.LENGTH_SHORT).show();
                cargarDetallesDelCollabDesdeFirestore(collabId);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collab_item_view_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupToolbar();
        setupTabs();
        setupFab();
        if (collabId != null) {
            cargarDetallesDelCollabDesdeFirestore(collabId);
        }
    }

    private void initializeViews(View view) {
        detailToolbar = view.findViewById(R.id.collabViewListToolbar);
        tabLayout = view.findViewById(R.id.tabLayoutCollabItems);
        viewPager = view.findViewById(R.id.viewPager);
        fabCreate = view.findViewById(R.id.fabCreateCollabItem);
    }

    private void setupToolbar() {
        detailToolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Manejo del menú (Editar, Archivar, etc.)
        detailToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_view_more) {
                if (currentCollab != null) {
                    CollabDetailFragment detailFragment = CollabDetailFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, detailFragment)
                            .addToBackStack("collab_detail_tag")
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_edit) {
                // Lógica de edición
                if (collabId != null) {
                    Fragment editFragment = CollabEditFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, editFragment) // R.id.fragmentApp es el contenedor principal
                            .addToBackStack("collab_item_view_list_tag")
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                // Lógica de eliminación
                deleteCollab();
                return true;
            } else if (itemId == R.id.action_exit) {
                // Lógica para salir del Collab
                exitCollab();
                return true;
            }
            return false;
        });
    }

    private void deleteCollab() {
        if (currentCollab == null) {
            Toast.makeText(getContext(), "Error: No se ha cargado el Collab", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) return;
        if (!currentCollab.getCreadorId().equals(usuarioActual.getUid())) {
            Toast.makeText(getContext(), "Error: Solo el creador puede eliminar el Collab", Toast.LENGTH_SHORT).show();
        } else {
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Eliminar Collab");
        builder.setMessage("¿Estás seguro de que deseas eliminar este Collab? Esta acción no se puede deshacer.");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            // Lógica para eliminar el Collab
            currentCollab.eliminar(new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Collab eliminado", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al eliminar Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exitCollab() {
        if (currentCollab == null) {
            Toast.makeText(getContext(), "Error: No se ha cargado el Collab", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentCollab.getMiembros().size() == 1 && currentCollab.getMiembros().contains(usuarioActual.getUid())) {
            Toast.makeText(getContext(), "Eres el último miembro. Si sales, el Collab se eliminará.", Toast.LENGTH_SHORT).show();
            showDeleteConfirmationDialog();
            return;
        }
        if (currentCollab.getCreadorId().equals(usuarioActual.getUid())) {
            Toast.makeText(getContext(), "Eres el creador, se asignara a otro creador al collab", Toast.LENGTH_SHORT).show();
            newCreatorAssignment(usuarioActual.getUid());
        }
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Salir del Collab");
        builder.setMessage("¿Estás seguro de que deseas salir de este Collab?");

        builder.setPositiveButton("Salir", (dialog, which) -> {
            // Lógica para salir del Collab
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser usuarioActual = mAuth.getCurrentUser();
            if (usuarioActual == null) return;
            currentCollab.removerMiembro(usuarioActual.getUid(), new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Has salido del Collab", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al salir del Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void newCreatorAssignment(String exitingCreatorId) {
        ArrayList<String> miembros = currentCollab.getMiembros();
        String newCreatorId = null;
        for (String miembroId : miembros) {
            if (!miembroId.equals(exitingCreatorId)) {
                newCreatorId = miembroId;
                break;
            }
        }
        if (newCreatorId != null) {
            currentCollab.setCreadorId(newCreatorId, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    // Creador asignado correctamente
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al asignar nuevo creador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupTabs() {
        // Creo un nuevo adapter para el ViewPager2 (que maneja las pestañas del TabLayout)
        TabCollabItemViewAdapter adapter = new TabCollabItemViewAdapter(this, collabId);
        viewPager.setAdapter(adapter);

        // Conecto mi TabLayout con ViewPager2 donde ira collabViews y collabItems usando TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Collab Views");
                    break;
                case 1:
                    tab.setText("Collab Items");
                    break;
            }
        }).attach();
    }

    private void setupFab() {
        fabCreate.setOnClickListener(v -> {
            // Detectar en qué pestaña estamos para saber qué crear, o un nuevo Collab View o un nuevo Collab Item
            int currentTab = viewPager.getCurrentItem();

            if (currentTab == 0) {
                // Estamos en "Collab Views"
                if (collabId != null) {
                    Intent intent = new Intent(getActivity(), AddCollabViewActivity.class);

                    intent.putExtra("COLLAB_ID", this.collabId);
                    intent.putExtra("COLLAB_NAME", "Mi Collab");
                    startActivity(intent);
                }
            } else {
                // Estamos en "Collab Items"
                if (collabId != null) {
                    Fragment createItemFragment = CreateCollabItemFragment.newInstance(collabId, null);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, createItemFragment)
                            .addToBackStack("collab_item_view_list_tag")
                            .commit();
                }
            }
        });
    }

    private void cargarDetallesDelCollabDesdeFirestore(String id) {
        Collab dao = new Collab();
        dao.obtener(id, new OnDataLoadedCallback<Collab>() {
            @Override
            public void onSuccess(Collab data) {
                if (isAdded()) {
                    currentCollab = data;
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}