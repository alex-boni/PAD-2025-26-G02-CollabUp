package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.adapters.CollabViewListAdapter;
import es.ucm.fdi.pad.collabup.modelo.collabView.AbstractCollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabViewStatic;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class CollabViewsListFragment extends Fragment {

    private static final String ARG_COLLAB_ID = "collab_id";
    private String collabId;

    private RecyclerView recyclerView;
    private CollabViewListAdapter adapter;
    private List<CollabView> items;

    public CollabViewsListFragment() {
    }

    public static CollabViewsListFragment newInstance(String collabId) {
        CollabViewsListFragment fragment = new CollabViewsListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collab_views_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerCollabViews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        items = new ArrayList<>();
        adapter = new CollabViewListAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);

        // Cargamos los datos inicialmente
        if (collabId != null) {
            cargarCollabViewsDesdeFirestore();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (collabId != null) {
            cargarCollabViewsDesdeFirestore();
        }
    }

    private void cargarCollabViewsDesdeFirestore() {
        if (collabId == null) {
            Toast.makeText(getContext(), "ERROR: collabId es NULO", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear una instancia estatica de CollabView para usar obtenerListado
        AbstractCollabView tempCollabView = new CollabViewStatic();
        tempCollabView.setCollabId(collabId);

        // Usar el método obtenerListado de AbstractCollabView
        tempCollabView.obtenerListado(new OnDataLoadedCallback<>() {
            @Override
            public void onSuccess(ArrayList<CollabView> collabViews) {
                items.clear();
                items.addAll(collabViews);
                adapter.notifyDataSetChanged();

                if (collabViews.isEmpty()) {
                    Toast.makeText(getContext(), "No hay CollabViews en este Collab", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error al cargar CollabViews: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}