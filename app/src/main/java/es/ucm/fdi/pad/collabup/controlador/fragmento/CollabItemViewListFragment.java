package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
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
import es.ucm.fdi.pad.collabup.controlador.AddCollabViewActivity;
import es.ucm.fdi.pad.collabup.modelo.adapters.TabCollabItemViewAdapter;

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
            if(itemId == R.id.action_view_more){
                Toast.makeText(getContext(), "Ver más información del Collab", Toast.LENGTH_SHORT).show();
                if(collabId != null){
                    Fragment infoFragment = CollabDetailFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, infoFragment)
                            .addToBackStack("collab_item_view_list_tag")
                            .commit();
                }
                return true;
            }
            if (itemId == R.id.action_edit) {
                // Lógica de edición
                Toast.makeText(getContext(), "Editar Collab", Toast.LENGTH_SHORT).show();
                if (collabId != null) {
                    Fragment editFragment = CollabEditFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, editFragment)
                            .addToBackStack("collab_item_view_list_tag")
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
                Toast.makeText(getContext(), "Crear nuevo Collab View", Toast.LENGTH_SHORT).show();
                if(collabId != null){
                    Intent intent = new Intent(getActivity(), AddCollabViewActivity.class);

                    intent.putExtra("COLLAB_ID", this.collabId);
                    intent.putExtra("COLLAB_NAME", "Mi Collab");
                    startActivity(intent);
                }
            } else {
                // Estamos en "Collab Items"
                Toast.makeText(getContext(), "Crear nuevo Collab Item", Toast.LENGTH_SHORT).show();
                if(collabId != null){
                    Fragment createItemFragment = CreateCollabItemFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, createItemFragment)
                            .addToBackStack("collab_item_view_list_tag")
                            .commit();
                }
            }
        });
    }
}