package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import es.ucm.fdi.pad.collabup.R;

public class CollabItemViewListFragment extends Fragment {

    private static final String ARG_COLLAB_ID = "collab_id";
    private String collabId;

    // Componentes de la UI
    private Toolbar detailToolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCreate;

    public CollabItemViewListFragment() {}

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

        // Manejo del menú (Editar, Salir, eliminar, ver mas - Falta por terminar de implementar)
        detailToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // Lógica de edición
                Toast.makeText(getContext(), "Editar Collab", Toast.LENGTH_SHORT).show();
                if (collabId != null) {
                    Fragment editFragment = CollabEditFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, editFragment)
                            .addToBackStack("collab_item_view_listt_tag")
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                // Lógica de eliminación
                Toast.makeText(getContext(), "Eliminar Collab", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_exit) {
                // Lógica para salir del Collab
                Toast.makeText(getContext(), "Salir del Collab", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupTabs() {
        // Creo un nuevo adapter para el ViewPager2 (que maneja las pestañas del TabLayout)
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, collabId);
        viewPager.setAdapter(adapter);

        // Conecto el TabLayout con ViewPager2 donde ira collabViews y collabItems usando TabLayoutMediator
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
                Toast.makeText(getContext(), "Crear nueva Vista", Toast.LENGTH_SHORT).show();
                //TODO: Lógica para abrir fragmento de crear vista...
            } else {
                // Estamos en "Collab Items"
                Toast.makeText(getContext(), "Crear nuevo Item", Toast.LENGTH_SHORT).show();
                // TODO: Lógica para abrir fragmento de crear item...
            }
        });
    }

    // --- CLASE INTERNA: Adapter para las Pestañas ---
    // Esta clase decide qué Fragmento mostrar en cada pestaña
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String collabId;

        public ViewPagerAdapter(@NonNull Fragment fragment, String collabId) {
            super(fragment);
            this.collabId = collabId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Aquí debes devolver el Fragmento correspondiente a cada pestaña
            // Debes crear estos fragmentos (CollabViewsFragment y CollabItemsFragment)

            switch (position) {
                case 0:

//                     return CollabViewsFragment.newInstance(collabId);
                     return CollabItemsListFragment.newInstance(collabId);
                case 1:
                     return CollabItemsListFragment.newInstance(collabId);
                default:
                    return new PlaceholderFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Contador de pestañas de mi tabLayout
        }
    }

    // --- Fragmento temporal solo para evitar errores hasta que crees los reales ---
    public static class PlaceholderFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Simplemente devuelve una vista vacía o un texto
            return new View(container.getContext());
        }
    }
}